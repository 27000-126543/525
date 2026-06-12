# 多租户电商平台 - 运行说明

## 一、项目概述

本项目是基于 Spring Cloud + Spring Cloud Alibaba 的大型多租户全渠道电商平台后端API服务，包含11个微服务模块，支撑千万级交易请求。

### 模块清单与端口

| 模块 | 端口 | context-path | 说明 |
|------|------|--------------|------|
| ecommerce-gateway | 8080 | / | API网关 |
| ecommerce-iam | 8001 | /iam | 身份认证与权限服务 |
| ecommerce-product | 8002 | /product | 商品与库存服务 |
| ecommerce-order | 8003 | /order | 订单服务 |
| ecommerce-recommendation | 8004 | /recommend | 智能推荐引擎 |
| ecommerce-approval | 8005 | /approval | 审批流引擎 |
| ecommerce-analytics | 8006 | /analytics | 实时数据分析 |
| ecommerce-notification | 8007 | /notification | 多渠道消息推送 |
| ecommerce-payment | 8008 | /payment | 支付与物流对账 |
| ecommerce-audit | 8009 | /audit | 全链路日志审计 |

---

## 二、环境要求

### 必需软件

| 软件 | 版本要求 | 说明 |
|------|----------|------|
| JDK | 17+ | Java开发环境 |
| Maven | 3.8+ | 项目构建工具 |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | 6.0+ | 缓存、分布式锁、限流 |
| Nacos | 2.2+ | 服务注册与配置中心（可选，首次测试可跳过） |

### 可选软件（生产环境必需）

| 软件 | 版本要求 | 说明 |
|------|----------|------|
| RocketMQ | 4.9+ | 消息队列（实现最终一致性） |
| Elasticsearch | 7.x+ | 日志与分析搜索引擎 |
| Node.js | 16+ | 前端开发环境 |

---

## 三、中间件安装与启动

### 1. MySQL 安装与配置

```bash
# macOS 使用 Homebrew
brew install mysql
brew services start mysql

# 初始化配置
mysql_secure_installation

# 创建数据库
mysql -u root -p < sql/init.sql
```

**默认账号密码**：root / root

### 2. Redis 安装与配置

```bash
# macOS 使用 Homebrew
brew install redis
brew services start redis

# 验证
redis-cli ping
# 返回 PONG 表示成功
```

### 3. Nacos 安装与配置（可选）

```bash
# 下载 nacos
wget https://github.com/alibaba/nacos/releases/download/2.2.3/nacos-server-2.2.3.zip
unzip nacos-server-2.2.3.zip
cd nacos/bin

# 单机模式启动
sh startup.sh -m standalone
```

访问：http://localhost:8848/nacos
默认账号密码：nacos / nacos

---

## 四、数据库初始化

### 执行初始化脚本

```bash
# 进入项目目录
cd /path/to/project

# 执行初始化脚本（包含表结构和测试数据）
mysql -u root -p < sql/init.sql
```

### 初始化数据说明

脚本会自动创建：
- **2个租户**：京东旗舰店(tenant_code=jd_flagship)、淘宝旗舰店(tenant_code=tb_flagship)
- **多个测试用户**（密码均为 `123456`）：
  - 租户1管理员：jd_admin / 123456
  - 租户1运营：jd_operator / 123456
  - 租户1普通用户：jd_user / 123456
  - 租户2管理员：tb_admin / 123456
  - 租户2普通用户：tb_user / 123456
- **测试商品数据**：每个租户各有若干商品和SKU
- **基础权限配置**：角色、菜单、组织、岗位等
- **审批流程配置**：价格变更、供应商入驻、大额退款审批流程

---

## 五、服务启动顺序

> **注意**：首次测试建议不依赖 Nacos，直接启动服务。如要使用 Nacos，请确保 Nacos 已启动。

### 1. 编译项目

```bash
# 全量编译
mvn clean install -DskipTests

# 或者单独编译某个模块
mvn clean install -DskipTests -pl ecommerce-common -am
```

