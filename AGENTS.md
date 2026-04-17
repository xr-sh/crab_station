# AGENTS.md - Coding Guidelines for AI Agents

## Project Overview

Spring Boot 3.x + React 18 + JWT 认证系统，前后端分离架构，包含快递分析、进货管理、财务管理、规格映射等业务模块。

- **后端**: Java 17, Spring Boot 3.2+, Spring Security, JPA, MySQL, JWT, EasyExcel
- **前端**: React 18, TypeScript, Vite 5, Ant Design 5, Zustand, React Router v6

### 核心业务模块

| 模块 | 描述 |
|------|------|
| Auth | 用户认证（登录、注册、JWT Token） |
| Users | 用户管理（CRUD、状态控制） |
| Finance | 财务记录管理 |
| Purchase | 进货管理（主记录 + 明细） |
| Express | 快递分析（Excel 导入、动态字段解析） |
| PlatformSpec | 平台规格管理 |
| PurchaseSpec | 进货规格管理 |
| SpecificationMapping | 规格映射关系 |
| ApiConfig | API 配置管理（平台接口密钥配置） |

## Build/Test Commands

### 后端 (Maven)
```bash
cd backend
mvn clean install          # 编译并打包
mvn spring-boot:run        # 启动开发服务 (port 8080)
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
- Controller 返回 ResponseEntity<T> 或统一响应包装

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

### 后端结构
```
backend/
├── src/main/java/com/example/
│   ├── config/              # 配置类 (SecurityConfig, WebConfig)
│   ├── controller/          # REST API 控制器 (9个)
│   │   ├── AuthController.java
│   │   ├── UserController.java
│   │   ├── FinanceController.java
│   │   ├── ExpressController.java
│   │   ├── PurchaseController.java
│   │   ├── PlatformSpecController.java
│   │   ├── PurchaseSpecController.java
│   │   ├── SpecificationMappingController.java
│   │   └── ApiConfigController.java
│   ├── service/             # 业务逻辑层 (11+)
│   │   ├── AuthService.java
│   │   ├── UserService.java
│   │   ├── FinanceRecordService.java
│   │   ├── ExpressAnalysisService.java
│   │   ├── ExcelImportService.java
│   │   ├── PurchaseService.java
│   │   ├── PurchaseSpecService.java
│   │   ├── PlatformSpecService.java
│   │   ├── SpecificationMappingService.java
│   │   ├── ApiConfigService.java
│   │   └── CustomUserDetailsService.java
│   ├── repository/          # JPA 数据访问层
│   ├── entity/              # 数据库实体 (9个)
│   │   ├── User.java
│   │   ├── FinanceRecord.java
│   │   ├── ExpressAnalysis.java
│   │   ├── PurchaseRecord.java
│   │   ├── PurchaseItem.java
│   │   ├── PurchaseSpec.java
│   │   ├── PlatformSpec.java
│   │   ├── SpecificationMapping.java
│   │   └── ApiConfig.java
│   ├── dto/                 # 数据传输对象 (32+)
│   │   ├── LoginRequest.java / LoginResponse.java
│   │   ├── RegisterRequest.java
│   │   ├── UserDTO.java / CreateUserRequest.java / UpdateUserRequest.java
│   │   ├── FinanceRecordDTO.java / CreateFinanceRecordRequest.java / UpdateFinanceRecordRequest.java
│   │   ├── ExpressAnalysisDTO.java / ImportResultDTO.java
│   │   ├── PurchaseRecordDTO.java / PurchaseItemDTO.java
│   │   ├── PurchaseSpecDTO.java / PlatformSpecDTO.java / SpecificationMappingDTO.java
│   │   ├── ApiConfigDTO.java / CreateApiConfigRequest.java / UpdateApiConfigRequest.java
│   │   ├── PageRequest.java / PageResponse.java
│   │   └── ...Request/Response 对象
│   └── security/            # JWT 过滤器/工具
│       ├── JwtAuthenticationFilter.java
│       ├── JwtTokenProvider.java
│       └── CustomUserDetailsService.java
│   └── exception/           # 自定义异常
└── src/main/resources/
    └── application.yml      # 配置文件
