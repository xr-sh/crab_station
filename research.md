# crab_station 代码研究与风险审计报告

更新时间：2026-05-02

## 1. 报告范围与文件合并说明

本报告基于当前工作区代码重新阅读后整理，已合并原有两份研究文档内容，并以当前代码状态为准。原先目录中同时存在 `research.md` 与拼写错误的 `reseach.md`，两者内容已经合并到本文件，后续只保留 `research.md` 作为唯一研究报告。

本次审阅覆盖：

- `backend/src/main/java/com/example` 下的配置、鉴权、控制器、服务、仓库、实体、DTO、Excel 监听器。
- `backend/src/main/resources/application.yml`。
- `frontend/src` 下的 API 封装、状态管理、主要业务页面与弹窗。
- 当前可执行验证：前端 TypeScript 检查、Java 源码字符串静态扫描、Maven 可用性检查。

## 2. 当前总体结论

`crab_station` 是一个前后端分离的中文业务管理后台。后端使用 Spring Boot 3.2、Spring Security、Spring Data JPA、MySQL、JWT、EasyExcel；前端使用 React 18、TypeScript、Vite、Ant Design、Zustand、Axios、React Router。

系统实际覆盖的业务模块包括：

- 用户注册、登录、JWT 鉴权、用户管理。
- 财务收支记录。
- 采购主从单、采购规格、平台规格、规格映射。
- 快递 Excel 多文件、多 Sheet、动态列导入与分析。
- API 配置管理。
- 定时任务配置管理。

上一轮修复后，项目已经解决了不少 P0/P1 问题，包括角色权限、API 密钥返回脱敏、全局异常结构、分页排序白名单、采购空明细校验、规格删除引用检查、重复规格映射检查、部分乱码导致的 Java 字符串未闭合问题等。

但当前仍不能视为生产可用，主要原因：

- 后端无法在当前环境执行 Maven 编译验证，因为没有 `mvn` 和 Maven Wrapper。
- 前端 `npx tsc --noEmit` 通过，但完整 `npm run build` 之前在 Vite/esbuild 阶段出现 `spawn EPERM`，需要在非受限环境复验。
- 快递查询的寄件日期过滤逻辑被简化为 `dynamic_fields IS NOT NULL`，功能上已经失真。
- 快递列名统计和平均运费统计仍使用 `findAll()` 全表扫描，数据量增大后风险很高。
- API 密钥只是返回脱敏，数据库仍明文保存。
- 项目仍存在大量中文乱码文案、注释和 UI 文本，可读性和字段匹配可靠性不足。
- `backend/target`、`frontend/dist` 和异常命名目录存在于仓库/工作区中，构建产物与异常目录需要清理或纳入 `.gitignore` 策略。

## 3. 技术结构

后端目录结构：

```text
backend/src/main/java/com/example/
  config/        Spring Security 与 CORS
  controller/    REST API
  dto/           请求/响应 DTO
  entity/        JPA 实体
  exception/     统一异常结构
  listener/      EasyExcel 动态解析监听器
  repository/    Spring Data JPA 仓库
  security/      JWT 与 UserDetailsService
  service/       业务逻辑
  util/          分页排序工具
```

前端目录结构：

```text
frontend/src/
  api/           Axios 请求封装与业务 API
  components/    布局、私有路由
  pages/         页面与弹窗
  stores/        Zustand 登录态
  utils/         本地存储、行政区划等工具
```

## 4. 已完成或已缓解的问题

### 4.1 构建阻断类

- `frontend/src/pages/Express/index.tsx` 的 `Statistics` 初始值已补充 `averageFee: 0`，`npx tsc --noEmit` 通过。
- `backend/pom.xml` 已重建为合法 XML，修复了原先 `<name>` 标签损坏导致的 Maven 解析风险。
- 多个后端 Java 文件中由乱码造成的字符串未闭合问题已重建或修复，当前 Java 奇数引号扫描未发现残留。

### 4.2 安全与权限

- `SecurityConfig` 启用 `@EnableMethodSecurity`。
- 用户实体增加 `role` 字段，`CustomUserDetailsService` 会按 `ROLE_<role>` 授权。
- `UserController`、`ApiConfigController`、`ScheduledTaskController` 以及快递清空/按文件删除接口已增加管理员权限限制。
- 注册增加 `app.registration-enabled` 开关；首个注册用户为 `ADMIN`，后续用户为 `USER`。
- `AuthService.me`/登录响应不再返回完整 `User` 实体。
- `application.yml` 已支持环境变量覆盖数据库、JWT、JPA 和日志配置。

### 4.3 异常与参数

- 新增 `ErrorResponse`、`BusinessException`、`GlobalExceptionHandler`。
- 参数校验、业务异常、登录失败、权限不足、数据完整性异常有统一返回结构。
- 增加 `PageRequestUtils`，主要分页接口已使用 page/size/sortBy 白名单，避免非法排序字段直接打到 JPA。

### 4.4 业务一致性

