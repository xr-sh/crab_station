# Spring Boot + React 工作站

一个完整的前后端分离工作站系统，采用 Spring Boot 3.x + React 18 + JWT 认证。

## 项目结构

```
project-root/
├── backend/          # Spring Boot 后端
└── frontend/         # React 前端
```

## 技术栈

### 后端
- Spring Boot 3.2+
- Spring Security
- Spring Data JPA
- JWT (JSON Web Token)
- MySQL 8.0
- Maven

### 前端
- React 18
- TypeScript
- Vite 5
- Ant Design 5
- Zustand (状态管理)
- Axios
- React Router v6

## 快速开始

### 1. 数据库准备

创建 MySQL 数据库：

```sql
CREATE DATABASE crab_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

配置已设置好：
- 数据库名：`crab_db`
- 用户名：`root`
- 密码：`123456`

### 2. 启动后端

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端服务将在 `http://localhost:3000` 启动

## API 接口

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 登录 | POST | `/api/auth/login` | 用户名密码登录 |
| 注册 | POST | `/api/auth/register` | 用户注册 |
| 登出 | POST | `/api/auth/logout` | 退出登录 |
| 获取用户 | GET | `/api/auth/me` | 获取当前登录用户 |

## 功能特性

- [x] 用户注册/登录
- [x] JWT Token 认证
- [x] 密码加密存储 (BCrypt)
- [x] 登录状态持久化
- [x] 路由守卫保护
- [x] 响应式设计
- [x] 表单验证

## 默认测试账号

注册一个新用户即可测试，或使用以下 SQL 插入测试数据：

```sql
INSERT INTO users (username, password, email, status, created_at, updated_at) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', 'admin@example.com', 1, NOW(), NOW());
```

密码: `admin123` (BCrypt加密后的值)

## 开发环境要求

- Java 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.8+

## 部署说明

### 后端打包

```bash
cd backend
mvn clean package
```

生成的 jar 包在 `target/auth-demo-1.0.0.jar`

### 前端打包

```bash
cd frontend
npm run build
```

构建产物在 `dist/` 目录

## 注意事项

1. 生产环境请修改 JWT secret
2. 建议使用 HTTPS
3. 数据库密码请使用复杂密码
4. 定期更新依赖版本
