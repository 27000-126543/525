package com.ecommerce.platform.iam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.platform.iam.entity.SysUserRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId} AND tenant_id = #{tenantId}")
    int deleteByUserId(@Param("userId") Long userId, @Param("tenantId") Long tenantId);

    int batchInsert(@Param("list") List<SysUserRole> list);
}