- 采购创建和更新拒绝空明细。
- 用户手机号唯一性在服务层检查。
- 禁止删除当前用户，禁止禁用/降级/删除最后一个活跃管理员。
- 规格删除前检查采购明细和规格映射引用。
- 规格映射服务层检查重复，实体层增加 `purchase_spec_id + platform_spec_id` 唯一约束。
- API 配置动态字段可通过传 `{}` 清空，前端编辑时不再把脱敏后的 key/secret 当作真实值回填。

### 4.5 快递导入

- 表头不再计入失败数据行。
- 空行跳过。
- 时效只计算一次。
- 日期解析支持日期时间、纯日期、Excel 数字日期。
- 平均运费不再依赖损坏的原生 SQL，改为服务层解析 JSON 字段计算，避免统计接口被坏 SQL 直接打挂。

## 5. 当前仍存在的高风险问题

### P0-1 后端未经过真实 Maven 编译验证

当前环境没有 `mvn`，也没有 Maven Wrapper，因此后端无法执行：

```bash
mvn test
mvn -DskipTests package
```

这意味着 Spring Data 派生方法、JPA 查询、Lombok、EasyExcel/POI 依赖、注解处理等仍缺少最终编译确认。虽然静态扫描已清理明显语法破损，但后端可构建性仍是未关闭风险。

### P0-2 快递寄件日期过滤逻辑当前失真

位置：`backend/src/main/java/com/example/repository/ExpressAnalysisRepository.java`

当前查询条件：

```java
(:sentTimeStart IS NULL OR e.dynamic_fields IS NOT NULL)
(:sentTimeEnd IS NULL OR e.dynamic_fields IS NOT NULL)
```

这并没有比较寄件日期，只要 `dynamic_fields` 非空就通过。结果是前端传入 `sentTimeStart/sentTimeEnd` 时，用户以为在按寄件日期筛选，实际几乎没有生效。

风险：

- 快递列表筛选结果错误。
- 统计与导出判断被误导。
- 这是功能正确性 bug，不是性能或需求扩展。

### P0-3 API 密钥仍是数据库明文存储

位置：

- `ApiConfig.secret`
- `ApiConfig.apiKey`
- `ApiConfigService.createApiConfig/updateApiConfig`

当前只做了响应脱敏，数据库仍明文保存。数据库泄露、备份泄露、日志误打或管理员误操作时，密钥仍会直接泄露。

短期可以接受“返回脱敏”作为止血，但生产前必须引入加密存储或密钥托管方案。

### P0-4 JWT 默认 secret 仍是固定开发值

位置：`backend/src/main/resources/application.yml`

```yaml
jwt:
  secret: ${JWT_SECRET:mySecretKey123456789012345678901234567890}
```

虽然支持环境变量覆盖，但默认值仍是固定公开值。如果生产环境未设置 `JWT_SECRET`，token 可被伪造。生产启动时应强制要求非默认 secret，或至少在非 dev profile 下拒绝启动。

### P0-5 旧数据角色迁移策略存在权限放大窗口

位置：`CustomUserDetailsService`

```java
if (userRepository.countByRoleAndStatus("ADMIN", 1) == 0) {
    role = "ADMIN";
}
```

这个 fallback 可以防止老数据没有管理员时锁死后台，但也意味着只要数据库中没有活跃 ADMIN，任意活跃登录用户都会被提升为 ADMIN。它适合一次性迁移过渡，不适合作为长期逻辑。

建议改为明确初始化管理员或一次性迁移脚本，迁移完成后移除 fallback。

## 6. P1 级剩余问题

### 6.1 快递统计全表扫描

位置：`ExpressAnalysisService.getAllColumnNames()`、`getAverageFee()`

当前都使用：

```java
expressAnalysisRepository.findAll()
```

影响：

- `/api/express/statistics` 会触发文件名、列名、平均运费等多项扫描。
- 数据量较大时会慢查询、内存上涨，甚至 OOM。
- 平均运费 Java 解析虽然规避了坏 SQL，但性能更差。

建议建立导入批次表、列名表、标准化运费字段，或至少增加缓存和失效策略。

### 6.2 快递地址筛选退化为 JSON 字符串 LIKE

位置：`ExpressAnalysisRepository.findByFilters`

当前使用：

```sql
e.dynamic_fields LIKE CONCAT('%', :receiverAddress, '%')
```

风险：

- 会误匹配任意 JSON 字段，不限于收件地址。
- 无法有效使用索引。
- 地址字段名变体无法明确管理。

建议导入时抽取标准列，例如 `receiver_address`、`sent_time`、`received_time`、`fee`。

### 6.3 Excel 导入仍按 Sheet 全量驻留内存

位置：`DynamicExcelListener.dataList`、`ExcelImportService.importSingleFile`

虽然导入正确性已有改善，但每个 Sheet 仍会把全部数据行放入内存 List，再一次性 `saveAll`。大 Excel 文件仍有内存风险。

建议监听器接收保存回调或 repository，按 500/1000 行批量 flush。

### 6.4 采购明细仍为 EAGER 加载

位置：`PurchaseRecord.items`

