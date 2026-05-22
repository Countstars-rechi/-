# 仅运行阶段（JAR 由宿主机 Maven 编译，Docker 只负责运行）
FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY target/ecommerce-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -Xms256m -Xmx512m -XX:+UseG1GC -jar app.jar"]
