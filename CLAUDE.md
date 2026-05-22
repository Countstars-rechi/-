# ecommerce-demo — 项目审计与开发指南

## 1. 项目概览

这是一个基于 Spring Boot 的电商教学演示系统，支持用户浏览商品、加入购物车、下单支付（模拟），以及管理员/销售人员的后台管理功能。项目采用 Docker 容器化部署在阿里云 ECS（Ubuntu 22.04, 4GB）。

- **访问地址**: http://8.134.207.192:8080/
- **测试账号**: `testuser` / `123456`
- **管理员账号**: `admin` / `admin123`
- **销售员账号**: `sales01` / `sales123`

---

## 2. 技术栈

| 层次 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 2.7.18 |
| 安全框架 | Spring Security | 5.x (随 Boot 2.7) |
| ORM | Spring Data JPA / Hibernate | 5.x |
| 数据库 | MySQL | 8.0 |
| 模板引擎 | Thymeleaf | 3.x |
| 前端 UI | Bootstrap 5 (CDN) | 5.1.3 |
| 构建工具 | Maven | 3.8+ |
| 容器化 | Docker + Docker Compose | 3.8 |
| JDK | 本地 Java 17 / Docker 镜像 JDK 8 (⚠️ 待修复) | — |

### Maven 依赖全景

```
spring-boot-starter-web          → REST + MVC
spring-boot-starter-thymeleaf    → 服务端模板渲染
spring-boot-starter-security     → 认证与授权
spring-boot-starter-data-jpa     → Hibernate ORM
mysql-connector-j                → MySQL 驱动
lombok                           → 注解简化 POJO
spring-boot-starter-test         → 测试框架
```

---

## 3. 数据库表结构（7 张表，JPA ddl-auto=update 自动建表）

### 3.1 `users` — 用户表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO | — |
| username | VARCHAR | UNIQUE, NOT NULL | 登录用户名 |
| password | VARCHAR | NOT NULL | BCrypt 加密密文 |
| full_name | VARCHAR | — | 昵称/姓名 |
| email | VARCHAR | — | 邮箱 |
| phone | VARCHAR | — | 电话 |
| region | VARCHAR | — | 地域（用于用户画像） |
| role | VARCHAR | NOT NULL | CUSTOMER / SALES / ADMIN |
| enabled | BOOLEAN | DEFAULT true | 账号启用状态 |
| created_at | DATETIME | — | 注册时间（自动填充） |
| last_login_at | DATETIME | — | 最后登录时间 |
| last_login_ip | VARCHAR | — | 最后登录 IP |

### 3.2 `products` — 商品表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO | — |
| name | VARCHAR | NOT NULL | 商品名称 |
| description | VARCHAR | — | 描述 |
| price | DECIMAL | NOT NULL | 价格 |
| stock | INT | — | 库存数量 |
| category | VARCHAR | — | 商品分类 |
| image_url | VARCHAR | — | 图片 URL |
| enabled | BOOLEAN | DEFAULT true | 上架状态 |

### 3.3 `cart_items` — 购物车表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO | — |
| user_id | BIGINT | FK → users | 用户 |
| product_id | BIGINT | FK → products | 商品 |
| quantity | INT | NOT NULL | 数量 |

### 3.4 `orders` — 订单表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO | — |
| order_no | VARCHAR | UNIQUE, NOT NULL | 订单号 ORD+时间戳 |
| user_id | BIGINT | FK → users | 下单用户 |
| total_amount | DECIMAL | NOT NULL | 总金额 |
| status | VARCHAR | NOT NULL | PENDING / PAID / SHIPPED / COMPLETED / CANCELLED |
| shipping_address | VARCHAR | — | 收货地址 |
| receiver_name | VARCHAR | — | 收货人 |
| receiver_phone | VARCHAR | — | 收货人电话 |
| created_at | DATETIME | — | 下单时间（自动） |
| paid_at | DATETIME | — | 付款时间 |

### 3.5 `order_items` — 订单项表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO | — |
| order_id | BIGINT | FK → orders | 订单 |
| product_id | BIGINT | FK → products | 商品 |
| quantity | INT | NOT NULL | 数量 |
| price | DECIMAL | NOT NULL | 购买时单价 |

