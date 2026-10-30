package com.fons.cloud.canal.core;

import cn.hutool.core.lang.Assert;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author qiyuan.hong
 * @version 1.0
 * @date 2023/5/19 10:23
 */
public class BaseParameterizedTypeReferenceSupport<T> {

    private final Class<T> klass;
    private final Class<?> childKlass;

    @SuppressWarnings("unchecked")
    public BaseParameterizedTypeReferenceSupport() {
        childKlass = getClass();
        Type type = findParameterizedTypeReferenceSubClass(childKlass);
        Assert.isInstanceOf(ParameterizedType.class, type, "Type must be a parameterized type");
        ParameterizedType parameterizedType = (ParameterizedType) type;
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        Assert.isTrue(actualTypeArguments.length == 1, () -> new RuntimeException("Number of type arguments must be 1"));
        this.klass = (Class<T>) actualTypeArguments[0];
    }


    public Class<T> getKlass() {
        return klass;
    }

    public Class<?> getChildKlass() {
        return childKlass;
    }

    /**
     * 递归搜索ParameterizedType类型引用的父类 - 目的是获取泛型参数类型
     *
     * @param child child
     * @return Class
     */
    private static Type findParameterizedTypeReferenceSubClass(Class<?> child) {
        Type genericSuperclass = child.getGenericSuperclass();
        if (!(genericSuperclass instanceof ParameterizedType)) {
            Class<?> parent = child.getSuperclass();
            if (Object.class == parent) {
                throw new IllegalStateException("Expected ParameterizedTypeReference superclass");
            }
            return findParameterizedTypeReferenceSubClass(parent);
        }
        return genericSuperclass;
    }
}
