package com.ecommerce.platform.iam.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.platform.common.context.TenantContext;
import com.ecommerce.platform.common.context.UserContext;
import com.ecommerce.platform.common.exception.BusinessException;
import com.ecommerce.platform.common.result.ResultCode;
import com.ecommerce.platform.common.util.RedisUtil;
import com.ecommerce.platform.iam.dto.LoginDTO;
import com.ecommerce.platform.iam.entity.*;
import com.ecommerce.platform.iam.mapper.*;
import com.ecommerce.platform.iam.service.AuthService;
import com.ecommerce.platform.iam.vo.LoginVO;
import com.ecommerce.platform.iam.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final SysTenantMapper tenantMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysOrgMapper orgMapper;
    private final SysPostMapper postMapper;
    private final RedisUtil redisUtil;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        SysTenant tenant = validateTenant(loginDTO.getTenantCode());
        TenantContext.setTenantId(tenant.getId());

        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, loginDTO.getUsername())
                .eq(SysUser::getDeleted, 0));

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        if (!BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }

        List<SysRole> roles = roleMapper.selectRolesByUserId(user.getId());
        List<String> roleCodes = roles.stream().map(SysRole::getRoleCode).toList();
        List<String> perms = menuMapper.selectPermsByUserId(user.getId());

        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();

        UserContext userContext = UserContext.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .tenantId(tenant.getId())
                .tenantCode(tenant.getTenantCode())
                .orgId(user.getOrgId())
                .roleCodes(String.join(",", roleCodes))
                .token(token)
                .build();
        UserContext.set(userContext);

        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        UserInfoVO userInfo = buildUserInfo(user, tenant, roles, perms);

        return LoginVO.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(StpUtil.getTokenTimeout())
                .userInfo(userInfo)
                .build();
    }

    @Override
    public void logout() {
        StpUtil.logout();
        UserContext.remove();
        TenantContext.remove();
    }

    @Override
    public UserInfoVO getUserInfo() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.USER_NOT_LOGIN);
        }

        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        SysTenant tenant = tenantMapper.selectById(UserContext.getTenantId());
        List<SysRole> roles = roleMapper.selectRolesByUserId(userId);
        List<String> perms = menuMapper.selectPermsByUserId(userId);

        return buildUserInfo(user, tenant, roles, perms);
    }

    @Override
    public LoginVO refreshToken(String refreshToken) {
        StpUtil.checkLogin();
        String newToken = StpUtil.getTokenValue();
        UserInfoVO userInfo = getUserInfo();

        return LoginVO.builder()
                .token(newToken)
                .tokenType("Bearer")
                .expiresIn(StpUtil.getTokenTimeout())
                .userInfo(userInfo)
                .build();
    }

    private SysTenant validateTenant(String tenantCode) {
        if (StrUtil.isBlank(tenantCode)) {
            throw new BusinessException("租户编码不能为空");
        }

        SysTenant tenant = tenantMapper.selectOne(new LambdaQueryWrapper<SysTenant>()
                .eq(SysTenant::getTenantCode, tenantCode)
                .eq(SysTenant::getDeleted, 0));

        if (tenant == null) {
            throw new BusinessException(ResultCode.TENANT_NOT_FOUND);
        }
        if (tenant.getStatus() != null && tenant.getStatus() == 0) {
            throw new BusinessException(ResultCode.TENANT_DISABLED);
        }

        return tenant;
    }

    private UserInfoVO buildUserInfo(SysUser user, SysTenant tenant, List<SysRole> roles, List<String> perms) {
        String orgName = null;
        String postName = null;

        if (user.getOrgId() != null) {
            SysOrg org = orgMapper.selectById(user.getOrgId());
            if (org != null) {
                orgName = org.getOrgName();
            }
        }
        if (user.getPostId() != null) {
            SysPost post = postMapper.selectById(user.getPostId());
            if (post != null) {
                postName = post.getPostName();
            }
        }

        return UserInfoVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .tenantId(tenant.getId())
                .tenantName(tenant.getTenantName())
                .orgId(user.getOrgId())
                .orgName(orgName)
                .postId(user.getPostId())
                .postName(postName)
                .roles(roles.stream().map(SysRole::getRoleCode).collect(Collectors.toList()))
                .perms(perms)
                .build();
    }
}
