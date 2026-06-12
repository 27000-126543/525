package com.ecommerce.platform.approval.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.platform.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("approval_node")
public class ApprovalNode extends BaseEntity {

    private Long id;
    private Long processId;
    private String nodeCode;
    private String nodeName;
    private Integer nodeType;
    private Integer nodeOrder;
    private Integer approvalType;
    private String approvalUsers;
    private String approvalRoles;
    private String approvalOrgs;
    private Integer approvalMode;
    private Integer passCondition;
    private Integer passCount;
    private Integer passPercent;
    private String formPermissions;
    private String fieldPermissions;
    private Long nextNodeId;
    private Long prevNodeId;
    private String remark;

    @TableField(exist = false)
    private List<ApprovalNode> childNodes;
}