### 2. 启动服务（按顺序）

#### 方式一：命令行启动

```bash
# 1. 启动 IAM 服务
cd ecommerce-iam
mvn spring-boot:run

# 2. 启动商品服务
cd ../ecommerce-product
mvn spring-boot:run

# 3. 启动订单服务
cd ../ecommerce-order
mvn spring-boot:run

# 4. 启动网关服务
cd ../ecommerce-gateway
mvn spring-boot:run

# 5. 其他服务（按需启动）
cd ../ecommerce-recommendation
mvn spring-boot:run
```

#### 方式二：IDE 启动

在 IDEA 中直接运行各模块的主类：
- IAM: `IamApplication.java`
- 商品: `ProductApplication.java`
- 订单: `OrderApplication.java`
- 网关: `GatewayApplication.java`
- ...

### 3. 跳过 Nacos 启动（可选）

如果没有启动 Nacos，可以在启动时添加参数禁用：

```bash
mvn spring-boot:run -Dspring.cloud.nacos.discovery.enabled=false -Dspring.cloud.nacos.config.enabled=false
```

或者修改 `bootstrap.yml`，暂时注释掉 nacos 配置。

---

## 六、接口验证指南

### 1. 健康检查接口

所有服务都提供健康检查接口，验证服务是否启动成功：

```bash
# 网关健康检查
curl http://localhost:8080/health

# IAM服务健康检查
curl http://localhost:8001/iam/health

# 商品服务健康检查
curl http://localhost:8002/product/health

# 订单服务健康检查
curl http://localhost:8003/order/health
```

### 2. 多租户与权限验证

#### 步骤1：租户1（京东）管理员登录

```bash
curl -X POST http://localhost:8001/iam/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "tenantCode": "jd_flagship",
    "username": "jd_admin",
    "password": "123456"
  }'
```

**返回示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "xxxx-xxxx-xxxx-xxxx",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "userInfo": {
      "userId": 11001,
      "username": "jd_admin",
      "tenantId": 1001,
      "tenantName": "京东旗舰店",
      "roles": ["JD_ADMIN"],
      "perms": ["system:user:list", "product:list", "..."]
    }
  }
}
```

#### 步骤2：使用 Token 访问受保护接口

```bash
# 保存 Token
TOKEN="你的token值"

# 查询租户1的用户列表（只能看到京东旗舰店的用户）
curl http://localhost:8001/iam/user/page \
  -H "Authorization: $TOKEN"

# 查询租户1的商品列表（只能看到京东旗舰店的商品）
curl http://localhost:8002/product/page \
  -H "Authorization: $TOKEN"
```

#### 步骤3：切换租户2（淘宝）验证数据隔离

```bash
# 淘宝管理员登录
curl -X POST http://localhost:8001/iam/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "tenantCode": "tb_flagship",
    "username": "tb_admin",
    "password": "123456"
  }'

# 使用新Token查询商品（只能看到淘宝旗舰店的商品，看不到京东的）
curl http://localhost:8002/product/page \
  -H "Authorization: $TB_TOKEN"
```

### 3. 商品库存与下单流程验证

#### 步骤1：查询商品库存

```bash
# 先获取商品列表，找一个有库存的SKU
curl http://localhost:8002/product/page \
  -H "Authorization: $TOKEN"

# 查询指定SKU的库存（假设SKU ID是 11001）
curl http://localhost:8002/product/stock/11001 \
  -H "Authorization: $TOKEN"
```

#### 步骤2：获取订单Token（防重复下单）

```bash
curl http://localhost:8003/order/token \
  -H "Authorization: $TOKEN"
```

**返回**：
```json
{
  "code": 200,
  "message": "success",
  "data": "order-token-xxxx-xxxx"
}
```

#### 步骤3：提交订单

```bash
ORDER_TOKEN="上一步获取的token"

