# crab_station 开发与维护计划

更新时间：2026-05-03

## 1. 文档分工

- `crab_station.md`：只记录产品需求和业务边界。
- `plan.md`：记录开发计划、修复计划、验证计划和优先级。
- `README.md`：记录项目入口说明、启动方式和环境配置。
- `AGENTS.md`：记录 AI 代理和协作者需要遵守的工程规则。
- `DEVELOPMENT_MEMORY.md`：记录每一轮重要对话、决策、改动和验证结果。

## 2. 执行原则

当前项目已经具备主要业务页面和 API 骨架，但仍处于内部工具/MVP 向稳定系统演进的阶段。

后续维护优先级：

1. 先修正确性问题。
2. 再处理安全风险。
3. 再处理性能和可维护性。
4. 最后扩展新功能。

短期不建议继续扩展复杂新模块。应优先关闭快递筛选失效、密钥明文、默认 JWT secret、构建验证缺口、乱码文案和测试缺失等基础问题。

## 3. P0：必须优先处理

### 3.1 建立后端真实构建验证

问题：

- 当前仓库没有 Maven Wrapper。
- 当前环境未确认可运行 `mvn`。
- 后端缺少真实 Maven 编译和测试验证。

计划：

1. 添加 Maven Wrapper，或在开发/CI 环境安装 Maven。
2. 执行 `mvn -DskipTests package`。
3. 修复编译错误、Spring Data 查询错误、依赖问题和 Lombok 注解处理问题。
4. 执行 `mvn test`。
5. 把后端构建命令写入 README 和后续 CI。

验收：

- `backend` 下可执行 `mvn -DskipTests package` 成功。
- 如果已有测试，则 `mvn test` 成功。
- 后端可构建性不再依赖人工静态阅读判断。

### 3.2 修复快递寄件日期筛选失效

问题：

- `ExpressAnalysisRepository.findByFilters` 中 `sentTimeStart/sentTimeEnd` 目前只判断 `dynamic_fields IS NOT NULL`。
- 用户选择寄件日期范围后，实际没有按寄件日期过滤。

短期计划：

1. 集中定义寄件日期字段别名。
2. 从动态字段中提取寄件日期。
3. 修复列表查询和 countQuery，使寄件日期条件真实生效。
4. 增加最小测试或手工验证数据。

推荐中期方案：

1. 在 `express_analysis` 增加标准字段：
   - `sent_time`
   - `received_time`
   - `receiver_address`
   - `fee`
2. 导入时从动态字段抽取标准字段。
3. 查询、排序、统计统一使用标准字段。
4. 动态 JSON 只用于展示原始扩展列。

验收：

- 选择寄件日期范围后，列表数量和内容真实变化。
- 列表 query 与 countQuery 条件一致。
- 不再用 `dynamic_fields IS NOT NULL` 代替日期比较。

### 3.3 强化 JWT Secret 配置

问题：

- `JWT_SECRET` 存在固定默认值。
- 生产环境如果未覆盖，token 存在伪造风险。

计划：

1. 增加启动校验，非开发 profile 禁止使用默认 JWT secret。
2. README 中明确生产环境必须配置 `JWT_SECRET`。
3. 可选：把默认值改成明显的开发专用值。

验收：

- 生产环境未配置安全 secret 时应用拒绝启动或明确报错。
- 本地开发仍可按说明启动。

### 3.4 移除长期管理员兜底提权逻辑

问题：

- 当前 `CustomUserDetailsService` 在没有活跃 ADMIN 时会把当前登录用户临时提升为 ADMIN。
- 这适合迁移期，但长期运行有权限放大风险。

计划：

1. 提供明确的管理员初始化方式。
2. 对旧用户补默认 `USER` 角色。
3. 对指定账号设置 `ADMIN`。
4. 移除登录时动态提权 fallback。
5. 保留最后一个活跃管理员保护。

验收：

- 普通用户不会因为系统没有 ADMIN 而隐式获得管理员权限。
- 系统仍有明确的首个管理员创建路径。

### 3.5 API Secret 加密存储

问题：

- API Key/Secret 当前只是响应脱敏，数据库仍明文保存。

计划：

1. 增加 `API_SECRET_ENCRYPTION_KEY` 环境变量。
2. 至少对 `secret` 加密入库。
3. 业务读取时解密。
4. DTO 响应继续脱敏。
5. 编辑时未填写 secret 应保留旧值。
6. 设计旧明文数据迁移方案。

验收：

- 数据库中不再保存明文 secret。
- 编辑 API 配置时不填 secret 能保留旧值。
- 加密 key 缺失或错误时有明确错误。

## 4. P1：短期修复

### 4.1 优化快递统计性能

问题：

- `getAllColumnNames()` 和 `getAverageFee()` 使用 `findAll()` 全表扫描。

