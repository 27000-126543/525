package com.ecommerce.platform.iam.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.ecommerce.platform.common.context.TenantContext;
import com.ecommerce.platform.common.context.UserContext;
import com.ecommerce.platform.common.exception.BusinessException;
import com.ecommerce.platform.common.result.ResultCode;
import com.ecommerce.platform.common.util.RedisUtil;
import com.ecommerce.platform.iam.entity.SysUser;
import com.ecommerce.platform.iam.mapper.SysUserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final SysUserMapper userMapper;
    private final RedisUtil redisUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("Authorization");
        if (StrUtil.isBlank(token)) {
            token = request.getParameter("token");
        }

        String tenantHeader = request.getHeader("X-Tenant-Id");
        if (StrUtil.isBlank(tenantHeader)) {
            tenantHeader = request.getHeader("tenantId");
        }

        if (StrUtil.isBlank(token)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        try {
            StpUtil.checkLogin();
            Long userId = StpUtil.getLoginIdAsLong();

            SysUser user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException(ResultCode.USER_NOT_FOUND);
            }
            if (user.getStatus() != null && user.getStatus() == 0) {
                throw new BusinessException(ResultCode.USER_DISABLED);
            }

            Long tenantId = user.getTenantId();
            if (StrUtil.isNotBlank(tenantHeader)) {
                tenantId = Long.parseLong(tenantHeader);
            }

            TenantContext.setTenantId(tenantId);

            UserContext userContext = UserContext.builder()
                    .userId(userId)
                    .username(user.getUsername())
                    .tenantId(tenantId)
                    .orgId(user.getOrgId())
                    .token(token)
                    .ip(getClientIp(request))
                    .userAgent(request.getHeader("User-Agent"))
                    .build();
            UserContext.set(userContext);

            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Token验证失败: {}", e.getMessage());
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.remove();
        TenantContext.remove();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
