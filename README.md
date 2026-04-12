# StarRocks Admin - Java Edition

StarRocks 集群管理后台，使用 Spring Boot 3.x + Java 21 重写。

## 技术栈

- Java 21
- Spring Boot 3.4
- Spring Data JPA + SQLite
- Spring Security + JWT (jjwt)
- Casbin (RBAC)
- Flyway (数据库迁移)
- SpringDoc OpenAPI (Swagger)
- Lombok

## 快速开始

```bash
# 编译
mvn clean package -DskipTests

# 运行
java -jar target/starrocks-admin-1.0.0.jar

# 或使用 Docker
docker build -t starrocks-admin .
docker run -p 8080:8080 -v ./data:/app/data starrocks-admin
```

## 默认账号

- 用户名: `admin`
- 密码: `admin`

## API 文档

启动后访问: http://localhost:8080/api-docs

## 配置

编辑 `application.yml` 或通过环境变量:

| 环境变量 | 说明 | 默认值 |
|---------|------|--------|
| `JWT_SECRET` | JWT 密钥 | change-me-in-production-please |
| `SERVER_PORT` | 服务端口 | 8080 |