计划：

1. 短期增加缓存，并在导入、删除、清空后失效。
2. 中期引入标准字段 `fee`。
3. 平均运费改为数据库聚合标准字段。
4. 列名改为导入批次或列名表维护。

验收：

- `/api/express/statistics` 不再长期依赖全表 JSON 解析。
- 大数据量下响应时间可控。

### 4.2 Excel 导入批量保存

问题：

- 每个 Sheet 数据先全部存入内存 List，再一次性 `saveAll`。

计划：

1. 修改 `DynamicExcelListener`，支持批量保存回调。
2. 批大小建议 500 或 1000。
3. 达到批大小后保存并清空缓存。
4. 解析结束时 flush 剩余数据。
5. 保证导入统计仍准确。

验收：

- 大 Excel 导入内存占用明显下降。
- 成功行数与数据库保存行数一致。

### 4.3 建立快递导入批次

问题：

- 按文件名删除会误删同名文件的不同导入记录。
- 重复导入不可控。

计划：

1. 新增 `ExpressImportBatch` 实体。
2. 字段建议：
   - id
   - fileName
   - fileHash
   - category
   - importedAt
   - operator
   - totalRows
   - successRows
   - failedRows
3. `ExpressAnalysis` 增加 `batchId`。
4. 上传时计算文件 hash。
5. 支持重复文件提示或拒绝。
6. 删除接口改为按 batchId 删除。

验收：

- 同文件重复导入可检测。
- 删除某次导入不会误删同名其他批次。

### 4.4 采购明细 LAZY 化

问题：

- `PurchaseRecord.items` 当前为 EAGER，分页查询会无条件加载明细。

计划：

1. 改为 `FetchType.LAZY`。
2. 详情接口用 fetch join 或 EntityGraph。
3. 确保 DTO 转换在事务内完成。

验收：

- 采购列表分页不会无条件加载所有明细。
- 详情和编辑功能保持正常。

### 4.5 清理重复 CORS 配置

问题：

- 多个 Controller 上仍有 `@CrossOrigin`。
- 全局 CORS 已在 `SecurityConfig` 配置。

计划：

1. 移除 Controller 层 `@CrossOrigin`。
2. 将 allowed origins 配置化，例如 `APP_CORS_ALLOWED_ORIGINS`。
3. 保留 GET、POST、PUT、DELETE、PATCH、OPTIONS。

验收：

- CORS 只在一个位置维护。
- PATCH 等预检请求正常。

### 4.6 清理前端日志和 token 工具

问题：

- 前端仍有调试日志。
- `utils/storage.ts` 使用独立 `token` key，但真实认证使用 `auth-storage`。

计划：

1. 删除生产无用 `console.log`。
2. 删除未使用的 `utils/storage.ts`，或统一到 `authStore`。
3. 必要错误日志按环境控制。
4. 中期评估 HttpOnly Cookie 替代 localStorage token。

验收：

- 生产构建无菜单、token、文件列表调试输出。
- 项目只有一套 token 存储读取方式。

### 4.7 修复乱码文案

问题：

- README、AGENTS、前端 UI 文案、后端提示和部分注释存在乱码。

计划：

1. 确认文件统一 UTF-8。
2. 优先修复用户可见 UI 文案。
3. 再修复后端错误 message。
4. 最后修复注释和历史文档。
5. 快递关键中文字段使用集中常量或 Unicode escape。

验收：

- 核心页面文案可读。
- 接口错误提示可读。
- 快递字段别名集中维护。

## 5. P2：中期增强

### 5.1 财务和采购统计后端化

计划：

1. 增加 `/api/finance/statistics`。
2. 增加 `/api/purchases/statistics`。
3. 统计接口与列表接口使用相同筛选条件。
4. 前端统计卡片改用后端统计接口。

验收：

- 翻页不影响统计结果。
- 统计结果与数据库聚合一致。

### 5.2 引入数据库迁移

计划：

1. 引入 Flyway 或 Liquibase。
2. 固化当前 schema baseline。
3. 为新增字段、唯一约束、批次表等写迁移脚本。
4. 生产 profile 禁用 `ddl-auto:update`。

验收：

- schema 变更可审计、可回滚。
- 生产环境不依赖 Hibernate 自动改表。

### 5.3 补充自动化测试

优先测试：

- 登录、注册开关、首个管理员。
- 管理员权限和普通用户 403。
- 最后管理员保护。
- API 配置脱敏、保留旧 secret、清空 dynamicConfig。
- 采购空明细和金额计算。
- 规格引用删除保护。
- 规格映射重复保护。
- Excel 表头、空行、日期解析。
- 快递日期筛选、地址筛选、平均运费。

验收：

- 核心服务层和关键 Controller 有覆盖。
- CI 能运行前后端检查。

## 6. P3：长期功能扩展

