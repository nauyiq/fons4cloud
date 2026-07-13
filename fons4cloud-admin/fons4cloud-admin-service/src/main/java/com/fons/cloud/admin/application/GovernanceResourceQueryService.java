package com.fons.cloud.admin.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fons.cloud.admin.api.constants.AdminResultCode;
import com.fons.cloud.admin.api.enums.GovernanceDomain;
import com.fons.cloud.admin.domain.adapter.GovernanceResourceReadAdapter;
import com.fons.cloud.admin.domain.entity.AdminGovernanceResource;
import com.fons.cloud.admin.domain.mapper.AdminGovernanceResourceMapper;
import com.fons.cloud.admin.interfaces.rest.api.model.GovernanceResourceDetailResponse;
import com.fons.cloud.admin.interfaces.rest.api.model.PageResponse;
import com.fons.cloud.common.base.exception.BizException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 合并权威目标当前态与 admin 已登记历史，不在读取路径制造持久化副作用。 */
@Service
public class GovernanceResourceQueryService {

    private final List<GovernanceResourceReadAdapter> readAdapters;
    private final AdminGovernanceResourceMapper resourceMapper;

    public GovernanceResourceQueryService(List<GovernanceResourceReadAdapter> readAdapters,
                                          AdminGovernanceResourceMapper resourceMapper) {
        this.readAdapters = List.copyOf(readAdapters);
        this.resourceMapper = resourceMapper;
    }

    public PageResponse<GovernanceResourceDetailResponse> list(GovernanceDomain domain, String resourceType,
                                                                String keyword, int offset, int limit) {
        GovernanceResourceReadAdapter adapter = requiredAdapter(domain, resourceType);
        GovernanceResourceReadAdapter.ReadPage page = adapter.list(
                new GovernanceResourceReadAdapter.ReadQuery(resourceType, keyword, offset, limit));
        Map<String, AdminGovernanceResource> registered = registeredByKey(domain, resourceType,
                page.items().stream().map(GovernanceResourceReadAdapter.ReadResource::resourceKey).toList());
        List<GovernanceResourceDetailResponse> items = page.items().stream()
                .map(resource -> response(domain, resource, registered.get(resource.resourceKey())))
                .toList();
        return new PageResponse<>(items, page.total(), Math.max(0, offset), Math.min(100, Math.max(1, limit)));
    }

    public GovernanceResourceDetailResponse detail(GovernanceDomain domain, String resourceType, String resourceKey) {
        GovernanceResourceReadAdapter.ReadResource current = requiredAdapter(domain, resourceType)
                .detail(resourceType, resourceKey);
        if (current == null) {
            throw new BizException(AdminResultCode.ADMIN_TARGET_UNAVAILABLE);
        }
        AdminGovernanceResource registered = resourceMapper.selectOne(new LambdaQueryWrapper<AdminGovernanceResource>()
                .eq(AdminGovernanceResource::getDomain, domain.getCode())
                .eq(AdminGovernanceResource::getResourceType, resourceType)
                .eq(AdminGovernanceResource::getResourceKey, resourceKey));
        return response(domain, current, registered);
    }

    private GovernanceResourceReadAdapter requiredAdapter(GovernanceDomain domain, String resourceType) {
        return readAdapters.stream()
                .filter(adapter -> adapter.domain() == domain && adapter.resourceTypes().contains(resourceType))
                .findFirst()
                .orElseThrow(() -> new BizException(AdminResultCode.ADMIN_TARGET_UNAVAILABLE));
    }

    private Map<String, AdminGovernanceResource> registeredByKey(GovernanceDomain domain, String resourceType,
                                                                  List<String> resourceKeys) {
        if (resourceKeys.isEmpty()) {
            return Map.of();
        }
        List<AdminGovernanceResource> resources = resourceMapper.selectList(
                new LambdaQueryWrapper<AdminGovernanceResource>()
                        .eq(AdminGovernanceResource::getDomain, domain.getCode())
                        .eq(AdminGovernanceResource::getResourceType, resourceType)
                        .in(AdminGovernanceResource::getResourceKey, resourceKeys));
        Map<String, AdminGovernanceResource> result = new HashMap<>();
        resources.forEach(resource -> result.put(resource.getResourceKey(), resource));
        return result;
    }

    private GovernanceResourceDetailResponse response(GovernanceDomain domain,
                                                       GovernanceResourceReadAdapter.ReadResource current,
                                                       AdminGovernanceResource registered) {
        return new GovernanceResourceDetailResponse(registered == null ? null : registered.getId(), domain.getCode(),
                current.resourceType(), current.resourceKey(), current.displayName(), summarize(current.targetRef()),
                current.currentHash(), current.safeContent(), current.status(), current.supportedActions(),
                registered != null);
    }

    private String summarize(String targetRef) {
        if (targetRef == null || targetRef.isBlank()) {
            return "-";
        }
        if (targetRef.length() <= 10) {
            return targetRef;
        }
        return targetRef.substring(0, 4) + "…" + targetRef.substring(targetRef.length() - 4);
    }
}
