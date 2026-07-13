package com.fons.cloud.admin.domain.adapter;

import com.fons.cloud.admin.api.enums.GovernanceDomain;

import java.util.List;
import java.util.Set;

/**
 * 治理目标只读端口。
 *
 * <p>列表和详情以外部目标当前态为权威来源；该端口不得创建资源登记、快照或审计记录。</p>
 */
public interface GovernanceResourceReadAdapter {

    GovernanceDomain domain();

    Set<String> resourceTypes();

    ReadPage list(ReadQuery query);

    ReadResource detail(String resourceType, String resourceKey);

    record ReadQuery(String resourceType, String keyword, int offset, int limit) {
        public ReadQuery {
            offset = Math.max(0, offset);
            limit = Math.min(100, Math.max(1, limit));
        }
    }

    record ReadPage(List<ReadResource> items, long total) {
        public ReadPage {
            items = List.copyOf(items);
        }
    }

    record ReadResource(String resourceType, String resourceKey, String displayName, String targetRef,
                        String currentHash, String safeContent, String status, Set<String> supportedActions) {
        public ReadResource {
            supportedActions = Set.copyOf(supportedActions);
        }
    }
}
