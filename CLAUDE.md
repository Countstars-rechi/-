# ecommerce-demo — 课设完善计划

> 课程要求原始文件：[《网络应用开发》课程设计.pdf](《网络应用开发》课程设计.pdf)

---

## 0. 课设要求对照

| 课设要求 | 当前状态 | 待完善 |
|----------|---------|--------|
| 用户注册/登录/注销 | 已实现 | — |
| 未登录可浏览商品 | 已实现 (permitAll) | — |
| 浏览 → 购物车 → 付款 | 已实现 | — |
| 邮件确认 | 已实现 (模拟模式) | 可选：配置真实 SMTP |
| 库存扣减/恢复 | 已实现 | — |
| 订单取消 | 已实现 | — |
| 用户画像（地域/购买力/偏好） | 已实现 | — |
| 销售趋势（日/周/月） | 数据层已实现 | **缺图表可视化** |
| 销售异常判别 | 已实现 | — |
| 商品排行榜 | 已实现 | — |
| 简单推荐"浏览过的人也买了" | 已实现 | — |
| **协同过滤推荐系统** | **未实现** | **阶段4** |
| 销售人员：商品管理 | 已实现 (CRUD) | — |
| 销售人员：**分类管理** | **未实现** | **阶段2** |
| 销售人员：**订单状态流转** | **未实现** | **阶段2** |
| 销售人员：日志监控 | 已实现 | — |
| 管理员：销售员管理 | 已实现 | — |
| 管理员：密码重置 | 已实现 | — |
| 管理员：业绩查询/统计 | 已实现 | **缺图表可视化** |
| 用户登录日志（时间/IP） | 已实现 (last_login_at/ip) | — |
| 浏览行为日志 | 已实现 | — |
| 操作日志 | 已实现 | — |
| 商品分页 | **未实现** | **阶段5** |
| 用户个人中心 | **未实现** | **阶段5** |
| **数据可视化大屏**（ECharts） | **未实现** | **阶段3** |

---

## 1. 技术栈

| 层次 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 2.7.18 |
| 安全框架 | Spring Security | 5.x |
| ORM | Spring Data JPA / Hibernate | 5.x |
| 数据库 | MySQL | 8.0 |
| 模板引擎 | Thymeleaf | 3.x |
| 前端 UI | Bootstrap 5 + ECharts (CDN) | 5.1.3 / 5.x |
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
| ProductController.java | /products, /product/detail | 商品浏览 |
| CartController.java | /cart/** | 购物车 |
| OrderController.java | /orders/** | 订单 |
| SalesController.java | /sales/** | 销售员后台 |
| AdminController.java | /admin/** | 管理员后台 |

### Service

| 文件 | 职责 |
|------|------|
| UserService.java | 用户认证、注册、管理 |
| ProductService.java | 商品 CRUD、搜索 |
| CartService.java | 购物车操作 |
| OrderService.java | 订单创建、支付、取消、统计 |
| AnalysisService.java | 用户画像、趋势、异常、推荐、仪表盘 |
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

## 3. 实施计划

### 阶段1：修复已知Bug & 清理冗余 ✓

1. 删除 `src/main/resources/data.sql`（与 DataInitializer 重复）
2. 抽取 Thymeleaf 公共导航栏 fragment
3. 添加全局错误页面 `error.html`
4. 本地开发数据库配置支持（默认连 Docker MySQL，增加本地 profile）

### 阶段2：分类管理 & 订单状态流转 ✓

1. **分类管理**（Sales 后台）
   - `SalesController` 增加分类列表、添加、删除
   - 分类列表页 `sales/categories.html`
   - 删除分类时检查是否有关联商品

2. **订单状态流转**（Sales 后台）
   - 订单列表添加"发货"按钮（PENDING/PAID → SHIPPED）
   - 订单列表添加"完成"按钮（SHIPPED → COMPLETED）
   - 记录操作日志
   - 库存不足时不允许发货？不，发货不涉及库存变更，只是状态变更

### 阶段3：ECharts 数据可视化 ✓

1. **Admin Dashboard** (`admin/dashboard.html`)
   - 近7天销售趋势折线图（已有 `trend` 数据）
   - 各类别销售占比饼图（需新增查询）
   - 异常标记（已有 anomalies 数据）

2. **Admin 销售业绩页** (`admin/sales-performance.html`)
   - 近30天销售趋势图
   - 商品排行榜柱状图（已有 ranking 数据）

3. **Sales 统计页** (`sales/statistics.html`)
   - 销售趋势折线图（已有 trend 数据）
   - 类别销售柱状图（已有 categorySales 数据）

4. **公共引入**
   - ECharts CDN in layout fragment
   - 统一图表渲染工具函数

### 阶段4：协同过滤推荐系统 ✓

1. **基于用户的协同过滤（UserCF）**
   - 计算用户间相似度（基于共同购买的商品）
   - 为目标用户推荐相似用户购买过的商品
   - 在商品详情页展示

2. **在 AnalysisService 中重构**
   - 保留现有简单推荐作为 fallback
   - 新增 `getCollaborativeFilteringRecommendations(Long userId, int limit)` 方法
   - 冷启动问题：新用户回退到简单推荐

### 阶段5：商品分页 & 用户体验优化 ✓

1. **商品列表分页**
   - ProductController 支持 page/size 参数
   - 分页导航条（Bootstrap Pagination）

2. **"立即购买"按钮**
   - 商品详情页增加"立即购买"
   - 跳转到简化结算页或直接下单

3. **用户个人中心**
   - `/profile` 查看个人信息
   - `/profile/edit` 编辑邮箱、电话、地域
   - `/profile/change-password` 修改密码

4. **前端样式优化**
   - 购物车数量 badge
   - 订单状态标签颜色
   - 首页分类导航优化

---

## 4. 邮箱配置说明

`app.email.enabled=false` 时为模拟模式（只打日志不发邮件）。

真实 SMTP 配置（在 `application.properties` 中修改）：
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
