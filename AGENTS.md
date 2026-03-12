# AGENTS.md - Coding Guidelines for AI Agents

## Project Overview

Spring Boot 3.x + React 18 + JWT 认证系统，前后端分离架构。

- **后端**: Java 17, Spring Boot 3.2+, Spring Security, JPA, MySQL
- **前端**: React 18, TypeScript, Vite 5, Ant Design 5, Zustand

## Build/Test Commands

### 后端 (Maven)
```bash
cd backend
mvn clean install          # 编译并打包
mvn spring-boot:run        # 启动开发服务
mvn test                   # 运行所有测试
mvn test -Dtest=ClassName  # 运行单个测试类
mvn clean package          # 生产打包 (target/auth-demo-1.0.0.jar)
```

### 前端 (npm)
```bash
cd frontend
npm install                # 安装依赖
npm run dev                # 启动开发服务 (port 3000)
npm run build              # 生产构建
npm run preview            # 预览生产构建
```

**注意**: 前端没有配置 ESLint/Prettier 或测试框架。

## 代码风格规范

### Java (后端)

**命名规范**
- 类名: PascalCase (AuthController, UserService)
- 方法/变量: camelCase (getUserById, userName)
- 常量: UPPER_SNAKE_CASE
- 包名: 全小写 (com.example.service)

**代码组织**
- 使用 Lombok 注解: @Data, @RequiredArgsConstructor, @Builder
- 构造函数注入优先于 @Autowired 字段注入
- DTO 使用 Bean Validation (@NotBlank, @Size)
- Service 层使用 @Transactional

**示例**:
```java
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(RegisterRequest request) {
        // 实现
    }
}
```

**错误处理**
- Service 层抛出自定义 RuntimeException
- Controller 返回 ResponseEntity<T>
- 使用 try-catch 包装认证异常

### TypeScript/React (前端)

**命名规范**
- 组件: PascalCase (LoginForm, PrivateRoute)
- 函数/变量: camelCase (handleSubmit, isLoading)
- 接口: PascalCase (LoginRequest, User)
- 文件: PascalCase 用于组件，camelCase 用于工具

**导入顺序**
1. React 内置
2. 第三方库 (antd, react-router-dom)
3. 本地组件/页面
4. 工具/stores

**代码风格**
- 严格 TypeScript (strict: true)
- 使用函数组件 + Hooks
- 类型定义使用 interface
- 组件 props 使用解构赋值

**示例**:
```typescript
import React from 'react'
import { Button, Form } from 'antd'
import { useAuthStore } from '../stores/authStore'

interface LoginFormProps {
  onSuccess: () => void
}

const LoginForm: React.FC<LoginFormProps> = ({ onSuccess }) => {
  const [loading, setLoading] = React.useState(false)
  // ...
}
```

## 项目结构

```
crab_station/
├── backend/
│   ├── src/main/java/com/example/
│   │   ├── config/        # 配置类 (Security, Web)
│   │   ├── controller/    # REST API 控制器
│   │   ├── service/       # 业务逻辑层
│   │   ├── repository/    # JPA 数据访问层
│   │   ├── entity/        # 数据库实体
│   │   ├── dto/           # 数据传输对象
│   │   └── security/      # JWT 过滤器/工具
│   └── src/main/resources/
│       └── application.yml
└── frontend/
    ├── src/
    │   ├── pages/         # 页面组件 (Login, Home)
    │   ├── components/    # 可复用组件
    │   ├── api/           # API 请求封装
    │   ├── stores/        # Zustand 状态管理
    │   └── utils/         # 工具函数
    └── index.html
```

## API 规范

**基础路径**: `/api`

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 登录 | POST | /api/auth/login | 用户名密码登录 |
| 注册 | POST | /api/auth/register | 用户注册 |
| 获取用户 | GET | /api/auth/me | 获取当前登录用户 |
| 登出 | POST | /api/auth/logout | 退出登录 |

**认证方式**: Bearer Token (JWT)
- 登录后获取 token
- 存储于 localStorage (Zustand persist)
- 请求头: `Authorization: Bearer {token}`

## 依赖规范

**前端主要依赖**:
- react ^18.2.0
- react-router-dom ^6.20.0
- antd ^5.12.0
- axios ^1.6.2
- zustand ^4.4.7

**后端主要依赖**:
- Spring Boot 3.2.0
- Spring Security
- Spring Data JPA
- JWT (jjwt 0.12.3)
- MySQL Connector
- Lombok

## 数据库配置

MySQL:
- 数据库: crab_db
- 用户名: root
- 密码: 123456
- 端口: 3306

## 开发环境

- 后端服务: http://localhost:8080
- 前端服务: http://localhost:3000
- Vite 代理: /api → http://localhost:8080
