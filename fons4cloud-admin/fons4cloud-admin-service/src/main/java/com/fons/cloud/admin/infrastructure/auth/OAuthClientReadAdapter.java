package com.fons.cloud.admin.infrastructure.auth;

import com.fons.cloud.admin.api.constants.AdminResultCode;
import com.fons.cloud.admin.api.enums.GovernanceDomain;
import com.fons.cloud.admin.domain.adapter.GovernanceResourceReadAdapter;
import com.fons.cloud.admin.domain.codec.OAuthClientConfigCodec;
import com.fons.cloud.auth.request.OauthClientQueryRequest;
import com.fons.cloud.auth.response.OauthClientInfo;
import com.fons.cloud.common.base.exception.BizException;
import com.fons.cloud.common.result.R;
import com.fons.cloud.util.JsonUtil;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/** 通过 auth-service RPC 查询不含 Secret 的 OAuth Client 当前态。 */
@Component
public class OAuthClientReadAdapter implements GovernanceResourceReadAdapter {
    private final AdminOauthClientManagementClient client;
    private final OAuthClientConfigCodec codec;
    public OAuthClientReadAdapter(AdminOauthClientManagementClient client, OAuthClientConfigCodec codec) {
        this.client = client; this.codec = codec;
    }
    @Override public GovernanceDomain domain() { return GovernanceDomain.CLIENTS; }
    @Override public Set<String> resourceTypes() { return Set.of("OAUTH_CLIENT"); }
    @Override public ReadPage list(ReadQuery query) {
        R<List<OauthClientInfo>> result = client.queryList(OauthClientQueryRequest.builder()
                .clientId(query.keyword()).build());
        if (result == null || !result.isSuccess() || result.getData() == null) {
            throw new BizException(AdminResultCode.ADMIN_AUTH_RPC_FAILED);
        }
        List<ReadResource> resources = result.getData().stream().filter(info -> !Boolean.TRUE.equals(info.getDeleted()))
                .sorted(Comparator.comparing(OauthClientInfo::getClientId)).map(this::resource).toList();
        int from = Math.min(query.offset(), resources.size()); int to = Math.min(from + query.limit(), resources.size());
        return new ReadPage(resources.subList(from, to), resources.size());
    }
    @Override public ReadResource detail(String resourceType, String resourceKey) {
        R<OauthClientInfo> result = client.query(OauthClientQueryRequest.builder().clientId(resourceKey).build());
        return result == null || !result.isSuccess() || result.getData() == null ? null : resource(result.getData());
    }
    private ReadResource resource(OauthClientInfo info) {
        String safeContent = JsonUtil.toJson(info);
        String hash = codec.normalize(safeContent).contentHash();
        return new ReadResource("OAUTH_CLIENT", info.getClientId(), info.getClientId(), info.getClientId(), hash,
                safeContent, Boolean.TRUE.equals(info.getStatus()) ? "ACTIVE" : "DISABLED",
                Set.of("EDIT", "STATUS", "ROTATE_SECRET"));
    }
}