curl -X POST http://localhost:8003/order \
  -H "Content-Type: application/json" \
  -H "Authorization: $TOKEN" \
  -d '{
    "orderToken": "'$ORDER_TOKEN'",
    "orderType": 1,
    "orderSource": 1,
    "receiverName": "张三",
    "receiverPhone": "13800138000",
    "receiverProvince": "北京市",
    "receiverCity": "北京市",
    "receiverDistrict": "朝阳区",
    "receiverAddress": "某某街道123号",
    "items": [
      {
        "skuId": 11001,
        "quantity": 2,
        "price": 9999.00
      }
    ]
  }'
```

**返回成功示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 订单ID,
    "orderNo": "ORD2024010100001",
    "status": 0,
    "totalAmount": 19998.00,
    "payAmount": 19998.00
  }
}
```

#### 步骤4：验证库存扣减

```bash
# 再次查询库存，应该减少了2
curl http://localhost:8002/product/stock/11001 \
  -H "Authorization: $TOKEN"
```

#### 步骤5：测试防重复下单

```bash
# 使用同一个 orderToken 再次提交订单，应该返回失败
curl -X POST http://localhost:8003/order \
  -H "Content-Type: application/json" \
  -H "Authorization: $TOKEN" \
  -d '{
    "orderToken": "'$ORDER_TOKEN'",
    ...
  }'
```

**返回**：`订单已提交，请勿重复操作`

#### 步骤6：测试库存不足场景

```bash
# 获取新的订单Token
curl http://localhost:8003/order/token \
  -H "Authorization: $TOKEN"

# 提交一个超过库存数量的订单
curl -X POST http://localhost:8003/order \
  -H "Content-Type: application/json" \
  -H "Authorization: $TOKEN" \
  -d '{
    "orderToken": "新的token",
    ...
    "items": [
      {
        "skuId": 11001,
        "quantity": 99999,  # 超过实际库存
        "price": 9999.00
      }
    ]
  }'
```

**返回**：`库存不足`

### 4. 其他模块最小闭环验证

#### 推荐引擎

```bash
# 获取热门推荐
curl http://localhost:8004/recommend/hot \
  -H "Authorization: $TOKEN"

# 获取个性化推荐
curl http://localhost:8004/recommend/personal \
  -H "Authorization: $TOKEN"
```

#### 审批流

```bash
# 查询审批流程列表
curl http://localhost:8005/approval/process/list \
  -H "Authorization: $TOKEN"

# 发起审批
curl -X POST http://localhost:8005/approval/instance \
  -H "Content-Type: application/json" \
  -H "Authorization: $TOKEN" \
  -d '{
    "processCode": "PRICE_CHANGE",
    "businessId": 11001,
    "title": "iPhone 15 Pro Max 价格变更",
    "content": "{\"oldPrice\": 9999, \"newPrice\": 8999}"
  }'
```

#### 消息通知

```bash
# 发送短信
curl -X POST http://localhost:8007/notification/send \
  -H "Content-Type: application/json" \
  -H "Authorization: $TOKEN" \
  -d '{
    "channelType": 1,
    "title": "验证码",
    "content": "您的验证码是：123456",
    "receiverAccount": "13800138000"
  }'

# 发送站内信
curl -X POST http://localhost:8007/notification/send \
  -H "Content-Type: application/json" \
  -H "Authorization: $TOKEN" \
  -d '{
    "channelType": 3,
    "receiverId": 11003,
    "title": "订单通知",
    "content": "您的订单已发货"
  }'
```

#### 支付回调

```bash
# 模拟支付宝回调（实际由支付宝服务器调用）
curl -X POST http://localhost:8008/payment/callback/alipay \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d 'trade_no=202401010001&out_trade_no=ORD2024010100001&total_amount=19998.00&trade_status=TRADE_SUCCESS'

# 查询支付记录
curl http://localhost:8008/payment/record/page \
  -H "Authorization: $TOKEN"
```

#### 审计日志

