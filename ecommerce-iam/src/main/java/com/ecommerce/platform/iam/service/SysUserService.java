package com.ecommerce.platform.iam.service;

import com.ecommerce.platform.common.page.PageQuery;
import com.ecommerce.platform.common.page.PageResult;
import com.ecommerce.platform.iam.entity.SysUser;

import java.util.List;

public interface SysUserService {

    PageResult<SysUser> page(PageQuery pageQuery, SysUser user);

    SysUser getById(Long id);

    void add(SysUser user, List<Long> roleIds);

    void update(SysUser user, List<Long> roleIds);

    void delete(Long id);

    void updateStatus(Long id, Integer status);

    void resetPassword(Long id, String newPassword);

    List<SysUser> listByOrgId(Long orgId);
}
