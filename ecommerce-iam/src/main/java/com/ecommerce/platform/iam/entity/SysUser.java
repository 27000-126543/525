package com.ecommerce.platform.iam.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.platform.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private String email;
    private String mobile;
    private Integer gender;
    private Integer status;
    private Long orgId;
    private Long postId;
    private String remark;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;

    @TableField(exist = false)
    private String orgName;

    @TableField(exist = false)
    private String postName;
}
