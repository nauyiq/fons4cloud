package com.fons.cloud.auth.request;

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

    /**
     * 用户名。
     */
    private String username;

    /**
     * 手机号。
     */
    private String phone;

    /**
     * 邮箱。
     */
    private String email;

}
