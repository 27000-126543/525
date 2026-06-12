package com.ecommerce.platform.iam.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.platform.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_post")
public class SysPost extends BaseEntity {

    private Long id;
    private String postCode;
    private String postName;
    private Integer sortOrder;
    private Integer status;
    private String remark;
}
