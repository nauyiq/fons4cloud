package com.fons.cloud.admin.infrastructure.limiter;

import com.fons.cloud.admin.api.enums.GovernanceDomain;
import com.fons.cloud.admin.domain.adapter.GovernanceResourceReadAdapter;
import com.fons.cloud.admin.domain.adapter.GovernanceTargetAdapter;
import com.fons.cloud.admin.domain.codec.TrafficIpConfigCodec;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/** limiter 人工 IP 名单权威当前态只读适配器。 */
@Component
public class TrafficIpReadAdapter implements GovernanceResourceReadAdapter {
    private final TrafficIpGovernanceAdapter targetAdapter;
    private final TrafficIpConfigCodec codec;
    public TrafficIpReadAdapter(TrafficIpGovernanceAdapter targetAdapter, TrafficIpConfigCodec codec) {
        this.targetAdapter = targetAdapter; this.codec = codec;
    }
    @Override public GovernanceDomain domain() { return GovernanceDomain.TRAFFIC; }
    @Override public Set<String> resourceTypes() { return Set.of("IP_LIST"); }
    @Override public ReadPage list(ReadQuery query) {
        ReadResource resource = current();
        boolean matched = query.keyword() == null || query.keyword().isBlank()
                || resource.displayName().toLowerCase().contains(query.keyword().toLowerCase());
        return new ReadPage(matched && query.offset() == 0 ? List.of(resource) : List.of(), matched ? 1 : 0);
    }
    @Override public ReadResource detail(String resourceType, String resourceKey) {
        return "manual-ip-list".equals(resourceKey) ? current() : null;
    }
    private ReadResource current() {
        GovernanceTargetAdapter.CurrentConfig current = targetAdapter.loadCurrent(
                new GovernanceTargetAdapter.ResourceRef(domain(), "IP_LIST", "manual-ip-list", null));
        return new ReadResource("IP_LIST", "manual-ip-list", "人工 IP 黑白名单", current.targetRef(),
                current.contentHash(), codec.normalize(current.content()).content(), "ACTIVE",
                Set.of("EDIT", "PUBLISH", "ROLLBACK"));
    }
}
