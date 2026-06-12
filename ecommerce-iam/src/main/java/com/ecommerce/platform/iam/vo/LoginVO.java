package com.ecommerce.platform.iam.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO implements Serializable {

    private String token;
    private String tokenType;
    private Long expiresIn;
    private UserInfoVO userInfo;
}
