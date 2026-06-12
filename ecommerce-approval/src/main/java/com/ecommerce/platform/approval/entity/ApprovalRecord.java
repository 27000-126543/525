package com.ecommerce.platform.approval.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.platform.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("approval_record")
public class ApprovalRecord extends BaseEntity {

    private Long id;
    private Long instanceId;
    private Long nodeId;
    private String nodeName;
    private Integer nodeOrder;
    private Integer approvalType;
    private Long approverId;
    private String approverName;
    private Long approverOrgId;
    private String approverOrgName;
    private Integer approvalResult;
    private String approvalOpinion;
    private LocalDateTime approvalTime;
    private Integer status;
    private String remark;
    private String signImage;
    private String ip;
    private String userAgent;
}
