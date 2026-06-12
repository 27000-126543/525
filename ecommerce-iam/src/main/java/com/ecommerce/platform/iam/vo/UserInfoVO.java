package com.ecommerce.platform.iam.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoVO implements Serializable {

    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private String email;
    private String mobile;
    private Long tenantId;
    private String tenantName;
    private Long orgId;
    private String orgName;
    private Long postId;
    private String postName;
    private List<String> roles;
    private List<String> perms;
}
