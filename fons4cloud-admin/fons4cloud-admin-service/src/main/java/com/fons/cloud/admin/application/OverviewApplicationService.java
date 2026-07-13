package com.fons.cloud.admin.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fons.cloud.admin.api.constants.AdminPermissionCodes;
import com.fons.cloud.admin.api.constants.AdminResultCode;
import com.fons.cloud.admin.api.enums.GovernanceChangeStatus;
import com.fons.cloud.admin.api.enums.GovernanceReleaseStatus;
import com.fons.cloud.admin.domain.entity.AdminGovernanceChange;
import com.fons.cloud.admin.domain.entity.AdminGovernanceRelease;
import com.fons.cloud.admin.domain.mapper.AdminGovernanceChangeMapper;
import com.fons.cloud.admin.domain.mapper.AdminGovernanceReleaseMapper;
import com.fons.cloud.admin.infrastructure.discovery.ServiceDiscoveryReadAdapter;
import com.fons.cloud.admin.infrastructure.security.AdminAuthorizationService;
import com.fons.cloud.admin.interfaces.rest.api.model.AdminSessionContextResponse;
import com.fons.cloud.admin.interfaces.rest.api.model.OverviewResponse;
import com.fons.cloud.auth.api.AuthUser;
import com.fons.cloud.common.result.R;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 聚合当前管理员有权查看的高优先级行动项。 */
@Service
public class OverviewApplicationService {

    private final AdminAuthorizationService authorizationService;
    private final AdminGovernanceChangeMapper changeMapper;
    private final AdminGovernanceReleaseMapper releaseMapper;
    private final ServiceDiscoveryReadAdapter discoveryReadAdapter;

    public OverviewApplicationService(AdminAuthorizationService authorizationService,
                                      AdminGovernanceChangeMapper changeMapper,
                                      AdminGovernanceReleaseMapper releaseMapper,
                                      ServiceDiscoveryReadAdapter discoveryReadAdapter) {
        this.authorizationService = authorizationService;
        this.changeMapper = changeMapper;
        this.releaseMapper = releaseMapper;
        this.discoveryReadAdapter = discoveryReadAdapter;
    }

    public R<OverviewResponse> overview(AuthUser authUser) {
        AdminAuthorizationService.AdminSessionAccess access = authorizationService.loadSessionAccess(authUser);
        if (access == null || !access.active()) {
            return R.failed(access == null ? AdminResultCode.ADMIN_USER_NOT_BOUND : AdminResultCode.ADMIN_USER_DISABLED);
        }

        Set<String> permissions = access.permissionCodes();
        List<OverviewResponse.ActionItem> actions = new ArrayList<>();
        Map<String, Long> statistics = new LinkedHashMap<>();
        Map<String, AdminSessionContextResponse.DependencyState> dependencies = new LinkedHashMap<>();

        if (permissions.contains(AdminPermissionCodes.CHANGES_VIEW)) {
            long failed = countChanges(GovernanceChangeStatus.PUBLISH_FAILED, GovernanceChangeStatus.ROLLBACK_FAILED);
            long drifted = countChanges(GovernanceChangeStatus.DRIFT_DETECTED);
            long pending = countChanges(GovernanceChangeStatus.DRAFT, GovernanceChangeStatus.VALIDATION_FAILED,
                    GovernanceChangeStatus.VALIDATED);
            long uncertain = countReleases(GovernanceReleaseStatus.PENDING_CONFIRM);
            addAction(actions, "FAILED_RELEASE", "ERROR", failed, "失败发布", "/changes");
            addAction(actions, "PENDING_CONFIRM", "WARNING", uncertain, "待确认执行", "/changes");
            addAction(actions, "DRIFT", "WARNING", drifted, "配置漂移", "/changes");
            addAction(actions, "PENDING_CHANGE", "INFO", pending, "待处理变更", "/changes");
            statistics.put("changes", changeMapper.selectCount(null));
        }

        if (permissions.contains(AdminPermissionCodes.SERVICES_VIEW)) {
            try {
                long serviceCount = discoveryReadAdapter.listServices().size();
                statistics.put("services", serviceCount);
                dependencies.put("discovery", AdminSessionContextResponse.DependencyState.UP);
            } catch (RuntimeException ex) {
                dependencies.put("discovery", AdminSessionContextResponse.DependencyState.DOWN);
            }
        } else {
            dependencies.put("discovery", AdminSessionContextResponse.DependencyState.UNKNOWN);
        }
        dependencies.put("nacos", AdminSessionContextResponse.DependencyState.UNKNOWN);
        dependencies.put("redis", AdminSessionContextResponse.DependencyState.UNKNOWN);
        dependencies.put("auth", AdminSessionContextResponse.DependencyState.UP);

        return R.ok(new OverviewResponse(List.copyOf(actions), Map.copyOf(statistics), Map.copyOf(dependencies)));
    }

    private long countChanges(GovernanceChangeStatus... statuses) {
        return changeMapper.selectCount(new LambdaQueryWrapper<AdminGovernanceChange>()
                .in(AdminGovernanceChange::getStatus, (Object[]) statusesToNames(statuses)));
    }

    private long countReleases(GovernanceReleaseStatus... statuses) {
        return releaseMapper.selectCount(new LambdaQueryWrapper<AdminGovernanceRelease>()
                .in(AdminGovernanceRelease::getStatus, (Object[]) statusesToNames(statuses)));
    }

    private String[] statusesToNames(Enum<?>[] statuses) {
        return java.util.Arrays.stream(statuses).map(Enum::name).toArray(String[]::new);
    }

    private void addAction(List<OverviewResponse.ActionItem> actions, String type, String severity, long count,
                           String title, String route) {
        if (count > 0) {
            actions.add(new OverviewResponse.ActionItem(type, severity, count, title, route));
        }
    }
}
