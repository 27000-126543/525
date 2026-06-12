package com.ecommerce.platform.common.constant;

public interface RedisConstants {

    String TOKEN_PREFIX = "auth:token:";
    String USER_PERM_PREFIX = "auth:perm:";
    String TENANT_INFO_PREFIX = "tenant:info:";

    String PRODUCT_STOCK_PREFIX = "product:stock:";
    String PRODUCT_INFO_PREFIX = "product:info:";
    String SECKILL_STOCK_PREFIX = "seckill:stock:";
    String SECKILL_USER_PREFIX = "seckill:user:";
    String HOT_PRODUCT_KEY = "product:hot";

    String ORDER_TOKEN_PREFIX = "order:token:";
    String ORDER_LOCK_PREFIX = "order:lock:";

    String RECOMMEND_USER_PREFIX = "recommend:user:";
    String RECOMMEND_HOT_PREFIX = "recommend:hot";

    String RATE_LIMIT_PREFIX = "rate:limit:";

    String AUDIT_LOG_PREFIX = "audit:log:";

    String APPROVAL_PROCESS_PREFIX = "approval:process:";

    long TOKEN_EXPIRE_HOURS = 24;
    long CACHE_EXPIRE_MINUTES = 30;
    long SECKILL_EXPIRE_HOURS = 24;
    long ORDER_TOKEN_EXPIRE_MINUTES = 30;
}
