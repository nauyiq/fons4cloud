package com.fons.cloud.account.request;

import com.fons.cloud.common.request.BaseRequest;
import lombok.*;

import java.io.Serial;

/**
 * @author qiyuan.hong
 * @version 1.0
 * @date 2024/7/11
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AccountQueryParams extends BaseRequest {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String phone;
    private String email;

}
