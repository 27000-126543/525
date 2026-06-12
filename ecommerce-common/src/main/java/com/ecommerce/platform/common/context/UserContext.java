package com.ecommerce.platform.common.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserContext implements Serializable {

    private Long userId;
    private String username;
    private Long tenantId;
    private String tenantCode;
    private Long orgId;
    private String roleCodes;
    private String token;
    private String ip;
    private String userAgent;

    private static final TransmittableThreadLocal<UserContext> HOLDER = new TransmittableThreadLocal<>();

    public static void set(UserContext context) {
        HOLDER.set(context);
    }

    public static UserContext get() {
        return HOLDER.get();
    }

    public static void remove() {
        HOLDER.remove();
    }

    public static Long getUserId() {
        UserContext context = HOLDER.get();
        return context != null ? context.getUserId() : null;
    }

    public static Long getTenantId() {
        UserContext context = HOLDER.get();
        return context != null ? context.getTenantId() : null;
    }

    public static String getTenantCode() {
        UserContext context = HOLDER.get();
        return context != null ? context.getTenantCode() : null;
    }

    public static Long getOrgId() {
        UserContext context = HOLDER.get();
        return context != null ? context.getOrgId() : null;
    }

    public static String getUsername() {
        UserContext context = HOLDER.get();
        return context != null ? context.getUsername() : null;
    }
}
