package com.ecommerce.platform.iam.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.platform.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    private Long id;
    private String menuName;
    private Long parentId;
    private Integer sortOrder;
    private String path;
    private String component;
    private String perms;
    private Integer menuType;
    private String icon;
    private Integer visible;
    private Integer status;
    private String remark;

    @TableField(exist = false)
    private List<SysMenu> children;
}
