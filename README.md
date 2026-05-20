# 电商应用开发实验项目

这是一个基于 Spring Boot 和 MySQL 部署在 Docker 容器中的电商应用。

## 杨润淇202330452071

## 访问信息
* **演示地址**: http://8.134.207.192:8080/
* **测试账号**: `testuser`
* **测试密码**: `123456`

## 技术栈
* **后端**: Spring Boot, Spring Security, Hibernate
* **数据库**: MySQL 8.0
* **部署**: Docker, Docker-compose, 阿里云 ECS

## 功能实现
- [x] 用户登录验证（BCrypt 加密）
- [x] 商品数据展示（英文描述防止乱码）
- [x] Docker 容器化部署
- [x] 2GB Swap 内存优化