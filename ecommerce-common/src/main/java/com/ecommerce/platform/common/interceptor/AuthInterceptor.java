package com.ecommerce.platform.common.interceptor;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.ecommerce.platform.common.context.TenantContext;
import com.ecommerce.platform.common.context.UserContext;
import com.ecommerce.platform.common.exception.BusinessException;
import com.ecommerce.platform.common.result.ResultCode;
import com.ecommerce.platform.common.util.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({RedisUtil.class, HttpServletRequest.class})
public class AuthInterceptor implements HandlerInterceptor {

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
            String tokenKey = "auth:token:" + token;
            Object tokenObj = redisUtil.get(tokenKey);
            if (tokenObj == null) {
                throw new BusinessException(ResultCode.UNAUTHORIZED);
            }

            Map<String, Object> tokenInfo;
            if (tokenObj instanceof String) {
                tokenInfo = JSON.parseObject((String) tokenObj, new TypeReference<Map<String, Object>>() {});
            } else if (tokenObj instanceof Map) {
                tokenInfo = (Map<String, Object>) tokenObj;
            } else {
                throw new BusinessException(ResultCode.UNAUTHORIZED);
            }

            Long userId = Long.valueOf(tokenInfo.get("userId").toString());
            String username = (String) tokenInfo.get("username");
            Long tenantId = tokenInfo.get("tenantId") != null
                    ? Long.valueOf(tokenInfo.get("tenantId").toString())
                    : null;
            Long orgId = tokenInfo.get("orgId") != null
                    ? Long.valueOf(tokenInfo.get("orgId").toString())
                    : null;
            Object roleCodesObj = tokenInfo.get("roleCodes");
            String roleCodes = "";
            if (roleCodesObj != null) {
                if (roleCodesObj instanceof List) {
                    roleCodes = String.join(",", (List<String>) roleCodesObj);
                } else {
                    roleCodes = roleCodesObj.toString();
                }
            }

            if (StrUtil.isNotBlank(tenantHeader)) {
                tenantId = Long.parseLong(tenantHeader);
            }

            TenantContext.setTenantId(tenantId);

            UserContext userContext = UserContext.builder()
                    .userId(userId)
                    .username(username)
                    .tenantId(tenantId)
                    .orgId(orgId)
                    .roleCodes(roleCodes)
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