```bash
# 查询审计日志
curl "http://localhost:8009/audit/search?module=order&businessType=create" \
  -H "Authorization: $TOKEN"

# 查询链路日志
curl "http://localhost:8009/trace/search?serviceName=ecommerce-order" \
  -H "Authorization: $TOKEN"
```

#### 数据分析

```bash
# 获取实时概览
curl http://localhost:8006/analytics/overview \
  -H "Authorization: $TOKEN"

# 获取销售趋势
curl "http://localhost:8006/analytics/sales/trend?timeRange=7d" \
  -H "Authorization: $TOKEN"

# 获取商品排行榜
curl "http://localhost:8006/analytics/product/ranking?limit=10" \
  -H "Authorization: $TOKEN"
```

---

## 七、核心技术说明

### 1. 多租户数据隔离

- 基于 MyBatis-Plus `TenantLineInnerInterceptor` 插件实现行级隔离
- 所有查询自动注入 `tenant_id` 条件
- 登录时根据 `tenantCode` 确定租户，`TenantContext` 传递租户ID
- 系统级表（如 `sys_tenant`）不受多租户限制

### 2. 分布式锁

- 基于 Redisson 实现
- 用于库存扣减、支付回调幂等、审批并发控制等场景

### 3. 订单防重

- 下单前获取唯一 `orderToken`（存储在Redis，有效期30分钟）
- 下单时校验并删除 Token，同一个 Token 只能使用一次

### 4. 库存扣减

- 普通订单：分布式锁 + DB 原子扣减（`UPDATE ... WHERE stock >= ?`）
- 秒杀订单：Redis `DECR` 原子操作预扣库存，高性能

### 5. 全链路追踪

- 网关生成 `TraceId`，通过 HTTP Header 传递
- 各服务接力传递 `TraceId` 和 `SpanId`
- 支持按 `TraceId` 查询完整调用链

---

## 八、常见问题

### 1. 启动时报数据库连接错误

检查：
- MySQL 是否启动
- 数据库连接配置（application.yml）是否正确
- 数据库 `ecommerce_platform` 是否已创建
- 初始化脚本是否已执行

### 2. 启动时报 Redis 连接错误

检查：
- Redis 是否启动：`redis-cli ping`
- Redis 配置是否正确

### 3. 登录后查询不到数据

检查：
- 是否在请求头中携带了正确的 `Authorization` Token
- 该用户所属租户是否有对应数据
- Token 是否已过期

### 4. 多租户隔离不生效

检查：
- 实体类是否继承了 `BaseEntity`（包含 `tenantId` 字段）
- MyBatis-Plus 租户插件是否正确配置
- 表名是否在忽略列表中

### 5. 编译时报错

```bash
# 清理后重新编译
mvn clean install -DskipTests

# 检查 JDK 版本
java -version  # 必须是 17+
```

---

## 九、生产环境部署建议

1. **数据库**：采用主从复制 + 读写分离，按租户分库分表
2. **Redis**：采用集群模式，开启持久化
3. **服务部署**：K8s + Docker，每个服务独立部署，多实例
4. **消息队列**：RocketMQ 主从架构，开启消息轨迹
5. **监控告警**：Prometheus + Grafana，配置关键指标告警
6. **日志收集**：ELK 或者 Loki，统一日志收集分析

---

## 十、接口文档

启动服务后，可以通过 Knife4j 查看详细的接口文档：

| 服务 | 文档地址 |
|------|----------|
| IAM | http://localhost:8001/iam/doc.html |
| 商品 | http://localhost:8002/product/doc.html |
| 订单 | http://localhost:8003/order/doc.html |
| 推荐 | http://localhost:8004/recommend/doc.html |
| 审批 | http://localhost:8005/approval/doc.html |
| 分析 | http://localhost:8006/analytics/doc.html |
| 消息 | http://localhost:8007/notification/doc.html |
| 支付 | http://localhost:8008/payment/doc.html |
| 审计 | http://localhost:8009/audit/doc.html |

---

**祝使用愉快！如有问题，请查看各模块的源代码注释。**
