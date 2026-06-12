package com.ecommerce.platform.approval.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.platform.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("approval_process")
public class ApprovalProcess extends BaseEntity {

    private Long id;
    private String processCode;
    private String processName;
    private Integer processType;
    private String description;
    private Integer status;
    private Integer version;
    private String formConfig;
    private String notifyConfig;

    @TableField(exist = false)
    private List<ApprovalNode> nodes;
}