### 3.6 `browse_logs` — 浏览日志表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO | — |
| user_id | BIGINT | FK → users | 用户 |
| product_id | BIGINT | FK → products, nullable | 浏览的商品 |
| category | VARCHAR | — | 商品类别 |
| stay_duration | INT | — | 停留时长（秒） |
| ip_address | VARCHAR | — | 客户端 IP |
| browse_time | DATETIME | — | 浏览时间（自动） |

### 3.7 `operation_logs` — 操作日志表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO | — |
| username | VARCHAR | NOT NULL | 操作人 |
| role | VARCHAR | NOT NULL | 操作人角色 |
| operation | VARCHAR | NOT NULL | 操作内容描述 |
| ip_address | VARCHAR | — | 操作 IP |
| operation_time | DATETIME | — | 操作时间（自动） |

---

## 4. 现有核心逻辑

### 4.1 认证与授权（Spring Security）

**配置类**: [SecurityConfig.java](src/main/java/com/example/ecommerce/config/SecurityConfig.java)

```
/login, /register, /css/**, /js/**, /images/**  → 公开访问
/sales/**                                        → SALES 或 ADMIN 角色
/admin/**                                        → 仅 ADMIN 角色
其他所有请求                                       → 需登录
```

- 登录页: `/login`，登录成功跳转 `/products`
- 登出: `/logout`，登出后跳转 `/login?logout`
- CSRF: 已全局关闭（`.csrf().disable()`）

### 4.2 密码加密方式

使用 **BCryptPasswordEncoder**（Spring Security 提供），加密强度为默认的 10 轮（$2a$10$...）。

- 初始数据通过 `DataInitializer.java`（CommandLineRunner）在应用启动时自动创建
- 初始化条件: `app.init-data=true` 且对应表为空
- `data.sql` 中的 INSERT 语句与 DataInitializer 功能重复（冗余，建议删除）

### 4.3 用户角色体系

