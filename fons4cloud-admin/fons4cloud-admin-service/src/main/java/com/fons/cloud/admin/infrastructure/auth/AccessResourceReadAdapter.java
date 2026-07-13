package com.fons.cloud.admin.infrastructure.auth;

import com.fons.cloud.admin.api.enums.GovernanceDomain;
import com.fons.cloud.admin.domain.adapter.GovernanceResourceReadAdapter;
import com.fons.cloud.admin.domain.adapter.GovernanceTargetAdapter;
import com.fons.cloud.admin.domain.codec.AccessResourceConfigCodec;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/** auth-core 授权资源集合的权威只读视图。 */
@Component
public class AccessResourceReadAdapter implements GovernanceResourceReadAdapter {
    private final AccessResourceGovernanceAdapter targetAdapter;
    private final AccessResourceConfigCodec codec;
    public AccessResourceReadAdapter(AccessResourceGovernanceAdapter targetAdapter, AccessResourceConfigCodec codec) {
        this.targetAdapter = targetAdapter; this.codec = codec;
    }
    @Override public GovernanceDomain domain() { return GovernanceDomain.ACCESS; }
    @Override public Set<String> resourceTypes() { return Set.of("AUTH_RESOURCE"); }
    @Override public ReadPage list(ReadQuery query) {
        ReadResource resource = current();
        boolean matched = query.keyword() == null || query.keyword().isBlank()
                || resource.displayName().contains(query.keyword());
        return new ReadPage(matched && query.offset() == 0 ? List.of(resource) : List.of(), matched ? 1 : 0);
    }
    @Override public ReadResource detail(String resourceType, String resourceKey) {
        return "authorization-resource".equals(resourceKey) ? current() : null;
    }
    private ReadResource current() {
        GovernanceTargetAdapter.CurrentConfig current = targetAdapter.loadCurrent(
                new GovernanceTargetAdapter.ResourceRef(domain(), "AUTH_RESOURCE", "authorization-resource", null));
        return new ReadResource("AUTH_RESOURCE", "authorization-resource", "授权资源与 Token 规则",
                current.targetRef(), current.contentHash(), codec.normalize(current.content()).content(), "ACTIVE",
                Set.of("EDIT", "PUBLISH", "ROLLBACK"));
    }
}
