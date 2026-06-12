package com.ecommerce.platform.iam.service.impl;

import com.ecommerce.platform.common.constant.RedisConstants;
import com.ecommerce.platform.common.util.RedisUtil;
import com.ecommerce.platform.iam.entity.SysRole;
import com.ecommerce.platform.iam.mapper.SysMenuMapper;
import com.ecommerce.platform.iam.mapper.SysRoleMapper;
import com.ecommerce.platform.iam.service.SysPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl implements SysPermissionService {

    private final SysMenuMapper menuMapper;
    private final SysRoleMapper roleMapper;
    private final RedisUtil redisUtil;

    @Override
    public List<String> getPermsByUserId(Long userId) {
        String cacheKey = RedisConstants.USER_PERM_PREFIX + userId;

        @SuppressWarnings("unchecked")
        List<String> perms = (List<String>) redisUtil.get(cacheKey);
        if (perms != null && !perms.isEmpty()) {
            return perms;
        }

        perms = menuMapper.selectPermsByUserId(userId);
        if (perms != null && !perms.isEmpty()) {
            redisUtil.set(cacheKey, perms, RedisConstants.CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        }

        return perms;
    }

    @Override
    public List<String> getRoleCodesByUserId(Long userId) {
        List<SysRole> roles = roleMapper.selectRolesByUserId(userId);
        return roles.stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasPerm(Long userId, String perm) {
        List<String> perms = getPermsByUserId(userId);
        if (perms == null || perms.isEmpty()) {
            return false;
        }
        return perms.contains(perm) || perms.contains("*");
    }

    @Override
    public boolean hasRole(Long userId, String roleCode) {
        List<String> roles = getRoleCodesByUserId(userId);
        return roles.contains(roleCode);
    }

    @Override
    public void refreshUserPermCache(Long userId) {
        String cacheKey = RedisConstants.USER_PERM_PREFIX + userId;
        redisUtil.delete(cacheKey);
    }
}
