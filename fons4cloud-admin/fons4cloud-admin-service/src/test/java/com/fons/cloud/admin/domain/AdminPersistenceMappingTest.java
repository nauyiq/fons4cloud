package com.fons.cloud.admin.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.fons.cloud.admin.domain.entity.AdminGovernanceAudit;
import com.fons.cloud.admin.domain.entity.AdminGovernanceChange;
import com.fons.cloud.admin.domain.entity.AdminGovernanceRelease;
import com.fons.cloud.admin.domain.entity.AdminGovernanceResource;
import com.fons.cloud.admin.domain.entity.AdminGovernanceSnapshot;
import com.fons.cloud.admin.domain.entity.AdminPermission;
import com.fons.cloud.admin.domain.entity.AdminRole;
import com.fons.cloud.admin.domain.entity.AdminRolePermission;
import com.fons.cloud.admin.domain.entity.AdminUser;
import com.fons.cloud.admin.domain.entity.AdminUserRole;
import com.fons.cloud.admin.domain.mapper.AdminGovernanceAuditMapper;
import com.fons.cloud.admin.domain.mapper.AdminGovernanceChangeMapper;
import com.fons.cloud.admin.domain.mapper.AdminGovernanceReleaseMapper;
import com.fons.cloud.admin.domain.mapper.AdminGovernanceResourceMapper;
import com.fons.cloud.admin.domain.mapper.AdminGovernanceSnapshotMapper;
import com.fons.cloud.admin.domain.mapper.AdminPermissionMapper;
import com.fons.cloud.admin.domain.mapper.AdminRoleMapper;
import com.fons.cloud.admin.domain.mapper.AdminRolePermissionMapper;
import com.fons.cloud.admin.domain.mapper.AdminUserMapper;
import com.fons.cloud.admin.domain.mapper.AdminUserRoleMapper;
import com.fons.cloud.db.entity.CommonEntity;
import com.fons.cloud.db.mybatisplus.BasePlusMapper;
import org.apache.ibatis.annotations.Mapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * admin 持久化映射结构测试。
 */
class AdminPersistenceMappingTest {

    private static final List<Mapping> MAPPINGS = List.of(
            new Mapping(AdminUser.class, "admin_user", AdminUserMapper.class),
            new Mapping(AdminRole.class, "admin_role", AdminRoleMapper.class),
            new Mapping(AdminPermission.class, "admin_permission", AdminPermissionMapper.class),
            new Mapping(AdminUserRole.class, "admin_user_role", AdminUserRoleMapper.class),
            new Mapping(AdminRolePermission.class, "admin_role_permission", AdminRolePermissionMapper.class),
            new Mapping(AdminGovernanceResource.class, "admin_governance_resource", AdminGovernanceResourceMapper.class),
            new Mapping(AdminGovernanceChange.class, "admin_governance_change", AdminGovernanceChangeMapper.class),
            new Mapping(AdminGovernanceRelease.class, "admin_governance_release", AdminGovernanceReleaseMapper.class),
            new Mapping(AdminGovernanceSnapshot.class, "admin_governance_snapshot", AdminGovernanceSnapshotMapper.class),
            new Mapping(AdminGovernanceAudit.class, "admin_governance_audit", AdminGovernanceAuditMapper.class)
    );

    @Test
    void adminEntitiesShouldBindCurrentTableNames() {
        MAPPINGS.forEach(mapping -> {
            TableName tableName = mapping.entityClass().getAnnotation(TableName.class);
            assertThat(tableName).as(mapping.entityClass().getSimpleName()).isNotNull();
            assertThat(tableName.value()).isEqualTo(mapping.tableName());
        });
    }

    @Test
    void adminEntitiesShouldReuseCommonEntityColumns() throws NoSuchFieldException {
        assertThat(CommonEntity.class.getDeclaredField("id").getAnnotation(TableId.class)).isNotNull();
        assertThat(CommonEntity.class.getDeclaredField("deleted").getAnnotation(TableLogic.class)).isNotNull();
        assertThat(CommonEntity.class.getDeclaredField("version").getAnnotation(Version.class)).isNotNull();

        MAPPINGS.forEach(mapping -> assertThat(CommonEntity.class)
                .as(mapping.entityClass().getSimpleName())
                .isAssignableFrom(mapping.entityClass()));
    }

    @Test
    void adminMappersShouldUseExpectedEntityGeneric() {
        MAPPINGS.forEach(mapping -> {
            assertThat(mapping.mapperClass().getAnnotation(Mapper.class)).isNotNull();
            assertThat(resolveBasePlusMapperEntity(mapping.mapperClass()))
                    .as(mapping.mapperClass().getSimpleName())
                    .isEqualTo(mapping.entityClass());
        });
    }

    @Test
    void mapperXmlShouldUseMapperNamespace() throws IOException {
        for (Mapping mapping : MAPPINGS) {
            String resourcePath = "/mapper/" + mapping.mapperClass().getSimpleName() + ".xml";
            byte[] xml = getClass().getResourceAsStream(resourcePath).readAllBytes();

            assertThat(new String(xml, StandardCharsets.UTF_8))
                    .contains("namespace=\"" + mapping.mapperClass().getName() + "\"");
        }
    }

    private Class<?> resolveBasePlusMapperEntity(Class<?> mapperClass) {
        for (Type type : mapperClass.getGenericInterfaces()) {
            if (type instanceof ParameterizedType parameterizedType
                    && parameterizedType.getRawType().equals(BasePlusMapper.class)) {
                return (Class<?>) parameterizedType.getActualTypeArguments()[0];
            }
        }
        throw new IllegalStateException("Mapper 未继承 BasePlusMapper：" + mapperClass.getName());
    }

    private record Mapping(
            Class<? extends CommonEntity> entityClass,
            String tableName,
            Class<?> mapperClass
    ) {
    }
}