| 角色 | 常量 | 说明 | 访问范围 |
|------|------|------|----------|
| 普通用户 | CUSTOMER | 前台消费者 | /products, /cart, /orders 等 |
| 销售人员 | SALES | 商品/订单管理 | /sales/** |
| 管理员 | ADMIN | 系统管理 | /admin/**, /sales/** |

⚠️ **已知 BUG**: `DataInitializer.java:99` 将 testuser 的角色设为 `"USER"` 而非 `"CUSTOMER"`。这与 Entity 注释约定和 `registerUser()` 方法中的 `"CUSTOMER"` 不一致，但不影响 Spring Security 鉴权（因 SecurityConfig 只检查 SALES/ADMIN，其他登录用户都能访问普通页面）。

### 4.4 核心业务流程

**浏览 → 购物车 → 下单 → 支付**:

1. 用户访问 `/products`，可按分类筛选或关键词搜索
2. 点击商品进入 `/product/detail?id=X`，可看到推荐商品
3. 加入购物车 `/cart/add`（POST），数量默认为 1
4. 查看购物车 `/cart`，可删除单项
5. 结算 `/cart/checkout`，填写收货信息，提交后生成订单（状态: PENDING）
6. 在订单详情中点击"立即付款"，模拟支付（状态 → PAID）

**浏览日志**: 每次访问 `/products` 和 `/product/detail` 都会记录用户浏览行为，用于后续用户画像分析。

**操作日志**: 销售人员/管理员的所有管理操作均被记录。

### 4.5 推荐系统

简单协同过滤：基于"购买过此商品的用户还买了什么"，在商品详情页展示推荐。

### 4.6 数据初始化

启动时通过 `DataInitializer.java` 自动插入 12 个商品（手机、平板、耳机、电脑、配件 5 个类别）和 3 个用户（admin, sales01, testuser）。条件：`app.init-data=true` 且对应表为空。

---

## 5. 当前问题清单（Bug + 待优化）

### 5.1 🔴 严重问题

| # | 问题 | 位置 | 影响 |
|---|------|------|------|
| 1 | **JDK 版本不一致** | `pom.xml:22` 设 `<java.version>1.8</java.version>`，`Dockerfile:2` 用 `maven:3.8.5-openjdk-8`，`Dockerfile:12` 用 `openjdk:8-jdk-alpine` | 本地 Java 17 环境与 Docker 镜像 JDK 8 不匹配，可能导致编译失败或运行时异常 |
| 2 | **testuser 角色字段错误** | `DataInitializer.java:99` 设 `role="USER"` | 与 Entity 约定不一致（应为 CUSTOMER），虽不影响鉴权但导致数据语义混乱 |
| 3 | **data.sql 与 DataInitializer 冲突风险** | `data.sql` 硬编码了 BCrypt 哈希，与 DataInitializer 的编码逻辑重复 | JPA ddl-auto=update 时 data.sql 先执行（若表不存在会报错），然后 DataInitializer 再次检查 count>0 跳过。两套初始化逻辑存在竞态 |

### 5.2 🟡 功能缺失

| # | 缺失功能 | 说明 |
|---|----------|------|
| 1 | **库存扣减** | 下单/支付时未扣减 `Product.stock`，库存字段形同虚设 |
| 2 | **订单取消** | 有 CANCELLED 状态枚举但无对应的取消接口/按钮 |
| 3 | **用户个人中心** | 无法查看/编辑个人资料，无法修改密码 |
| 4 | **商品图片上传** | imageUrl 仅支持手动填 URL，无文件上传功能 |
| 5 | **分页** | 商品列表无分页，数据量大时性能堪忧 |
| 6 | **订单状态流转管理** | 销售/管理员后台无法修改订单状态（如确认发货） |
| 7 | **商品分类管理** | 无独立的分类 CRUD，只能在新加商品时填写 |
| 8 | **表单校验** | 前端和后端都缺少充分的参数校验（如密码强度、手机号格式） |
| 9 | **无测试代码** | `src/test/` 目录不存在，无单元测试或集成测试 |

### 5.3 🟢 优化建议

| # | 建议 | 说明 |
|---|------|------|
| 1 | 删除 `data.sql` | 与 DataInitializer 功能完全重复 |
| 2 | 抽取公共导航栏 | 每个模板重复导航栏 HTML，可用 Thymeleaf fragment |
| 3 | 添加 `global-error.html` | 目前无统一错误页面 |
| 4 | 日志框架升级 | 建议使用 SLF4J 记录关键操作而非仅靠 `show-sql=true` |

---

## 6. 部署架构

### 6.1 Docker Compose 拓扑

```
ecommerce-network (bridge)
├── ecommerce-mysql (mysql:8.0)
│   - 端口: 3306
│   - 数据卷: mysql-data → /var/lib/mysql
│   - 环境: MYSQL_ROOT_PASSWORD=root123, MYSQL_DATABASE=ecommerce_db
│
└── ecommerce-app (自构建)
    - 端口: 8080
    - 依赖: mysql 先启动
    - 构建: 基于 Dockerfile（多阶段构建）
```

### 6.2 Dockerfile 流程

```
阶段 1 (build): maven:3.8.5-openjdk-8 → mvn clean package -DskipTests
阶段 2 (run):   openjdk:8-jdk-alpine  → java -jar app.jar
```

⚠️ **需要将两阶段的 JDK 版本都升级到 17**。

### 6.3 配置文件

所有敏感配置（数据库密码等）写在 [application.properties](src/main/resources/application.properties) 中，通过 Docker Compose 的 `environment` 覆盖 `SPRING_DATASOURCE_URL/USERNAME/PASSWORD`。

---

## 7. 快速启动（本地开发）

```bash
# 1. 启动 MySQL
docker-compose up -d mysql

# 2. 启动 Spring Boot（使用 Maven wrapper 或 IDE）
./mvnw spring-boot:run

# 3. 访问 http://localhost:8080/
# 测试账号: testuser / 123456
```

如需完全 Docker 化运行：`docker-compose up -d --build`

---

## 8. 关键文件索引

| 用途 | 路径 |
|------|------|
| 启动类 | [EcommerceApplication.java](src/main/java/com/example/ecommerce/EcommerceApplication.java) |
| 安全配置 | [SecurityConfig.java](src/main/java/com/example/ecommerce/config/SecurityConfig.java) |
| 数据初始化 | [DataInitializer.java](src/main/java/com/example/ecommerce/config/DataInitializer.java) |
| 应用配置 | [application.properties](src/main/resources/application.properties) |
| Docker 编排 | [docker-compose.yml](docker-compose.yml) |
| Docker 镜像 | [Dockerfile](Dockerfile) |
| SQL 种子数据 | [data.sql](src/main/resources/data.sql) |