```java
fetch = FetchType.EAGER
```

分页查询采购主单时会连带加载明细。数据量增大后会造成 SQL 和内存压力。建议改为 LAZY，并在详情或列表需要展开时用 `@EntityGraph` 或 fetch join。

### 6.5 部分控制器仍保留 `@CrossOrigin`

虽然全局 CORS 已在 `SecurityConfig` 中维护，但多个 Controller 仍有 `@CrossOrigin`。这会增加维护点，未来 allowed origins/methods 不一致时容易复发。

建议统一移除 Controller 级 CORS，仅保留 `SecurityConfig`。

### 6.6 前端仍存在调试日志和 localStorage token 风险

位置：

- `frontend/src/components/MainLayout/index.tsx`
- `frontend/src/pages/Express/components/ImportModal.tsx`
- `frontend/src/stores/authStore.ts`
- `frontend/src/api/request.ts`
- `frontend/src/utils/storage.ts`

问题：

- 菜单点击、导入文件仍有 `console.log`。
- token 存在 localStorage，XSS 后容易被读取。
- `utils/storage.ts` 操作 `localStorage.token`，而真实鉴权使用 Zustand 的 `auth-storage`，存在两套 token 工具不一致。

建议删除无用 storage 工具或统一封装，移除生产日志；中长期改为 HttpOnly Cookie 或加强 CSP。

### 6.7 中文乱码仍广泛存在

表现：

- 旧报告、部分前端 UI 文本、部分后端响应 message、注释仍显示乱码。
- 部分文件已被重建为 ASCII，但项目整体编码/文本质量仍未统一。

影响：

- UI 和错误提示不可读。
- 维护成本高。
- 快递字段名匹配依赖中文，乱码会影响查询/统计准确性。

建议统一 UTF-8，关键中文业务字段使用 Unicode escape 或集中常量，避免散落在 SQL/Java/TS 中。

## 7. P2/P3 问题与功能边界

### 7.1 API 配置模块只是配置库

当前 API 配置模块支持 CRUD、状态、动态配置，但没有真实第三方平台调用链路。它不应在 UI 或文档中暗示“已完成平台同步”。

### 7.2 定时任务模块只是配置库

当前没有 `@EnableScheduling`、`TaskScheduler`、Quartz、任务执行器、Cron 校验、执行状态更新。`ScheduledTask` 的执行时间和次数只是在数据结构上预留。

### 7.3 首页、财务、采购统计仍不完整

- 首页统计仍偏占位。
- 财务/采购页面统计主要基于当前页数据，而不是后端按筛选条件聚合。

这些属于功能完整性问题，优先级低于 P0/P1 bug，但需要在产品说明中明确。

### 7.4 缺少数据库迁移体系

当前依赖 `ddl-auto:update`。新增 `role`、规格映射唯一约束等结构变更没有迁移脚本，也没有旧数据清理脚本。生产前应引入 Flyway/Liquibase。

### 7.5 缺少测试

未发现系统性单元测试和集成测试。高风险区域应优先补：

- Auth/权限。
- UserService 最后管理员保护。
- API config 脱敏和更新保留旧 secret。
- Purchase 空明细和总额计算。
- SpecificationMapping 重复检查。
- DynamicExcelListener 表头、空行、日期解析。
- Express 查询过滤与统计。

## 8. 验证记录

已执行：

```bash
cd frontend
npx tsc --noEmit
```

结果：通过。

已执行 Java 源码未闭合字符串静态扫描：未发现奇数引号残留。

已检查 Maven 可用性：当前环境没有 `mvn`，且项目没有 Maven Wrapper，后端编译未验证。

历史/当前限制：

- `npm run build` 在 Vite/esbuild 阶段曾失败为 `spawn EPERM`，属于当前执行环境对子进程的权限限制可能性较高，但仍需在正常开发机或 CI 上复验。

## 9. 当前最重要的特殊之处

- 快递模块使用 `dynamic_fields` JSON 承接 Excel 任意列，这是项目最核心也最有风险的设计点。
- 快递时效、费用、地址等业务语义依赖中文字段名，必须集中管理字段别名。
- 规格映射、API 配置、定时任务看起来是为外部平台对接和自动化预留，但当前没有实际消费链路。
- 鉴权已从“只要登录即可”推进到“管理员保护高危接口”，但管理员初始化/迁移策略仍需固化。
- 前端请求直接从 localStorage 的 Zustand persist 结构读取 token，绕过 hydration 时序问题，但加重了 localStorage token 风险。

## 10. 结论

当前代码已经比初次审计时更接近可运行后台，P0/P1 中多项明显问题已处理。但仍存在几个必须优先关闭的风险：后端真实编译验证、快递寄件日期过滤失效、API 密钥明文存储、JWT 默认 secret、快递统计全表扫描、Excel 大文件导入内存风险、角色 fallback 长期权限放大。

建议下一轮不再扩大功能，先按 `plan.md` 关闭这些剩余 bug 和高风险点，再考虑定时任务执行器、第三方 API 调用链路、首页统计等需求型功能。