这些属于需求型扩展，不应抢在 P0/P1 前面：

- 真实定时任务调度和执行器。
- 第三方平台 API 同步链路。
- 订单导入或平台订单管理。
- 库存管理。
- 审计日志。
- 更完整的首页 BI 和经营报表。
- 更细粒度权限系统。

## 7. 验证矩阵

### 构建验证

- `frontend`: `npx tsc --noEmit`
- `frontend`: `npm run build`
- `backend`: `mvn -DskipTests package`
- `backend`: `mvn test`

### 安全验证

- 未登录访问业务接口返回 401。
- 普通用户访问用户管理、API 配置、定时任务返回 403。
- 关闭注册后注册接口返回明确业务错误。
- API 配置列表和详情不返回明文 secret。
- 非开发环境默认 JWT secret 无法启动。

### 参数验证

- `page=-1` 返回 400。
- `size=0` 返回 400。
- `size=999999` 返回 400 或被明确限制。
- `sortBy=notExists` 返回 400。

### 快递验证

- 有表头 Excel 不把表头计入失败行。
- 空行不入库。
- 纯日期、日期时间、Excel 数字日期可解析。
- 寄件日期范围筛选真实生效。
- 地址筛选只匹配收件地址语义。
- 平均运费统计正确。
- 大文件导入内存可控。

### 数据一致性验证

- 空明细采购单无法创建或更新。
- 重复规格映射无法创建。
- 被引用规格无法删除。
- 重复手机号无法创建或更新。
- 删除当前用户被拒绝。
- 删除、禁用或降级最后管理员被拒绝。

### 前端验证

- 登录刷新后仍可访问受保护页面。
- 401 后清理登录状态并跳转登录页。
- API 配置编辑时不提交脱敏 key/secret。
- 删除全部动态字段后可真实清空。
- 生产构建无调试日志。

## 8. 推荐执行顺序

1. 建立后端 Maven 构建验证。
2. 修复快递寄件日期筛选。
3. 强化 JWT secret 和管理员初始化策略。
4. API secret 加密存储。
5. 修复乱码文案。
6. 快递统计性能优化。
7. Excel 批量保存和导入批次。
8. 清理 CORS、前端日志和 token 工具。
9. 采购明细 LAZY 化。
10. 数据库迁移和自动化测试。

## 9. P0：统一修复 UUID 字段映射与查询问题

问题：
- 多个模块出现 `findById`、派生查询、JPQL bulk delete、native UUID 字符串查询无法命中已有数据的问题。
- 已确认平台套餐删除中，前端传入 ID 正确，`findAll()` 读取实体后用 Java UUID 比较可以命中，但 UUID 参数查询和删除影响行数为 0。
- 当前平台规格、规格映射、平台套餐已加入局部 fallback，能临时维持业务，但会增加维护风险。

计划：
1. 盘点所有 UUID 主键和外键字段的数据库真实类型，包括 `platform_specs`、`purchase_specs`、`specification_mappings`、`platform_packages`、`platform_package_items`、`platform_package_price_rules`。
2. 确认 Hibernate 当前对 `java.util.UUID` 的绑定方式，以及 MySQL 表字段是 `CHAR(36)`、`VARCHAR(36)`、`BINARY(16)` 还是其他类型。
3. 选择统一方案：
   - 方案 A：数据库统一使用 `CHAR(36)` / `VARCHAR(36)`，实体字段明确按字符串 UUID 映射。
   - 方案 B：数据库统一使用 `BINARY(16)`，实体字段明确使用 Hibernate UUID binary 映射。
4. 编写迁移脚本，统一主表和外键表字段类型，并验证外键数据一致。
5. 移除业务层临时 fallback：
   - `findAll()` 后 Java UUID 比较。
   - `name + createdAt` 删除兜底。
   - 其他为了规避 UUID 查询失败加入的特殊逻辑。
6. 恢复 repository 正常查询和删除方式：
   - `findById`
   - 派生查询
   - 标准 JPA 删除或明确的 bulk delete。
7. 增加最小验证用例或手工验证矩阵，覆盖创建、详情、编辑、删除、关联查询。

验收：
- 所有 UUID 主键表通过 `findById` 能稳定查到已存在数据。
- 所有 UUID 外键派生查询能稳定返回关联明细。
- 平台套餐删除不再依赖 `name + createdAt` fallback。
- 规格映射和平台套餐创建不再依赖 `findAll()` UUID 比较 fallback。
- 后端 `mvn -DskipTests package` 通过；条件允许时 `mvn test` 通过。

## 10. UUID 问题整体治理方案

### 背景

