package com.ecommerce.platform.approval.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.platform.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("approval_cc")
public class ApprovalCc extends BaseEntity {

    private Long id;
    private Long instanceId;
    private Long nodeId;
    private String nodeName;
    private Long userId;
    private String userName;
    private Integer readStatus;
    private LocalDateTime readTime;
    private String remark;
}