```

### 前端结构
```
frontend/
├── src/
│   ├── main.tsx             # 应用入口
│   ├── App.tsx              # 路由配置入口
│   ├── pages/               # 页面组件 (10个)
│   │   ├── Home/            # 首页（欢迎页、统计卡片）
│   │   ├── Login/           # 登录/注册页
│   │   ├── Users/           # 用户管理页 + UserModal
│   │   ├── Finance/         # 财务管理页 + FinanceModal
│   │   ├── Purchase/        # 进货管理页 + PurchaseModal
│   │   ├── Express/         # 快递分析页 + ImportModal
│   │   ├── PurchaseSpec/    # 进货规格页 + PurchaseSpecModal
│   │   ├── PlatformSpec/    # 平台规格页 + PlatformSpecModal
│   │   ├── SpecificationMapping/ # 规格映射页 + MappingModal
│   │   └── ApiConfig/       # API配置页 + ApiConfigModal
│   ├── components/          # 可复用组件
│   │   ├── MainLayout/      # 主布局（侧边导航、头部、Outlet）
│   │   └── PrivateRoute/    # 路由守卫
│   ├── api/                 # API 请求封装 (10个)
│   │   ├── request.ts       # Axios 实例 + 拦截器
│   │   ├── auth.ts          # 登录/注册/登出/me
│   │   ├── users.ts         # 用户 CRUD
│   │   ├── finance.ts       # 财务 CRUD
│   │   ├── purchase.ts      # 进货 CRUD
│   │   ├── express.ts       # 快递导入/列表/统计
│   │   ├── purchaseSpec.ts  # 进货规格 CRUD
│   │   ├── platformSpec.ts  # 平台规格 CRUD
│   │   ├── specificationMapping.ts # 规格映射 CRUD
│   │   └── apiConfig.ts     # API配置 CRUD
│   ├── stores/              # Zustand 状态管理
│   │   └── authStore.ts     # token、user、isAuthenticated、持久化
│   └── utils/               # 工具函数
│       └── storage.ts       # localStorage 工具
├── vite.config.ts           # Vite 配置 + 代理
├── package.json             # 依赖版本
└── index.html
```

### 前端路由结构
```
/login                       # 登录/注册页（公开）
/                            # 受保护路由（需登录）
  ├── /                      # Home 首页
  ├── /users                 # Users 用户管理
  ├── /finance               # Finance 财务管理
  ├── /purchase              # Purchase 进货管理
  ├── /express               # Express 快递分析
  ├── /purchase-spec         # PurchaseSpec 进货规格
  ├── /platform-spec         # PlatformSpec 平台规格
  ├── /specification-mapping # SpecificationMapping 规格映射
  ├── /api-config            # ApiConfig API配置