当前项目的 UUID 问题不是单一接口异常，而是贯穿了主表、外键表、Repository 查询、DTO 转换和局部删除逻辑。已确认的现象包括：
- `findById` 对某些 UUID 记录失效，但 `findAll()` 读出的 Java 对象可以匹配。
- 派生查询和 JPQL/native 删除对同一批 UUID 记录影响行数为 0。
- 列表/详情 DTO 在关联加载失败时会抛错或返回占位文本。
- 平台套餐、平台规格、规格映射、采购规格、采购记录都受到不同程度影响。

项目当前是测试数据阶段，且你已确认相关表可以删除重建，因此优先采用“清表重建 + 统一映射 + 去除 fallback”的路线，而不是继续堆业务兜底。

### 目标

1. 统一所有 UUID 主键和外键的数据库类型与实体映射。
2. 去掉 `findAll()` UUID 比较、`name + createdAt` 删除兜底等临时逻辑。
3. 让平台规格、平台套餐、规格映射、采购规格、采购记录在标准 JPA 路径下稳定工作。
4. 让列表、详情、编辑、删除、关联查询都不再依赖特殊 fallback。
5. 保持前端不改或少改，优先通过后端和数据库统一修复。

### 范围清单

需要重点治理的表和模块：
- `platform_specs`
- `platform_packages`
- `platform_package_items`
- `platform_package_price_rules`
- `purchase_specs`
- `purchase_spec_price_history`
- `purchase_records`
- `purchase_items`
- `specification_mappings`
- 相关 Repository、Service、DTO 转换逻辑

### 调研结论

1. 平台套餐目前已经确认：
   - 创建请求里的 `selections` 正常。
   - `specCount` 可以按 `selections.size()` 正确写入。
   - `items` 的展示字段需要保存快照，不应再依赖 `PlatformSpec` 关联实时加载。
   - 删除和更新曾多次出现 UUID 绑定失效，只能靠临时 fallback 维持。

2. 平台规格和规格映射链路中，已经出现过：
   - `findById` 查不到，但 `findAll()` 能查到。
   - 关联加载时抛 `Unable to find ...`。
   - 需要 Java 层兜底比较 UUID 才能保持业务流转。

3. 数据库目前处于测试数据阶段，允许重建，这给了我们一次性清理 UUID 映射问题的窗口。

### 推荐方案

#### 方案 A：统一成字符串 UUID

适用于当前项目最小改动和可读性优先的路线。
- 数据库主键/外键统一使用 `CHAR(36)` 或 `VARCHAR(36)`。
- 实体字段统一使用 `java.util.UUID`，但明确校准 Hibernate/MySQL 映射。
- 所有外键列都按字符串 UUID 存储。
- 优点：人工排查直观，迁移和调试简单。
- 缺点：空间占用略大。

#### 方案 B：统一成 binary UUID

适用于追求存储和索引效率的路线。
- 数据库主键/外键统一使用 `BINARY(16)`。
- 实体字段用明确的 UUID binary 映射。
- 所有查询、删除、关联都按同一二进制规则执行。
- 优点：性能更好。
- 缺点：迁移和排查复杂，当前项目修复成本更高。

#### 建议选择

优先选 **方案 A**。理由：
- 当前是测试数据阶段，核心目标是先恢复稳定性。
- 项目现有代码和日志更接近字符串 UUID 思路。
- 当前业务问题集中在查询/删除命中失败，不是极端性能瓶颈。

### 实施步骤

1. 盘点所有 UUID 相关表的字段类型和外键关系。
2. 确定主键与外键统一采用的具体类型。
3. 重建相关表或编写迁移脚本，清理旧测试数据。
4. 补齐实体映射，保证主键、外键、快照字段一致。
5. 回收所有临时 fallback：
   - `findAll()` UUID 比较
   - `name + createdAt` 删除兜底
   - DTO 中对缺失关联的特殊替代逻辑
6. 对平台套餐补齐业务快照字段：
   - `specCount`
   - `platform_package_items.platformSpecName`
   - `platform_package_items.platformSpecCategory`
7. 对列表/详情/编辑/删除/筛选做逐项验证。
8. 清理开发记录里的临时诊断日志，保留必要的错误处理。

### 风险点

- 目前平台套餐更新采用了“删除后重建”的短期方案，统一 UUID 修复前这属于临时设计，后续要恢复为真正更新。
- 旧测试数据如果不清表，容易继续触发历史 UUID 不一致问题。
- DTO 容错逻辑保留过多会掩盖后续修复效果，需要在统一修复后回收。

### 验收标准

- 平台规格、平台套餐、规格映射、采购相关模块都能通过标准 JPA 查询正常工作。
- 删除不再需要 `name + createdAt` 兜底。
- 编辑不再需要删除重建套餐。
- 列表/详情不再依赖 `findAll()` UUID 比较兜底。
- 相关前端页面在不改或少改的情况下正常显示。
- 后端能够在统一数据库类型后通过真实构建验证。
