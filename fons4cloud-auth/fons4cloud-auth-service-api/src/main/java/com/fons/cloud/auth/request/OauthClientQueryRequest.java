package com.fons.cloud.auth.request;

import com.fons.cloud.common.request.BaseRequest;
import lombok.*;

/**
 * OAuth Client 查询请求。
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OauthClientQueryRequest extends BaseRequest {

    /**
     * 客户端ID；单查时必填，列表查询时作为可选过滤条件。
     */
    private String clientId;

    /**
     * 客户端启停状态过滤条件；为空表示不过滤。
     */
    private Boolean status;
}
