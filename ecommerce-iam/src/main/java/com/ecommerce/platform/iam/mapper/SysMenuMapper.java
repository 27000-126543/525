package com.ecommerce.platform.iam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.platform.iam.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);

    List<SysMenu> selectMenusByRoleId(@Param("roleId") Long roleId);

    List<String> selectPermsByUserId(@Param("userId") Long userId);
}
