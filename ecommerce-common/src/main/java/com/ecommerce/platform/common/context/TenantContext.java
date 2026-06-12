package com.ecommerce.platform.common.context;

import com.alibaba.ttl.TransmittableThreadLocal;

public class TenantContext {

    private static final TransmittableThreadLocal<Long> TENANT_ID_HOLDER = new TransmittableThreadLocal<>();

    public static void setTenantId(Long tenantId) {
        TENANT_ID_HOLDER.set(tenantId);
    }

    public static Long getTenantId() {
        return TENANT_ID_HOLDER.get();
    }

    public static void remove() {
        TENANT_ID_HOLDER.remove();
    }
}
