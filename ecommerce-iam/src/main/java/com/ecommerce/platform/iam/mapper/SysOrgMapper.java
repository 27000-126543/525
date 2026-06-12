package com.ecommerce.platform.iam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.platform.iam.entity.SysOrg;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysOrgMapper extends BaseMapper<SysOrg> {

    List<SysOrg> selectOrgList(@Param("org") SysOrg org);

    List<Long> selectOrgIdsByRoleId(@Param("roleId") Long roleId);
}
