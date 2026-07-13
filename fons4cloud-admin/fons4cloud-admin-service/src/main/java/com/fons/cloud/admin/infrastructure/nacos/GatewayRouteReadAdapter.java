package com.fons.cloud.admin.infrastructure.nacos;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fons.cloud.admin.api.enums.GovernanceDomain;
import com.fons.cloud.admin.domain.adapter.GovernanceResourceReadAdapter;
import com.fons.cloud.admin.domain.adapter.GovernanceTargetAdapter;
import com.fons.cloud.admin.domain.codec.GatewayRouteConfigCodec;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/** 从 Nacos 权威路由数组构建可搜索的路由只读视图。 */
@Component
public class GatewayRouteReadAdapter implements GovernanceResourceReadAdapter {

    private final GatewayRouteGovernanceAdapter targetAdapter;
    private final GatewayRouteConfigCodec codec;
    private final ObjectMapper objectMapper;

    public GatewayRouteReadAdapter(GatewayRouteGovernanceAdapter targetAdapter, GatewayRouteConfigCodec codec,
                                   ObjectMapper objectMapper) {
        this.targetAdapter = targetAdapter;
        this.codec = codec;
        this.objectMapper = objectMapper;
    }

    @Override public GovernanceDomain domain() { return GovernanceDomain.GATEWAY; }
    @Override public Set<String> resourceTypes() { return Set.of("ROUTE"); }

    @Override
    public ReadPage list(ReadQuery query) {
        GovernanceTargetAdapter.CurrentConfig current = loadCurrent();
        List<ReadResource> resources = new ArrayList<>();
        try {
            for (JsonNode route : objectMapper.readTree(current.content())) {
                String routeId = route.path("id").asText();
                if (matches(query.keyword(), routeId, route.path("uri").asText())) {
                    resources.add(new ReadResource("ROUTE", routeId, routeId, current.targetRef(),
                            current.contentHash(), route.toString(), "ACTIVE", Set.of("EDIT", "PUBLISH", "ROLLBACK")));
                }
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("gateway route config cannot be parsed", ex);
        }
        resources.sort(Comparator.comparing(ReadResource::resourceKey));
        return page(resources, query.offset(), query.limit());
    }

    @Override
    public ReadResource detail(String resourceType, String resourceKey) {
        GovernanceTargetAdapter.CurrentConfig current = loadCurrent();
        try {
            for (JsonNode route : objectMapper.readTree(current.content())) {
                if (resourceKey.equals(route.path("id").asText())) {
                    return new ReadResource("ROUTE", resourceKey, resourceKey, current.targetRef(),
                            current.contentHash(), codec.normalize(current.content()).content(), "ACTIVE",
                            Set.of("EDIT", "PUBLISH", "ROLLBACK"));
                }
            }
            return null;
        } catch (Exception ex) {
            throw new IllegalArgumentException("gateway route config cannot be parsed", ex);
        }
    }

    private GovernanceTargetAdapter.CurrentConfig loadCurrent() {
        return targetAdapter.loadCurrent(new GovernanceTargetAdapter.ResourceRef(domain(), "ROUTE", "routes", null));
    }
    private boolean matches(String keyword, String... values) {
        if (keyword == null || keyword.isBlank()) return true;
        String expected = keyword.toLowerCase();
        return java.util.Arrays.stream(values).anyMatch(value -> value != null && value.toLowerCase().contains(expected));
    }
    private ReadPage page(List<ReadResource> items, int offset, int limit) {
        int from = Math.min(offset, items.size()); int to = Math.min(from + limit, items.size());
        return new ReadPage(items.subList(from, to), items.size());
    }
}
