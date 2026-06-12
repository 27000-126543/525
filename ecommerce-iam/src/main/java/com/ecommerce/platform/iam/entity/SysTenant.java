package com.ecommerce.platform.iam.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.platform.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_tenant")
public class SysTenant extends BaseEntity {

    private Long id;
    private String tenantCode;
    private String tenantName;
    private Integer status;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String address;
    private String logo;
    private LocalDateTime expireTime;
    private String description;
    private Integer sortOrder;
}
