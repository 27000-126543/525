package com.ecommerce.platform.iam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.platform.common.exception.BusinessException;
import com.ecommerce.platform.common.page.PageQuery;
import com.ecommerce.platform.common.page.PageResult;
import com.ecommerce.platform.common.util.RedisUtil;
import com.ecommerce.platform.iam.entity.SysUser;
import com.ecommerce.platform.iam.entity.SysUserRole;
import com.ecommerce.platform.iam.mapper.SysUserMapper;
import com.ecommerce.platform.iam.mapper.SysUserRoleMapper;
import com.ecommerce.platform.iam.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final RedisUtil redisUtil;

    @Override
    public PageResult<SysUser> page(PageQuery pageQuery, SysUser user) {
        Page<SysUser> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        Page<SysUser> result = userMapper.selectUserPage(page, user);
        return PageResult.of(result);
    }

    @Override
    public SysUser getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(SysUser user, List<Long> roleIds) {
        long count = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, user.getUsername())
                .eq(SysUser::getDeleted, 0));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        userMapper.insert(user);

        if (roleIds != null && !roleIds.isEmpty()) {
            saveUserRoles(user.getId(), roleIds);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysUser user, List<Long> roleIds) {
        SysUser exist = userMapper.selectById(user.getId());
        if (exist == null) {
            throw new BusinessException("用户不存在");
        }

        if (user.getPassword() != null) {
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        }

        userMapper.updateById(user);

        if (roleIds != null) {
            userRoleMapper.deleteByUserId(user.getId(), user.getTenantId());
            if (!roleIds.isEmpty()) {
                saveUserRoles(user.getId(), roleIds);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        userMapper.deleteById(id);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        userMapper.updateById(user);
    }

    @Override
    public List<SysUser> listByOrgId(Long orgId) {
        return userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getOrgId, orgId)
                .eq(SysUser::getStatus, 1)
                .eq(SysUser::getDeleted, 0));
    }

    private void saveUserRoles(Long userId, List<Long> roleIds) {
        List<SysUserRole> list = new ArrayList<>();
        for (Long roleId : roleIds) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            list.add(userRole);
        }
        userRoleMapper.batchInsert(list);
    }
}
