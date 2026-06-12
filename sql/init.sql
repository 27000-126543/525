-- =====================================================
-- 多租户电商平台 - 数据库初始化脚本
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS ecommerce_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ecommerce_platform;

-- =====================================================
-- IAM模块 - 租户、用户、权限相关表
-- =====================================================

-- 租户表
CREATE TABLE IF NOT EXISTS sys_tenant (
    id BIGINT PRIMARY KEY COMMENT '租户ID',
    tenant_code VARCHAR(64) NOT NULL UNIQUE COMMENT '租户编码',
    tenant_name VARCHAR(128) NOT NULL COMMENT '租户名称',
    contact_name VARCHAR(64) COMMENT '联系人',
    contact_phone VARCHAR(32) COMMENT '联系电话',
    address VARCHAR(512) COMMENT '地址',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    expire_time DATETIME COMMENT '过期时间',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID',
    INDEX idx_tenant_code (tenant_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户表';

-- 组织表
CREATE TABLE IF NOT EXISTS sys_org (
    id BIGINT PRIMARY KEY COMMENT '组织ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父组织ID',
    org_code VARCHAR(64) NOT NULL COMMENT '组织编码',
    org_name VARCHAR(128) NOT NULL COMMENT '组织名称',
    org_type TINYINT DEFAULT 1 COMMENT '组织类型 1-公司 2-部门 3-小组',
    leader VARCHAR(64) COMMENT '负责人',
    phone VARCHAR(32) COMMENT '联系电话',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    remark VARCHAR(512) COMMENT '备注',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    tenant_id BIGINT COMMENT '租户ID',
    INDEX idx_parent_id (parent_id),
    INDEX idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组织表';

-- 岗位表
CREATE TABLE IF NOT EXISTS sys_post (
    id BIGINT PRIMARY KEY COMMENT '岗位ID',
    post_code VARCHAR(64) NOT NULL COMMENT '岗位编码',
    post_name VARCHAR(128) NOT NULL COMMENT '岗位名称',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    remark VARCHAR(512) COMMENT '备注',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    tenant_id BIGINT COMMENT '租户ID',
    INDEX idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位表';

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(64) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(128) NOT NULL COMMENT '密码',
    nickname VARCHAR(64) COMMENT '昵称',
    avatar VARCHAR(512) COMMENT '头像',
    email VARCHAR(128) COMMENT '邮箱',
    mobile VARCHAR(32) COMMENT '手机号',
    gender TINYINT DEFAULT 0 COMMENT '性别 0-未知 1-男 2-女',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    org_id BIGINT COMMENT '组织ID',
    post_id BIGINT COMMENT '岗位ID',
    remark VARCHAR(512) COMMENT '备注',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(64) COMMENT '最后登录IP',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    tenant_id BIGINT COMMENT '租户ID',
    INDEX idx_username (username),
    INDEX idx_org_id (org_id),
    INDEX idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY COMMENT '角色ID',
    role_code VARCHAR(64) NOT NULL COMMENT '角色编码',
    role_name VARCHAR(128) NOT NULL COMMENT '角色名称',
    data_scope TINYINT DEFAULT 1 COMMENT '数据权限范围 1-全部 2-本部门及以下 3-本部门 4-本人',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    sort_order INT DEFAULT 0 COMMENT '排序',
    remark VARCHAR(512) COMMENT '备注',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    tenant_id BIGINT COMMENT '租户ID',
    INDEX idx_role_code (role_code),
    INDEX idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 菜单表
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT PRIMARY KEY COMMENT '菜单ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    menu_name VARCHAR(64) NOT NULL COMMENT '菜单名称',
    sort_order INT DEFAULT 0 COMMENT '排序',
    path VARCHAR(256) COMMENT '路由路径',
    component VARCHAR(256) COMMENT '组件路径',
    perms VARCHAR(128) COMMENT '权限标识',
    menu_type TINYINT DEFAULT 1 COMMENT '菜单类型 1-目录 2-菜单 3-按钮',
    icon VARCHAR(64) COMMENT '菜单图标',
    visible TINYINT DEFAULT 1 COMMENT '是否显示 0-隐藏 1-显示',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    remark VARCHAR(512) COMMENT '备注',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID',
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    tenant_id BIGINT COMMENT '租户ID',
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    tenant_id BIGINT COMMENT '租户ID',
    UNIQUE KEY uk_role_menu (role_id, menu_id),
    INDEX idx_role_id (role_id),
    INDEX idx_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 角色数据权限关联表
CREATE TABLE IF NOT EXISTS sys_role_dept (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    dept_id BIGINT NOT NULL COMMENT '部门ID',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    tenant_id BIGINT COMMENT '租户ID',
    UNIQUE KEY uk_role_dept (role_id, dept_id),
    INDEX idx_role_id (role_id),
    INDEX idx_dept_id (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色数据权限关联表';

-- =====================================================
-- 商品模块 - 商品、库存相关表
-- =====================================================

-- 商品SPU表
CREATE TABLE IF NOT EXISTS product_spu (
    id BIGINT PRIMARY KEY COMMENT 'SPU ID',
    spu_name VARCHAR(256) NOT NULL COMMENT 'SPU名称',
    spu_code VARCHAR(64) NOT NULL UNIQUE COMMENT 'SPU编码',
    category_id BIGINT COMMENT '分类ID',
    brand_id BIGINT COMMENT '品牌ID',
    description TEXT COMMENT '商品描述',
    main_image VARCHAR(512) COMMENT '主图',
    sub_images TEXT COMMENT '副图列表(JSON)',
    spec_template TEXT COMMENT '规格模板(JSON)',
    sales INT DEFAULT 0 COMMENT '销量',
    review_count INT DEFAULT 0 COMMENT '评价数',
    avg_score DECIMAL(3,2) DEFAULT 5.00 COMMENT '平均评分',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态 0-下架 1-上架',
    publish_time DATETIME COMMENT '上架时间',
    is_hot TINYINT DEFAULT 0 COMMENT '是否热销',
    is_new TINYINT DEFAULT 0 COMMENT '是否新品',
    is_recommend TINYINT DEFAULT 0 COMMENT '是否推荐',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    tenant_id BIGINT COMMENT '租户ID',
    INDEX idx_spu_code (spu_code),
    INDEX idx_category_id (category_id),
    INDEX idx_status (status),
    INDEX idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SPU表';

-- 商品SKU表
CREATE TABLE IF NOT EXISTS product_sku (
    id BIGINT PRIMARY KEY COMMENT 'SKU ID',
    sku_code VARCHAR(64) NOT NULL UNIQUE COMMENT 'SKU编码',
    spu_id BIGINT NOT NULL COMMENT 'SPU ID',
    sku_name VARCHAR(256) NOT NULL COMMENT 'SKU名称',
    spec_values TEXT COMMENT '规格值(JSON)',
    price DECIMAL(12,2) NOT NULL COMMENT '售价',
    original_price DECIMAL(12,2) COMMENT '原价',
    cost_price DECIMAL(12,2) COMMENT '成本价',
    image VARCHAR(512) COMMENT 'SKU图片',
    stock INT DEFAULT 0 COMMENT '库存数量',
    locked_stock INT DEFAULT 0 COMMENT '锁定库存',
    sales INT DEFAULT 0 COMMENT '销量',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    weight INT DEFAULT 0 COMMENT '重量(克)',
    bar_code VARCHAR(64) COMMENT '条码',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    tenant_id BIGINT COMMENT '租户ID',
    INDEX idx_sku_code (sku_code),
    INDEX idx_spu_id (spu_id),
    INDEX idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SKU表';

-- 秒杀商品表
CREATE TABLE IF NOT EXISTS seckill_product (
    id BIGINT PRIMARY KEY COMMENT '秒杀商品ID',
    sku_id BIGINT NOT NULL COMMENT 'SKU ID',
    spu_id BIGINT NOT NULL COMMENT 'SPU ID',
    seckill_price DECIMAL(12,2) NOT NULL COMMENT '秒杀价格',
    seckill_stock INT NOT NULL COMMENT '秒杀库存',
    seckill_sold INT DEFAULT 0 COMMENT '已秒杀数量',
    per_user_limit INT DEFAULT 1 COMMENT '每人限购数量',
    activity_name VARCHAR(256) NOT NULL COMMENT '活动名称',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    status TINYINT DEFAULT 0 COMMENT '状态 0-未开始 1-进行中 2-已结束 3-已关闭',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    tenant_id BIGINT COMMENT '租户ID',
    INDEX idx_sku_id (sku_id),
    INDEX idx_start_time (start_time),
    INDEX idx_status (status),
    INDEX idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀商品表';

-- =====================================================
-- 订单模块 - 订单相关表
-- =====================================================

-- 订单表
CREATE TABLE IF NOT EXISTS order_info (
    id BIGINT PRIMARY KEY COMMENT '订单ID',
    order_no VARCHAR(64) NOT NULL UNIQUE COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    order_type TINYINT DEFAULT 1 COMMENT '订单类型 1-普通订单 2-秒杀订单',
    order_source TINYINT DEFAULT 1 COMMENT '订单来源 1-APP 2-小程序 3-H5 4-PC',
    channel_code VARCHAR(64) COMMENT '渠道编码',
    total_amount DECIMAL(12,2) NOT NULL COMMENT '订单总金额',
    pay_amount DECIMAL(12,2) NOT NULL COMMENT '实付金额',
    freight_amount DECIMAL(12,2) DEFAULT 0 COMMENT '运费',
    discount_amount DECIMAL(12,2) DEFAULT 0 COMMENT '优惠金额',
    coupon_amount DECIMAL(12,2) DEFAULT 0 COMMENT '优惠券金额',
    status TINYINT DEFAULT 0 COMMENT '订单状态 0-待支付 1-已支付 2-已发货 3-已收货 4-已完成 5-已取消',
    pay_status TINYINT DEFAULT 0 COMMENT '支付状态 0-未支付 1-已支付 2-已退款',
    pay_type TINYINT COMMENT '支付方式 1-支付宝 2-微信 3-银联',
    pay_time DATETIME COMMENT '支付时间',
    delivery_time DATETIME COMMENT '发货时间',
    receive_time DATETIME COMMENT '收货时间',
    complete_time DATETIME COMMENT '完成时间',
    cancel_time DATETIME COMMENT '取消时间',
    cancel_reason VARCHAR(512) COMMENT '取消原因',
    receiver_name VARCHAR(64) NOT NULL COMMENT '收货人姓名',
    receiver_phone VARCHAR(32) NOT NULL COMMENT '收货人电话',
    receiver_province VARCHAR(64) COMMENT '收货省份',
    receiver_city VARCHAR(64) COMMENT '收货城市',
    receiver_district VARCHAR(64) COMMENT '收货区县',
    receiver_address VARCHAR(512) NOT NULL COMMENT '收货详细地址',
    remark VARCHAR(1024) COMMENT '订单备注',
    coupon_id BIGINT COMMENT '优惠券ID',
    activity_id BIGINT COMMENT '活动ID',
    out_trade_no VARCHAR(128) COMMENT '第三方支付流水号',
    transaction_id VARCHAR(128) COMMENT '第三方交易号',
    invoice_type TINYINT DEFAULT 0 COMMENT '发票类型 0-不开发票 1-电子发票 2-纸质发票',
    invoice_title VARCHAR(256) COMMENT '发票抬头',
    invoice_tax_no VARCHAR(64) COMMENT '发票税号',
    points_used INT DEFAULT 0 COMMENT '使用积分',
    points_deducted DECIMAL(12,2) DEFAULT 0 COMMENT '积分抵扣金额',
    delete_flag TINYINT DEFAULT 0 COMMENT '用户删除标记',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    tenant_id BIGINT COMMENT '租户ID',
    INDEX idx_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_pay_status (pay_status),
    INDEX idx_create_time (create_time),
    INDEX idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 订单明细表
CREATE TABLE IF NOT EXISTS order_item (
    id BIGINT PRIMARY KEY COMMENT '订单项ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    order_no VARCHAR(64) NOT NULL COMMENT '订单号',
    spu_id BIGINT NOT NULL COMMENT 'SPU ID',
    sku_id BIGINT NOT NULL COMMENT 'SKU ID',
    spu_name VARCHAR(256) NOT NULL COMMENT 'SPU名称',
    sku_name VARCHAR(256) NOT NULL COMMENT 'SKU名称',
    spec_values TEXT COMMENT '规格值(JSON)',
    price DECIMAL(12,2) NOT NULL COMMENT '单价',
    quantity INT NOT NULL COMMENT '数量',
    total_amount DECIMAL(12,2) NOT NULL COMMENT '小计金额',
    discount_amount DECIMAL(12,2) DEFAULT 0 COMMENT '优惠金额',
    pay_amount DECIMAL(12,2) NOT NULL COMMENT '实付金额',
    image VARCHAR(512) COMMENT '商品图片',
    is_seckill TINYINT DEFAULT 0 COMMENT '是否秒杀商品',
    is_gift TINYINT DEFAULT 0 COMMENT '是否赠品',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    tenant_id BIGINT COMMENT '租户ID',
    INDEX idx_order_id (order_id),
    INDEX idx_order_no (order_no),
    INDEX idx_sku_id (sku_id),
    INDEX idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- =====================================================
-- 审批流模块
-- =====================================================

CREATE TABLE IF NOT EXISTS approval_process (
    id BIGINT PRIMARY KEY COMMENT '流程ID',
    process_code VARCHAR(64) NOT NULL UNIQUE COMMENT '流程编码',
    process_name VARCHAR(128) NOT NULL COMMENT '流程名称',
    process_type VARCHAR(64) NOT NULL COMMENT '流程类型',
    description VARCHAR(512) COMMENT '流程描述',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    tenant_id BIGINT COMMENT '租户ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批流程表';

CREATE TABLE IF NOT EXISTS approval_node (
    id BIGINT PRIMARY KEY COMMENT '节点ID',
    process_id BIGINT NOT NULL COMMENT '流程ID',
    node_code VARCHAR(64) NOT NULL COMMENT '节点编码',
    node_name VARCHAR(128) NOT NULL COMMENT '节点名称',
    node_type TINYINT NOT NULL COMMENT '节点类型 1-审批 2-抄送',
    approval_type TINYINT COMMENT '审批类型 1-任何人通过 2-所有人通过 3-数量通过 4-百分比通过',
    approval_count INT COMMENT '通过数量',
    approval_percent INT COMMENT '通过百分比',
    sort_order INT DEFAULT 0 COMMENT '排序',
    approver_ids TEXT COMMENT '审批人ID列表(JSON)',
    cc_ids TEXT COMMENT '抄送人ID列表(JSON)',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    tenant_id BIGINT COMMENT '租户ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批节点表';

CREATE TABLE IF NOT EXISTS approval_instance (
    id BIGINT PRIMARY KEY COMMENT '实例ID',
    process_id BIGINT NOT NULL COMMENT '流程ID',
    business_id BIGINT NOT NULL COMMENT '业务ID',
    business_type VARCHAR(64) NOT NULL COMMENT '业务类型',
    business_no VARCHAR(128) COMMENT '业务编号',
    title VARCHAR(256) NOT NULL COMMENT '审批标题',
    content TEXT COMMENT '审批内容(JSON)',
    current_node_id BIGINT COMMENT '当前节点ID',
    status TINYINT DEFAULT 0 COMMENT '状态 0-审批中 1-已通过 2-已驳回 3-已撤回 4-已撤销',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    applicant_name VARCHAR(64) NOT NULL COMMENT '申请人名称',
    apply_time DATETIME NOT NULL COMMENT '申请时间',
    complete_time DATETIME COMMENT '完成时间',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    tenant_id BIGINT COMMENT '租户ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批实例表';

CREATE TABLE IF NOT EXISTS approval_record (
    id BIGINT PRIMARY KEY COMMENT '记录ID',
    instance_id BIGINT NOT NULL COMMENT '实例ID',
    node_id BIGINT NOT NULL COMMENT '节点ID',
    node_name VARCHAR(128) NOT NULL COMMENT '节点名称',
    approver_id BIGINT NOT NULL COMMENT '审批人ID',
    approver_name VARCHAR(64) NOT NULL COMMENT '审批人名称',
    approval_action TINYINT NOT NULL COMMENT '审批动作 1-通过 2-驳回 3-转交 4-撤回 5-撤销',
    approval_comment VARCHAR(1024) COMMENT '审批意见',
    approval_time DATETIME NOT NULL COMMENT '审批时间',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    tenant_id BIGINT COMMENT '租户ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批记录表';

CREATE TABLE IF NOT EXISTS approval_cc (
    id BIGINT PRIMARY KEY COMMENT '抄送ID',
    instance_id BIGINT NOT NULL COMMENT '实例ID',
    node_id BIGINT NOT NULL COMMENT '节点ID',
    cc_user_id BIGINT NOT NULL COMMENT '抄送人ID',
    cc_user_name VARCHAR(64) NOT NULL COMMENT '抄送人名称',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读 0-未读 1-已读',
    read_time DATETIME COMMENT '阅读时间',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    tenant_id BIGINT COMMENT '租户ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批抄送表';

-- =====================================================
-- 消息通知模块
-- =====================================================

CREATE TABLE IF NOT EXISTS notification_message (
    id BIGINT PRIMARY KEY COMMENT '消息ID',
    message_no VARCHAR(64) NOT NULL UNIQUE COMMENT '消息编号',
    template_code VARCHAR(64) COMMENT '模板编码',
    channel_type TINYINT NOT NULL COMMENT '渠道类型 1-短信 2-邮件 3-站内信 4-App推送',
    priority TINYINT DEFAULT 2 COMMENT '优先级 1-高 2-中 3-低',
    title VARCHAR(256) COMMENT '消息标题',
    content TEXT NOT NULL COMMENT '消息内容',
    receiver_id BIGINT COMMENT '接收人ID',
    receiver_name VARCHAR(64) COMMENT '接收人名称',
    receiver_account VARCHAR(128) COMMENT '接收账号(手机号/邮箱/设备ID)',
    params TEXT COMMENT '消息参数(JSON)',
    status TINYINT DEFAULT 0 COMMENT '状态 0-待发送 1-发送中 2-已发送 3-发送失败',
    send_time DATETIME COMMENT '发送时间',
    failed_reason VARCHAR(512) COMMENT '失败原因',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    max_retry INT DEFAULT 3 COMMENT '最大重试次数',
    next_retry_time DATETIME COMMENT '下次重试时间',
    trace_id VARCHAR(64) COMMENT '链路ID',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    tenant_id BIGINT COMMENT '租户ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- =====================================================
-- 支付模块
-- =====================================================

CREATE TABLE IF NOT EXISTS payment_record (
    id BIGINT PRIMARY KEY COMMENT '支付记录ID',
    pay_no VARCHAR(64) NOT NULL UNIQUE COMMENT '支付单号',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    order_no VARCHAR(64) NOT NULL COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    pay_type TINYINT NOT NULL COMMENT '支付方式 1-支付宝 2-微信 3-银联',
    pay_channel VARCHAR(64) COMMENT '支付渠道',
    pay_amount DECIMAL(12,2) NOT NULL COMMENT '支付金额',
    pay_status TINYINT DEFAULT 0 COMMENT '支付状态 0-待支付 1-已支付 2-已退款',
    pay_time DATETIME COMMENT '支付时间',
    out_trade_no VARCHAR(128) COMMENT '第三方支付流水号',
    transaction_id VARCHAR(128) COMMENT '第三方交易号',
    callback_time DATETIME COMMENT '回调时间',
    callback_content TEXT COMMENT '回调内容',
    refund_amount DECIMAL(12,2) DEFAULT 0 COMMENT '退款金额',
    refund_time DATETIME COMMENT '退款时间',
    refund_reason VARCHAR(512) COMMENT '退款原因',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    tenant_id BIGINT COMMENT '租户ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

CREATE TABLE IF NOT EXISTS logistics_order (
    id BIGINT PRIMARY KEY COMMENT '物流单ID',
    logistics_no VARCHAR(64) NOT NULL UNIQUE COMMENT '物流单号',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    order_no VARCHAR(64) NOT NULL COMMENT '订单号',
    logistics_company VARCHAR(64) NOT NULL COMMENT '物流公司',
    logistics_code VARCHAR(64) COMMENT '物流公司编码',
    sender_name VARCHAR(64) COMMENT '发货人姓名',
    sender_phone VARCHAR(32) COMMENT '发货人电话',
    sender_address VARCHAR(512) COMMENT '发货地址',
    receiver_name VARCHAR(64) NOT NULL COMMENT '收货人姓名',
    receiver_phone VARCHAR(32) NOT NULL COMMENT '收货人电话',
    receiver_address VARCHAR(512) NOT NULL COMMENT '收货地址',
    freight DECIMAL(12,2) DEFAULT 0 COMMENT '运费',
    status TINYINT DEFAULT 0 COMMENT '物流状态 0-待发货 1-已发货 2-运输中 3-已签收 4-异常',
    delivery_time DATETIME COMMENT '发货时间',
    receive_time DATETIME COMMENT '签收时间',
    latest_track TEXT COMMENT '最新物流轨迹(JSON)',
    track_callback_content TEXT COMMENT '轨迹回调内容',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    tenant_id BIGINT COMMENT '租户ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流单表';

-- =====================================================
-- 审计日志模块
-- =====================================================

CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT PRIMARY KEY COMMENT '日志ID',
    module VARCHAR(64) COMMENT '模块',
    business_type VARCHAR(64) COMMENT '业务类型',
    operation VARCHAR(128) COMMENT '操作',
    business_id BIGINT COMMENT '业务ID',
    business_no VARCHAR(128) COMMENT '业务编号',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(64) COMMENT '操作人名称',
    org_id BIGINT COMMENT '组织ID',
    tenant_id BIGINT COMMENT '租户ID',
    ip VARCHAR(64) COMMENT 'IP地址',
    user_agent VARCHAR(512) COMMENT 'User-Agent',
    request_params TEXT COMMENT '请求参数',
    response_result TEXT COMMENT '响应结果',
    status TINYINT DEFAULT 0 COMMENT '操作状态 0-成功 1-失败',
    cost_time BIGINT COMMENT '耗时(ms)',
    trace_id VARCHAR(64) COMMENT '链路ID',
    error_message VARCHAR(1024) COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_module (module),
    INDEX idx_business_type (business_type),
    INDEX idx_operator_id (operator_id),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_create_time (create_time),
    INDEX idx_trace_id (trace_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';

CREATE TABLE IF NOT EXISTS trace_log (
    id BIGINT PRIMARY KEY COMMENT 'ID',
    trace_id VARCHAR(64) NOT NULL COMMENT '链路ID',
    span_id VARCHAR(64) NOT NULL COMMENT '跨度ID',
    parent_span_id VARCHAR(64) COMMENT '父跨度ID',
    service_name VARCHAR(64) NOT NULL COMMENT '服务名称',
    method_name VARCHAR(128) COMMENT '方法名称',
    request_path VARCHAR(256) COMMENT '请求路径',
    start_time BIGINT NOT NULL COMMENT '开始时间戳',
    end_time BIGINT NOT NULL COMMENT '结束时间戳',
    duration BIGINT COMMENT '耗时(ms)',
    status TINYINT DEFAULT 0 COMMENT '状态 0-成功 1-失败',
    request_params TEXT COMMENT '请求参数',
    response_result TEXT COMMENT '响应结果',
    error_message VARCHAR(1024) COMMENT '错误信息',
    extra TEXT COMMENT '额外信息(JSON)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_trace_id (trace_id),
    INDEX idx_span_id (span_id),
    INDEX idx_service_name (service_name),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='链路日志表';

-- =====================================================
-- 推荐模块
-- =====================================================

CREATE TABLE IF NOT EXISTS user_behavior (
    id BIGINT PRIMARY KEY COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    spu_id BIGINT NOT NULL COMMENT 'SPU ID',
    behavior_type TINYINT NOT NULL COMMENT '行为类型 1-浏览 2-收藏 3-加购 4-下单 5-评价',
    behavior_score INT DEFAULT 1 COMMENT '行为分数',
    duration INT DEFAULT 0 COMMENT '停留时长(秒)',
    device_type VARCHAR(32) COMMENT '设备类型',
    source VARCHAR(64) COMMENT '来源',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    tenant_id BIGINT COMMENT '租户ID',
    INDEX idx_user_id (user_id),
    INDEX idx_spu_id (spu_id),
    INDEX idx_behavior_type (behavior_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为表';

-- =====================================================
-- 数据分析模块
-- =====================================================

CREATE TABLE IF NOT EXISTS analytics_summary (
    id BIGINT PRIMARY KEY COMMENT 'ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    stat_hour TINYINT COMMENT '统计小时(0-23)',
    tenant_id BIGINT COMMENT '租户ID',
    order_count INT DEFAULT 0 COMMENT '订单数',
    order_amount DECIMAL(15,2) DEFAULT 0 COMMENT '订单金额',
    pay_count INT DEFAULT 0 COMMENT '支付数',
    pay_amount DECIMAL(15,2) DEFAULT 0 COMMENT '支付金额',
    refund_count INT DEFAULT 0 COMMENT '退款数',
    refund_amount DECIMAL(15,2) DEFAULT 0 COMMENT '退款金额',
    new_user_count INT DEFAULT 0 COMMENT '新增用户数',
    active_user_count INT DEFAULT 0 COMMENT '活跃用户数',
    pv INT DEFAULT 0 COMMENT '页面浏览量',
    uv INT DEFAULT 0 COMMENT '独立访客数',
    conversion_rate DECIMAL(10,4) DEFAULT 0 COMMENT '转化率',
    average_order_amount DECIMAL(12,2) DEFAULT 0 COMMENT '客单价',
    channel_code VARCHAR(64) COMMENT '渠道编码',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_date_hour_tenant (stat_date, stat_hour, tenant_id, channel_code),
    INDEX idx_stat_date (stat_date),
    INDEX idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据分析汇总表';

-- =====================================================
-- 初始化数据
-- =====================================================

-- 初始化租户数据 (tenant_id=0 表示系统级数据，不受多租户限制)
-- 租户1: 京东旗舰店 (id=1001)
-- 租户2: 淘宝旗舰店 (id=1002)
INSERT INTO sys_tenant (id, tenant_code, tenant_name, contact_name, contact_phone, status, tenant_id) VALUES
(1001, 'jd_flagship', '京东旗舰店', '张经理', '13800138001', 1, 0),
(1002, 'tb_flagship', '淘宝旗舰店', '李经理', '13800138002', 1, 0);

-- 初始化菜单 (系统级，tenant_id=0)
INSERT INTO sys_menu (id, parent_id, menu_name, sort_order, path, component, perms, menu_type, icon, visible, status, tenant_id) VALUES
(1, 0, '系统管理', 1, '/system', NULL, NULL, 1, 'setting', 1, 1, 0),
(11, 1, '用户管理', 1, '/system/user', 'system/user/index', 'system:user:list', 2, 'user', 1, 1, 0),
(12, 1, '角色管理', 2, '/system/role', 'system/role/index', 'system:role:list', 2, 'role', 1, 1, 0),
(13, 1, '菜单管理', 3, '/system/menu', 'system/menu/index', 'system:menu:list', 2, 'menu', 1, 1, 0),
(2, 0, '商品管理', 2, '/product', NULL, NULL, 1, 'shop', 1, 1, 0),
(21, 2, '商品列表', 1, '/product/list', 'product/list/index', 'product:list', 2, 'list', 1, 1, 0),
(22, 2, '库存管理', 2, '/product/stock', 'product/stock/index', 'product:stock', 2, 'stock', 1, 1, 0),
(3, 0, '订单管理', 3, '/order', NULL, NULL, 1, 'order', 1, 1, 0),
(31, 3, '订单列表', 1, '/order/list', 'order/list/index', 'order:list', 2, 'list', 1, 1, 0);

-- 初始化租户1 (京东旗舰店) 的数据
-- 组织
INSERT INTO sys_org (id, parent_id, org_code, org_name, org_type, sort_order, status, tenant_id) VALUES
(11001, 0, 'JD_COMPANY', '京东旗舰店公司', 1, 1, 1, 1001),
(11002, 11001, 'JD_TECH', '技术部', 2, 1, 1, 1001),
(11003, 11001, 'JD_SALE', '销售部', 2, 2, 1, 1001);

-- 岗位
INSERT INTO sys_post (id, post_code, post_name, sort_order, status, tenant_id) VALUES
(11001, 'JD_MANAGER', '店长', 1, 1, 1001),
(11002, 'JD_DEVELOPER', '开发工程师', 2, 1, 1001),
(11003, 'JD_SALESMAN', '销售员', 3, 1, 1001);

-- 角色
INSERT INTO sys_role (id, role_code, role_name, data_scope, status, sort_order, remark, tenant_id) VALUES
(11001, 'JD_ADMIN', '管理员', 1, 1, 1, '拥有全部权限', 1001),
(11002, 'JD_OPERATOR', '运营', 3, 1, 2, '本部门数据权限', 1001),
(11003, 'JD_FINANCE', '财务', 4, 1, 3, '仅本人数据权限', 1001);

-- 角色菜单关联
INSERT INTO sys_role_menu (role_id, menu_id, tenant_id) VALUES
(11001, 11, 1001),
(11001, 12, 1001),
(11001, 13, 1001),
(11001, 21, 1001),
(11001, 22, 1001),
(11001, 31, 1001),
(11002, 21, 1001),
(11002, 22, 1001),
(11003, 31, 1001);

-- 用户 (密码: 123456, BCrypt加密后的值)
INSERT INTO sys_user (id, username, password, nickname, email, mobile, gender, status, org_id, post_id, tenant_id) VALUES
(11001, 'jd_admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '京东管理员', 'admin@jd.com', '13800138101', 1, 1, 11001, 11001, 1001),
(11002, 'jd_operator', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '京东运营', 'operator@jd.com', '13800138102', 1, 1, 11003, 11003, 1001),
(11003, 'jd_user', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '京东用户', 'user@jd.com', '13800138103', 1, 1, NULL, NULL, 1001);

-- 用户角色关联
INSERT INTO sys_user_role (user_id, role_id, tenant_id) VALUES
(11001, 11001, 1001),
(11002, 11002, 1001);

-- 初始化租户2 (淘宝旗舰店) 的数据
-- 组织
INSERT INTO sys_org (id, parent_id, org_code, org_name, org_type, sort_order, status, tenant_id) VALUES
(12001, 0, 'TB_COMPANY', '淘宝旗舰店公司', 1, 1, 1, 1002),
(12002, 12001, 'TB_TECH', '技术部', 2, 1, 1, 1002),
(12003, 12001, 'TB_SALE', '销售部', 2, 2, 1, 1002);

-- 岗位
INSERT INTO sys_post (id, post_code, post_name, sort_order, status, tenant_id) VALUES
(12001, 'TB_MANAGER', '店长', 1, 1, 1002),
(12002, 'TB_DEVELOPER', '开发工程师', 2, 1, 1002),
(12003, 'TB_SALESMAN', '销售员', 3, 1, 1002);

-- 角色
INSERT INTO sys_role (id, role_code, role_name, data_scope, status, sort_order, remark, tenant_id) VALUES
(12001, 'TB_ADMIN', '管理员', 1, 1, 1, '拥有全部权限', 1002),
(12002, 'TB_OPERATOR', '运营', 3, 1, 2, '本部门数据权限', 1002);

-- 角色菜单关联
INSERT INTO sys_role_menu (role_id, menu_id, tenant_id) VALUES
(12001, 11, 1002),
(12001, 12, 1002),
(12001, 13, 1002),
(12001, 21, 1002),
(12001, 22, 1002),
(12001, 31, 1002),
(12002, 21, 1002),
(12002, 22, 1002);

-- 用户
INSERT INTO sys_user (id, username, password, nickname, email, mobile, gender, status, org_id, post_id, tenant_id) VALUES
(12001, 'tb_admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '淘宝管理员', 'admin@taobao.com', '13800138201', 1, 1, 12001, 12001, 1002),
(12002, 'tb_user', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '淘宝用户', 'user@taobao.com', '13800138202', 1, 1, NULL, NULL, 1002);

-- 用户角色关联
INSERT INTO sys_user_role (user_id, role_id, tenant_id) VALUES
(12001, 12001, 1002);

-- 初始化租户1的商品数据
INSERT INTO product_spu (id, spu_name, spu_code, category_id, description, main_image, sales, status, is_hot, is_recommend, create_by, tenant_id) VALUES
(11001, 'iPhone 15 Pro Max 256G 原色钛金属', 'IP15PM-256', 1, '最新款iPhone，A17 Pro芯片', 'https://example.com/ip15pm.jpg', 100, 1, 1, 1, 11001, 1001),
(11002, 'MacBook Pro 14英寸 M3 Pro', 'MBP14-M3P', 2, '专业级笔记本电脑', 'https://example.com/mbp14.jpg', 50, 1, 0, 1, 11001, 1001),
(11003, 'AirPods Pro 2代', 'APP2', 3, '主动降噪无线耳机', 'https://example.com/app2.jpg', 200, 1, 1, 0, 11001, 1001);

INSERT INTO product_sku (id, sku_code, spu_id, sku_name, spec_values, price, original_price, cost_price, stock, locked_stock, status, create_by, tenant_id) VALUES
(11001, 'IP15PM-256-01', 11001, 'iPhone 15 Pro Max 256G 原色钛金属', '{"颜色":"原色钛金属","容量":"256G"}', 9999.00, 10999.00, 8000.00, 100, 0, 1, 11001, 1001),
(11002, 'IP15PM-256-02', 11001, 'iPhone 15 Pro Max 256G 蓝色钛金属', '{"颜色":"蓝色钛金属","容量":"256G"}', 9999.00, 10999.00, 8000.00, 50, 0, 1, 11001, 1001),
(11003, 'MBP14-M3P-01', 11002, 'MacBook Pro 14英寸 M3 Pro 18G+512G', '{"配置":"18G+512G"}', 16999.00, 17999.00, 14000.00, 30, 0, 1, 11001, 1001),
(11004, 'APP2-01', 11003, 'AirPods Pro 2代', '{}', 1899.00, 1999.00, 1500.00, 200, 0, 1, 11001, 1001);

-- 初始化租户2的商品数据
INSERT INTO product_spu (id, spu_name, spu_code, category_id, description, main_image, sales, status, is_hot, is_recommend, create_by, tenant_id) VALUES
(12001, '华为 Mate 60 Pro 12+512G 雅川青', 'MATE60P-512', 1, '华为最新旗舰，麒麟9000S芯片', 'https://example.com/mate60p.jpg', 150, 1, 1, 1, 12001, 1002),
(12002, '小米14 Ultra 16+512G 黑色', 'MI14U-512', 1, '小米影像旗舰', 'https://example.com/mi14u.jpg', 80, 1, 0, 1, 12001, 1002);

INSERT INTO product_sku (id, sku_code, spu_id, sku_name, spec_values, price, original_price, cost_price, stock, locked_stock, status, create_by, tenant_id) VALUES
(12001, 'MATE60P-512-01', 12001, '华为 Mate 60 Pro 12+512G 雅川青', '{"颜色":"雅川青","容量":"12+512G"}', 6999.00, 7499.00, 5500.00, 80, 0, 1, 12001, 1002),
(12002, 'MI14U-512-01', 12002, '小米14 Ultra 16+512G 黑色', '{"颜色":"黑色","容量":"16+512G"}', 6499.00, 6999.00, 5000.00, 60, 0, 1, 12001, 1002);

-- 初始化审批流程
INSERT INTO approval_process (id, process_code, process_name, process_type, description, status, create_by, tenant_id) VALUES
(1001, 'PRICE_CHANGE', '价格变更审批', 'PRICE_CHANGE', '商品价格变更需要审批', 1, 11001, 1001),
(1002, 'SUPPLIER_ENTRY', '供应商入驻审批', 'SUPPLIER_ENTRY', '新供应商入驻审批', 1, 11001, 1001),
(1003, 'BIG_REFUND', '大额退款审批', 'BIG_REFUND', '超过1000元的退款审批', 1, 11001, 1001);

INSERT INTO approval_node (id, process_id, node_code, node_name, node_type, approval_type, sort_order, approver_ids, create_by, tenant_id) VALUES
(1001, 1001, 'NODE1', '运营经理审批', 1, 1, 1, '[11001]', 11001, 1001),
(1002, 1001, 'NODE2', '财务审批', 1, 1, 2, '[11003]', 11001, 1001),
(1003, 1003, 'NODE1', '客服主管审批', 1, 1, 1, '[11001]', 11001, 1001);

-- =====================================================
-- 初始化完成
-- =====================================================
