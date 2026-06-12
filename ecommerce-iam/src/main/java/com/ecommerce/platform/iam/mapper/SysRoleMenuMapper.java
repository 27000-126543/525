package com.ecommerce.platform.iam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.platform.iam.entity.SysRoleMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

    int deleteByRoleId(@Param("roleId") Long roleId, @Param("tenantId") Long tenantId);

    int batchInsert(@Param("list") List<SysRoleMenu> list);
}
