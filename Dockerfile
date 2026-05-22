# 阶段1：构建 Jar 包（多阶段构建，减小镜像体积）
FROM maven:3.8.5-openjdk-17 AS build
# 设置工作目录
WORKDIR /app
# 复制 pom.xml 和源码
COPY pom.xml .
COPY src ./src
# 打包生成 Jar 包（跳过测试）
RUN mvn clean package -DskipTests

# 阶段2：运行 Jar 包
FROM openjdk:17-jdk-alpine
# 设置工作目录
WORKDIR /app
# 从构建阶段复制 Jar 包到当前镜像
COPY --from=build /app/target/ecommerce-1.0.0.jar app.jar
# 暴露端口（与 application.properties 中的 server.port 一致）
EXPOSE 8080
# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]