```

## API 规范

**基础路径**: `/api`
**认证方式**: Bearer Token (JWT)

### Auth API (`/api/auth`)
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /login | 用户名密码登录，返回 JWT |
| POST | /register | 用户注册 |
| GET | /me | 获取当前登录用户信息 |
| POST | /logout | 退出登录 |

### Users API (`/api/users`)
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | / | 分页查询用户列表 |
| GET | /{id} | 获取单个用户 |
| POST | / | 创建用户 |
| PUT | /{id} | 更新用户 |
| DELETE | /{id} | 删除用户 |
| PATCH | /{id}/status | 更新用户状态 |

### Finance API (`/api/finance`)
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | / | 分页查询财务记录 |
| GET | /{id} | 获取单条财务记录 |
| POST | / | 创建财务记录 |
| PUT | /{id} | 更新财务记录 |
| DELETE | /{id} | 删除财务记录 |

### Express API (`/api/express`)
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /import | 导入 Excel 文件 |
| GET | / | 分页查询快递分析数据 |
| GET | /{id} | 获取单条快递分析 |
| GET | /columns | 获取动态字段列名 |
| GET | /file-names | 获取已导入文件名列表 |
| GET | /categories | 获取类别统计 |
| DELETE | /{id} | 删除单条快递分析 |
| DELETE | /clear | 清空所有快递分析数据 |
| DELETE | /file/{fileName} | 按文件名删除数据 |
| GET | /statistics | 获取统计信息 |

### Purchase API (`/api/purchases`)
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | / | 分页查询进货记录 |
| GET | /{id} | 获取单条进货记录（含明细） |
| POST | / | 创建进货记录（含明细） |
| PUT | /{id} | 更新进货记录 |
| DELETE | /{id} | 删除进货记录 |

### PlatformSpec API (`/api/platform-specs`)
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | / | 分页查询平台规格 |
| GET | /all | 获取所有平台规格（不分页） |
| GET | /{id} | 获取单个平台规格 |
| POST | / | 创建平台规格 |
| PUT | /{id} | 更新平台规格 |
| DELETE | /{id} | 删除平台规格 |

### PurchaseSpec API (`/api/purchase-specs`)
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | / | 分页查询进货规格 |
| GET | /all | 获取所有进货规格（不分页） |
| GET | /{id} | 获取单个进货规格 |
| POST | / | 创建进货规格 |
| PUT | /{id} | 更新进货规格 |
| DELETE | /{id} | 删除进货规格 |

### SpecificationMapping API (`/api/specification-mappings`)
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | / | 分页查询规格映射 |
| GET | /{id} | 获取单个规格映射 |
| POST | / | 创建规格映射 |
| PUT | /{id} | 更新规格映射 |
| DELETE | /{id} | 删除规格映射 |

### ApiConfig API (`/api/api-configs`)
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | / | 分页查询 API 配置 |
| GET | /all | 获取所有启用的 API 配置（不分页） |
| GET | /{id} | 获取单个 API 配置 |
| GET | /platform/{platformName} | 按平台名称获取 API 配置 |
| POST | / | 创建 API 配置 |
| PUT | /{id} | 更新 API 配置 |
| DELETE | /{id} | 删除 API 配置 |

### 分页参数
```
GET /api/xxx?page=0&size=10&sortBy=createdAt&sortDir=desc
```

### 认证头
```
Authorization: Bearer {token}
```

## 数据模型

### 实体 (Entity)

#### User
| 字段 | 类型 | 描述 |
|------|------|------|
| id | UUID | 主键 |
| username | String | 用户名（唯一） |
| password | String | 密码（BCrypt 加密） |
| email | String | 邮箱（唯一） |
| phone | String | 电话 |
| avatar | String | 头像 URL |
| status | Integer | 状态 (0=禁用, 1=启用) |
| lastLoginTime | LocalDateTime | 最后登录时间 |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

#### FinanceRecord
| 字段 | 类型 | 描述 |
|------|------|------|
| id | UUID | 主键 |
| recordDate | LocalDate | 记录日期 |
| amount | BigDecimal | 金额 |
| type | String | 类型（收入/支出） |
| remark | String | 备注 |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

#### ExpressAnalysis
| 字段 | 类型 | 描述 |
|------|------|------|
| id | UUID | 主键 |
| category | String | 类别 |
| fileName | String | 来源文件名 |
| sheetName | String | Excel Sheet 名 |
| rowNum | Integer | 行号 |
| duration | String | 时长描述 |
| durationHours | Integer | 时长（小时） |
| dynamicFields | String | JSON 动态字段 |
| importedAt | LocalDateTime | 导入时间 |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

#### PurchaseRecord（进货主记录）
| 字段 | 类型 | 描述 |
|------|------|------|
| id | UUID | 主键 |
| purchaseDate | LocalDate | 进货日期 |
| supplier | String | 供应商 |
| totalWeight | BigDecimal | 总重量 |
| totalAmount | BigDecimal | 总金额 |
| remark | String | 备注 |
| items | List<PurchaseItem> | 进货明细列表 |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

#### PurchaseItem（进货明细）
| 字段 | 类型 | 描述 |
|------|------|------|
| id | UUID | 主键 |
| purchase | PurchaseRecord | 关联进货主记录 |
| purchaseSpec | PurchaseSpec | 关联进货规格 |
| weight | BigDecimal | 重量 |
| unitPrice | BigDecimal | 单价 |
| amount | BigDecimal | 金额 |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

#### PurchaseSpec（进货规格）
| 字段 | 类型 | 描述 |
|------|------|------|
| id | UUID | 主键 |
| name | String | 规格名称 |
| status | Integer | 状态 |
| remark | String | 备注 |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

#### PlatformSpec（平台规格）
| 字段 | 类型 | 描述 |
|------|------|------|
| id | UUID | 主键 |
| name | String | 规格名称 |
| status | Integer | 状态 |
| remark | String | 备注 |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

#### SpecificationMapping（规格映射）
| 字段 | 类型 | 描述 |
|------|------|------|
| id | UUID | 主键 |
| purchaseSpec | PurchaseSpec | 关联进货规格 |
| platformSpec | PlatformSpec | 关联平台规格 |
| status | Integer | 状态 |
| remark | String | 备注 |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

#### ApiConfig（API配置）
| 字段 | 类型 | 描述 |
|------|------|------|
| id | UUID | 主键 |
| platformName | String | 平台名称（如京东、淘宝） |
| apiKey | String | API Key（如 appKey、apiKey） |
| secret | String | Secret/Token（如 appSecret） |
| baseUrl | String | API 接口基础地址 |
| dynamicConfig | String | JSON 动态字段（存储平台特定配置） |
| status | Integer | 状态 |
| remark | String | 备注 |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

### 主要 DTO

#### LoginResponse
```json
{
  "token": "jwt_token",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "id": "uuid",
    "username": "string",
    "email": "string",
    "avatar": "string"
  }
}
```

#### ImportResultDTO（Excel 导入结果）
```json
{
  "category": "string",
  "fileName": "string",
  "totalRows": 100,
  "successRows": 95,
  "failedRows": 5,
  "columns": ["col1", "col2"],
  "errors": ["error1", "error2"],
  "success": true,
  "message": "string"
}
```

#### PageResponse<T>（通用分页响应）
```json
{
  "content": [...],
  "totalElements": 100,
  "totalPages": 10,
  "size": 10,
  "number": 0,
  "first": true,
  "last": false
}
```

## 依赖版本

### 前端主要依赖 (package.json)
| 依赖 | 版本 |
|------|------|
| react | ^18.2.0 |
| react-dom | ^18.2.0 |
| react-router-dom | ^6.20.0 |
| antd | ^5.12.0 |
| @ant-design/icons | ^5.2.6 |
| axios | ^1.6.2 |
| zustand | ^4.4.7 |
| dayjs | ^1.11.10 |
| typescript | ^5.2.2 |
| vite | ^5.0.0 |
| @vitejs/plugin-react | ^4.2.0 |

### 后端主要依赖 (pom.xml)
| 依赖 | 版本 |
|------|------|
| Spring Boot | 3.2.0 |
| Spring Security | starter-security |
| Spring Data JPA | starter-data-jpa |
| MySQL Connector | mysql-connector-j |
| JWT (jjwt) | 0.12.3 |
| EasyExcel | 3.3.3 |
| Lombok | provided |
| Validation | starter-validation |
| Java | 17 |

## 数据库配置

MySQL (application.yml):
- 数据库: crab_db
- 用户名: root
- 密码: 123456
- 端口: 3306
- JPA ddl-auto: update

JWT 配置:
- secret: mySecretKey123456789012345678901234567890
- expiration: 86400000 (24小时)
- header: Authorization
- prefix: "Bearer "

## 开发环境

- 后端服务: http://localhost:8080
- 前端服务: http://localhost:3000
- Vite 代理: /api → http://localhost:8080

## 状态管理

### authStore (Zustand)
```typescript
interface AuthState {
  token: string | null
  user: UserInfo | null
  isAuthenticated: boolean
  setToken: (token: string) => void
  setUser: (user: UserInfo) => void
  logout: () => void
}
```

- 使用 persist 中间件持久化到 localStorage
- PrivateRoute 组件检测 isAuthenticated 控制路由访问

## 请求拦截器 (request.ts)

```typescript
// Axios 实例配置
baseURL: '/api'

// 请求拦截：自动附加 Authorization header
config.headers.Authorization = `Bearer ${token}`

// 响应拦截：提取 data、处理 401 自动跳转登录
```

## 注意事项

1. **生产安全**: 修改 JWT secret、使用 HTTPS、数据库密码加密
2. **Excel 导入**: ExpressAnalysis 使用 EasyExcel 动态解析列
3. **规格映射**: PurchaseSpec ↔ PlatformSpec 关联管理
4. **进货明细**: PurchaseItem 自动计算金额 (weight * unitPrice)
5. **用户状态**: status=0 禁用登录，status=1 正常
6. **API配置**: dynamicConfig 使用 JSON 存储平台特定配置（如京东 accessToken、refreshToken 等）