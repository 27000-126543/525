package com.ecommerce.platform.approval.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.platform.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("approval_instance")
public class ApprovalInstance extends BaseEntity {

    private Long id;
    private String instanceNo;
    private Long processId;
    private String processName;
    private Integer processType;
    private Long businessId;
    private String businessType;
    private String businessNo;
    private String title;
    private BigDecimal amount;
    private String formData;
    private Long applicantId;
    private String applicantName;
    private Long applicantOrgId;
    private String applicantOrgName;
    private Integer status;
    private Long currentNodeId;
    private String currentNodeName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String cancelReason;
    private String remark;

    @TableField(exist = false)
    private List<ApprovalRecord> records;

    @TableField(exist = false)
    private List<ApprovalNode> nodes;
}
