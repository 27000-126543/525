package com.ecommerce.platform.iam.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.platform.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_org")
public class SysOrg extends BaseEntity {

    private Long id;
    private String orgCode;
    private String orgName;
    private Long parentId;
    private String ancestors;
    private Integer orgType;
    private Integer sortOrder;
    private String leader;
    private String phone;
    private String email;
    private Integer status;
    private String remark;
}
