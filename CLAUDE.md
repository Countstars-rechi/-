# ecommerce-demo — 课设完成状态

> 课程要求原始文件：[《网络应用开发》课程设计.pdf](《网络应用开发》课程设计.pdf)

---

## 0. 课设要求对照（全部完成）

| 课设要求 | 状态 |
|----------|------|
| 用户注册/登录/注销 | 已实现 |
| 未登录可浏览商品 | 已实现 |
| 浏览 → 购物车 → 付款 | 已实现 |
| 邮件确认 | 已实现 (模拟模式) |
| 库存扣减/恢复 | 已实现 |
| 订单取消 | 已实现 |
| 用户画像（地域/购买力/偏好） | 已实现 |
| 销售趋势（日/周/月）+ ECharts | 已实现 |
| 销售异常判别 | 已实现 |
| 商品排行榜 | 已实现 |
| 简单推荐"浏览过的人也买了" | 已实现 |
| 协同过滤推荐系统 (UserCF) | 已实现 |
| 销售人员：商品管理 (CRUD) | 已实现 |
| 销售人员：分类管理 | 已实现 |
| 销售人员：订单状态流转 | 已实现 |
| 销售人员：日志监控 | 已实现 |
| 管理员：销售员管理 | 已实现 |
| 管理员：密码重置 | 已实现 |
| 管理员：业绩查询/统计 + ECharts | 已实现 |
| 用户登录日志（时间/IP） | 已实现 |
| 浏览行为日志 | 已实现 |
| 操作日志 | 已实现 |
| 商品分页 | 已实现 |
| 用户个人中心 | 已实现 |
| 数据可视化大屏（ECharts） | 已实现 |
| 三角色权限隔离 (URL + 导航栏) | 已实现 |
| CSRF 防护 + 所有权校验 | 已实现 |

---

## 1. 技术栈

| 层次 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 2.7.18 |
| 安全框架 | Spring Security | 5.x |
| ORM | Spring Data JPA / Hibernate | 5.x |
| 数据库 | MySQL | 8.0 |
| 模板引擎 | Thymeleaf + thymeleaf-extras-springsecurity5 | 3.x |
| 前端 UI | Bootstrap 5 + ECharts (CDN) | 5.1.3 / 5.5 |
| 构建工具 | Maven | 3.8+ |
| 容器化 | Docker + Docker Compose | 3.8 |
| JDK | Java 17 | — |

---

## 2. 关键文件索引

| 用途 | 路径 |
|------|------|
| 启动类 | src/main/java/com/example/ecommerce/EcommerceApplication.java |
| 安全配置 | src/main/java/com/example/ecommerce/config/SecurityConfig.java |
| 数据初始化 | src/main/java/com/example/ecommerce/config/DataInitializer.java |
| 应用配置 | src/main/resources/application.properties |
| Docker 编排 | docker-compose.yml |
| Docker 镜像 | Dockerfile |

### Controller

| 文件 | 路由 | 功能 |
|------|------|------|
| LoginController.java | /login, /register | 登录注册 |
| ProductController.java | /products, /product/detail | 商品浏览（分页+搜索） |
| CartController.java | /cart/** | 购物车 |
| OrderController.java | /orders/** | 订单 |
| ProfileController.java | /profile/** | 个人中心 |
| SalesController.java | /sales/** | 销售员后台 |
| AdminController.java | /admin/** | 管理员后台 |

### Service

| 文件 | 职责 |
|------|------|
| UserService.java | 用户认证、注册、管理 |
| ProductService.java | 商品 CRUD、搜索、分页 |
| CartService.java | 购物车操作 |
| OrderService.java | 订单创建、支付、取消、统计、发货、完成 |
| AnalysisService.java | 用户画像、趋势、异常、协同过滤推荐、仪表盘 |
| LogService.java | 浏览日志、操作日志 |
| EmailService.java | 邮件发送（支持模拟模式） |

### Entity / Repository

| Entity | Repository | 表 |
|--------|-----------|-----|
| User.java | UserRepository.java | users |
| Product.java | ProductRepository.java | products |
| CartItem.java | CartItemRepository.java | cart_items |
| Order.java | OrderRepository.java | orders |
| OrderItem.java | OrderItemRepository.java | order_items |
| BrowseLog.java | BrowseLogRepository.java | browse_logs |
| OperationLog.java | OperationLogRepository.java | operation_logs |

---

## 3. 架构要点

### 三角色权限隔离

| 角色 | 首页 | 可访问路由 | 导航栏颜色 |
|------|------|-----------|-----------|
| CUSTOMER | /products | /cart/**, /orders/**, /profile/** | 蓝 bg-primary |
| SALES | /sales/dashboard | /sales/** | 绿 bg-success |
| ADMIN | /admin/dashboard | /admin/**, /sales/** | 红 bg-danger |

- 角色间严格 URL 隔离，越权返回 403
- 登录后按角色自动跳转不同首页
- 三个独立导航栏 fragment，无跨角色链接
- 订单/购物车操作有所有权校验

### 安全措施

- CSRF 防护已启用（/login, /register 豁免）
- 所有状态变更操作使用 POST（购物车删除、分类删除、商品下架等）
- 登出使用 POST 表单
- 密码 BCrypt 加密
- 所有 POST 表单通过 Thymeleaf `th:action` 自动注入 `_csrf` token

### 实体设计要点

- 所有 JPA 实体使用 `@Getter @Setter @NoArgsConstructor` + `@EqualsAndHashCode(onlyExplicitlyIncluded = true)`，仅基于 `id` 进行相等比较
- **不要使用 `@Data`**：双向关联（User↔CartItem, Order↔OrderItem）会导致 Hibernate 脏检查时 `equals()`/`hashCode()` 递归遍历整个对象图，引发 N+1 查询爆炸、堆内存爆满、GC 抖动

---

## 4. 邮箱配置

`app.email.enabled=false` 时为模拟模式（只打日志不发邮件）。

真实 SMTP 配置：
```properties
app.email.enabled=true
spring.mail.host=smtp.example.com
spring.mail.port=587
spring.mail.username=your-email@example.com
spring.mail.password=your-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

---

## 5. 快速命令

```bash
# 本地开发
docker-compose up -d mysql          # 启动 MySQL
./mvnw spring-boot:run              # 启动应用

# Docker 部署
docker-compose up -d --build        # 构建并启动全套服务

# 查看日志
docker-compose logs -f app          # 应用日志
docker-compose logs -f mysql        # 数据库日志
```

---

## 6. 部署信息

- **演示地址**: http://8.134.207.192:8080/
- **服务器**: 阿里云 ECS + Docker Compose
- **演示账号**:

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 销售员 | sales01 | sales123 |
| 顾客 | testuser | 123456 |
