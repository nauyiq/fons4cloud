package com.fons.cloud.auth.request;

import com.fons.cloud.common.request.BaseRequest;
import lombok.*;

/**
 * @author hongqy
 * @date 2025/2/14
 */
@Setter
@Getter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountAuthRequest extends BaseRequest {

    private Long id;
    private String rearName;
    private String idCard;

}
