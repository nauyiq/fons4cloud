package com.fons.cloud.common.sensitive;

import com.fons.cloud.common.response.MultiResponse;
import com.fons.cloud.common.response.Response;
import com.fons.cloud.common.response.SingleResponse;
import com.fons.cloud.common.result.PageResult;
import com.fons.cloud.common.result.R;
import com.github.houbb.sensitive.annotation.Sensitive;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 敏感类型识别器，web/dubbo 两条通道在写出响应前共用的脱敏判定入口。
 *
 * <p>识别依据是 DTO 字段上的 {@code @Sensitive} 注解——它是"什么是敏感数据"语义的唯一来源，
 * 框架不做字段名猜测，未标注的类即使包含手机号等字段也不会被脱敏，避免误伤机器消费方。
 * 判定分两步：先从声明返回类型解出业务载荷类型（剥掉 R/SingleResponse/MultiResponse、
 * 分页结果及集合容器的外壳），再对载荷类做字段级扫描；同时参考运行时实际载荷类型，
 * 兜底声明为父类/接口的多态场景。</p>
 *
 * <p>检测结果按类缓存，热路径为 O(1) 查表，{@code desCopy} 的反射开销
 * 只发生在真正含敏感字段的响应上。</p>
 *
 * <p>边界：泛型声明为 Object 或 Map&lt;String,Object&gt; 的载荷无法静态识别，
 * 应使用类型化 DTO 表达；嵌套自定义类型超过 {@link #MAX_NESTED_DEPTH} 层不递归；
 * SSE/流式响应不经消息转换器路径，需流内自理。</p>
 *
 * @author hongqy
 */
public final class SensitiveTypeDetector {

    /** 嵌套自定义类型递归深度上限，防止大对象图全量扫描 */
    private static final int MAX_NESTED_DEPTH = 3;

    /** 声明类型解包深度上限，保护多层容器嵌套 */
    private static final int MAX_UNWRAP_DEPTH = 5;

    /** 运行时集合载荷的代表性采样上限，多态集合按前 N 个不同类型判定 */
    private static final int MAX_RUNTIME_SAMPLE = 8;

    /** 类级检测结果缓存：Class -> 可达字段图内是否含 @Sensitive 字段 */
    private static final Map<Class<?>, Boolean> SENSITIVE_CACHE = new ConcurrentHashMap<>();

    private SensitiveTypeDetector() {
    }

    /**
     * 判断响应是否需要脱敏：声明返回类型与运行时响应体任一命中即返回 true。
     *
     * @param declaredType 声明返回类型（含泛型），可为 null
     * @param body         运行时响应体，可为 null
     * @return 含敏感数据返回 true
     */
    public static boolean needSanitize(Type declaredType, Object body) {
        if (declaredType != null && hasSensitivePayload(resolvePayloadTypes(declaredType))) {
            return true;
        }
        return hasSensitivePayload(extractRuntimePayloadTypes(body));
    }

    /**
     * 从声明返回类型解出业务载荷类型：剥掉响应容器与集合容器的外壳。
     *
     * @param declaredType 声明返回类型
     * @return 载荷类型集合，无法解析时为空集合
     */
    public static Set<Class<?>> resolvePayloadTypes(Type declaredType) {
        Set<Class<?>> payloadTypes = new HashSet<>();
        unwrap(declaredType, payloadTypes, 0);
        return payloadTypes;
    }

    /**
     * 判断类型自身或可达字段图内是否含 @Sensitive 字段，结果按类缓存。
     *
     * @param type 待检测类型
     * @return 含敏感字段返回 true
     */
    public static boolean hasSensitive(Class<?> type) {
        if (type == null || isSkippedType(type)) {
            return false;
        }
        return SENSITIVE_CACHE.computeIfAbsent(type, t -> scan(t, new HashSet<>(), 0));
    }

    /**
     * 解包声明类型：容器（响应外壳/集合/Map/数组）递归取泛型实参，其余类型视为载荷。
     */
    private static void unwrap(Type type, Set<Class<?>> out, int depth) {
        if (type == null || depth > MAX_UNWRAP_DEPTH) {
            return;
        }
        switch (type) {
            case ParameterizedType parameterized -> {
                Class<?> rawType = (Class<?>) parameterized.getRawType();
                if (isContainer(rawType)) {
                    Type[] typeArguments = parameterized.getActualTypeArguments();
                    if (Map.class.isAssignableFrom(rawType) && typeArguments.length == 2) {
                        // Map 载荷只关心 value 的类型
                        unwrap(typeArguments[1], out, depth + 1);
                    } else {
                        for (Type typeArgument : typeArguments) {
                            unwrap(typeArgument, out, depth + 1);
                        }
                    }
                    return;
                }
                out.add(rawType);
                return;
            }
            case GenericArrayType arrayType -> {
                unwrap(arrayType.getGenericComponentType(), out, depth + 1);
                return;
            }
            case WildcardType wildcard -> {
                for (Type upperBound : wildcard.getUpperBounds()) {
                    unwrap(upperBound, out, depth + 1);
                }
                return;
            }
            case TypeVariable<?> typeVariable -> {
                for (Type bound : typeVariable.getBounds()) {
                    unwrap(bound, out, depth + 1);
                }
                return;
            }
            case Class<?> clazz -> {
                if (clazz.isArray()) {
                    unwrap(clazz.getComponentType(), out, depth + 1);
                    return;
                }
                if (!isSkippedType(clazz)) {
                    out.add(clazz);
                }
            }
            default -> {
            }
        }
    }

    /**
     * 提取运行时响应体的载荷类型：解包响应容器与集合，采样集合元素的实际类型。
     */
    private static Set<Class<?>> extractRuntimePayloadTypes(Object body) {
        Set<Class<?>> payloadTypes = new HashSet<>();
        collectRuntimePayload(body, payloadTypes, 0);
        return payloadTypes;
    }

    /**
     * 运行时载荷收集：只解包框架响应容器与集合，采样上限防止超大集合逐元素遍历。
     */
    private static void collectRuntimePayload(Object body, Set<Class<?>> out, int depth) {
        if (body == null || depth > MAX_UNWRAP_DEPTH || out.size() >= MAX_RUNTIME_SAMPLE) {
            return;
        }
        if (body instanceof R<?> r) {
            collectRuntimePayload(r.getData(), out, depth + 1);
            return;
        }
        if (body instanceof SingleResponse<?> singleResponse) {
            collectRuntimePayload(singleResponse.getData(), out, depth + 1);
            return;
        }
        if (body instanceof MultiResponse<?> multiResponse) {
            collectRuntimePayload(multiResponse.getData(), out, depth + 1);
            return;
        }
        if (body instanceof PageResult<?> pageResult) {
            collectRuntimePayload(pageResult.getResultList(), out, depth + 1);
            return;
        }
        if (body instanceof Collection<?> collection) {
            for (Object element : collection) {
                collectRuntimePayload(element, out, depth + 1);
                if (out.size() >= MAX_RUNTIME_SAMPLE) {
                    return;
                }
            }
            return;
        }
        if (!isSkippedType(body.getClass())) {
            out.add(body.getClass());
        }
    }

    /**
     * 递归扫描类型字段图：自身或父类字段带 @Sensitive 直接命中；
     * 否则对嵌套自定义类型递归，visited 防环、深度上限防大对象图。
     */
    private static boolean scan(Class<?> type, Set<Class<?>> visited, int depth) {
        if (depth > MAX_NESTED_DEPTH || isSkippedType(type) || !visited.add(type)) {
            return false;
        }
        if (hasSensitiveField(type)) {
            return true;
        }
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                Class<?> nestedType = resolveFieldType(field);
                if (nestedType != null && scan(nestedType, visited, depth + 1)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 自身及父类字段上是否存在 @Sensitive 注解。
     */
    private static boolean hasSensitiveField(Class<?> type) {
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(Sensitive.class)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 解析字段声明的业务类型：Collection/Map 取泛型元素，数组取组件类型，其余取字段类型。
     */
    private static Class<?> resolveFieldType(Field field) {
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType parameterized) {
            Class<?> rawType = (Class<?>) parameterized.getRawType();
            Type[] typeArguments = parameterized.getActualTypeArguments();
            if (Collection.class.isAssignableFrom(rawType) && typeArguments.length == 1) {
                return toClass(typeArguments[0]);
            }
            if (Map.class.isAssignableFrom(rawType) && typeArguments.length == 2) {
                return toClass(typeArguments[1]);
            }
            return rawType;
        }
        Class<?> fieldType = field.getType();
        return fieldType.isArray() ? fieldType.getComponentType() : fieldType;
    }

    /**
     * Type 归一化为 Class；类型变量等无法归一的返回 null。
     */
    private static Class<?> toClass(Type type) {
        if (type instanceof Class<?> clazz) {
            return clazz;
        }
        if (type instanceof ParameterizedType parameterized) {
            return (Class<?>) parameterized.getRawType();
        }
        return null;
    }

    /**
     * 判断载荷类型集合内是否有任一类型命中敏感检测。
     */
    private static boolean hasSensitivePayload(Set<Class<?>> payloadTypes) {
        for (Class<?> payloadType : payloadTypes) {
            if (hasSensitive(payloadType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 响应外壳容器：R/SingleResponse/MultiResponse 及其他 Response 子类、分页结果、集合与 Map。
     */
    private static boolean isContainer(Class<?> rawType) {
        return Response.class.isAssignableFrom(rawType)
                || PageResult.class.isAssignableFrom(rawType)
                || Collection.class.isAssignableFrom(rawType)
                || Map.class.isAssignableFrom(rawType);
    }

    /**
     * 跳过 JDK/基础类型：它们不携带业务敏感语义，同时避免误扫第三方内部类。
     */
    private static boolean isSkippedType(Class<?> type) {
        if (type.isPrimitive() || type.isEnum() || type.isArray()) {
            return true;
        }
        String name = type.getName();
        return name.startsWith("java.")
                || name.startsWith("javax.")
                || name.startsWith("jdk.")
                || name.startsWith("sun.");
    }
}
