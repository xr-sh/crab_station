# crab_station 后续修复计划

更新时间：2026-05-02

## 1. 执行原则

本计划基于当前代码状态重新制定。优先处理 bug、高风险安全问题、构建验证缺口、运行时错误和数据正确性问题；暂不优先扩展需求型功能，例如真实定时任务执行器、第三方 API 同步、复杂 BI 报表和 UI 重设计。

当前已完成的内容不再重复列为待办，例如角色字段、部分管理员权限、全局异常处理、分页排序白名单、采购空明细校验、API 响应脱敏、CORS 去重、部分 Excel 正确性修复等。

## 2. P0：必须优先关闭

### 2.1 建立后端可编译验证

问题：当前环境没有 `mvn`，项目也没有 Maven Wrapper，后端尚未经过真实 Maven 编译。

修复步骤：

1. 在项目中加入 Maven Wrapper，或在开发/CI 环境安装 Maven。
2. 执行 `mvn -DskipTests package`。
3. 修复所有编译错误、Spring Data 查询错误、依赖缺失和注解处理问题。
4. 再执行 `mvn test`。
5. 将后端构建命令加入 README 或 CI。

验收标准：

- `backend` 可执行 `mvn -DskipTests package` 成功。
- 如果加入测试，则 `mvn test` 成功。
- 不再依赖人工静态扫描判断后端可编译性。

### 2.2 修复快递寄件日期过滤失效

问题：`ExpressAnalysisRepository.findByFilters` 中 `sentTimeStart/sentTimeEnd` 只判断 `dynamic_fields IS NOT NULL`，没有按日期过滤。

修复步骤：

1. 短期恢复对寄件时间字段的真实比较，至少支持当前导入字段中的主要别名。
2. 用 Unicode escape 或集中常量避免中文字段名再次乱码。
3. 如果继续用 JSON 查询，明确字段别名列表并覆盖 countQuery。
4. 增加最小测试数据，验证传入 `sentTimeStart/sentTimeEnd` 前后结果数量变化正确。

更稳妥方案：

1. 在 `express_analysis` 增加标准列：`sent_time`、`received_time`、`receiver_address`、`fee`。
2. 导入时从动态字段抽取并写入标准列。
3. 查询、排序、统计全部基于标准列，动态 JSON 只用于展示原始扩展字段。

验收标准：

- 前端快递页面选择寄件日期范围后，返回结果确实按寄件时间过滤。
- countQuery 与列表 query 条件一致。
- 不再用无意义的 `dynamic_fields IS NOT NULL` 代替日期比较。

### 2.3 强化 JWT secret 配置

问题：`JWT_SECRET` 有固定默认值，生产环境若未覆盖会留下 token 伪造风险。

修复步骤：

1. 增加启动时校验：非 dev profile 下禁止使用默认 secret。
2. README 补充必须配置 `JWT_SECRET`。
3. 可选：将默认 dev secret 改成明显的 `dev-only-change-me`。

验收标准：

- 未配置生产 secret 时应用拒绝启动或输出明确错误。
- 开发环境仍可本地启动。

### 2.4 移除长期管理员 fallback

问题：当活跃 ADMIN 数量为 0 时，当前登录用户会被动态提升为 ADMIN。这可防止迁移锁死，但长期运行有权限放大风险。

修复步骤：

1. 提供一次性初始化管理员脚本或启动初始化逻辑。
2. 对旧用户补默认 `USER` 角色，对指定账号设为 `ADMIN`。
3. 移除 `CustomUserDetailsService` 中“无 ADMIN 则当前用户为 ADMIN”的长期 fallback。
4. 保留“禁止删除/禁用/降级最后管理员”的保护。

验收标准：

- 无管理员时系统有明确初始化方式。
- 普通用户不会因为数据库中没有 ADMIN 而被隐式提升。

### 2.5 API 密钥加密存储

问题：API Key/Secret 响应已脱敏，但数据库仍明文保存。

修复步骤：

1. 增加 `API_SECRET_ENCRYPTION_KEY` 环境变量。
2. 对 `secret` 至少加密存储；如业务需要，也加密 `apiKey`。
3. 更新保存逻辑：入库前加密，使用时解密。
4. 响应 DTO 继续脱敏。
5. 提供旧明文数据迁移方案：首次读取迁移或一次性 SQL/脚本迁移。

验收标准：

- 数据库中不再出现明文 secret。
- 编辑 API 配置不填写 secret 时仍保留旧值。
- 密钥缺失或错误时有明确错误。

## 3. P1：短期修复

### 3.1 快递统计性能优化

问题：`getAllColumnNames()` 和 `getAverageFee()` 使用 `findAll()` 全表扫描。

修复步骤：

1. 短期为列名和平均运费增加缓存，导入、删除、清空后失效。
2. 中期增加标准字段列：`fee`、`receiver_address`、`sent_time`、`received_time`。
3. 平均运费改为数据库聚合标准列。
4. 列名改为导入时维护 `express_column_names` 或导入批次列名。

验收标准：

- `/api/express/statistics` 不再解析全表 JSON。
- 大数据量下响应时间可控。

### 3.2 Excel 导入批量保存

问题：每个 Sheet 数据仍全量驻留内存。

修复步骤：

1. 修改 `DynamicExcelListener`，接收批量保存回调或 repository。
2. 设置批大小，例如 500 或 1000。
3. `invoke` 累积到阈值后 `saveAll` 并清空列表。
4. `doAfterAllAnalysed` flush 剩余数据。
5. 确保 totalRows/successRows/failedRows 统计仍正确。

验收标准：

- 大 Excel 导入内存曲线明显下降。
- 导入结果行数和数据库保存行数一致。

### 3.3 快递导入批次与文件去重

问题：重复导入不可控，按 fileName 删除会误删同名不同批次。

修复步骤：

1. 新增 `ExpressImportBatch` 实体：id、fileName、fileHash、category、importedAt、operator、totalRows、successRows、failedRows。
2. `ExpressAnalysis` 增加 `batchId`。
3. 上传时计算文件 hash。
4. 同 hash 文件提示或拒绝重复导入。
5. 删除接口改为按 batchId 删除，保留 fileName 查询仅用于展示。

验收标准：

- 同文件重复导入可检测。
- 删除某次导入不会误删同名其他批次。

### 3.4 采购明细 LAZY 化

问题：`PurchaseRecord.items` 使用 EAGER。

修复步骤：

1. 将 `items` 改为 `FetchType.LAZY`。
2. 详情接口或列表展开所需查询增加 `@EntityGraph` 或 fetch join。
3. 确认 DTO 转换在事务内完成。

验收标准：

- 采购列表分页不会无条件加载所有明细。
- 前端展开明细功能保持正常。

### 3.5 清理重复 CORS 注解

问题：多个 Controller 仍保留 `@CrossOrigin`，全局 CORS 已在 `SecurityConfig` 中配置。

修复步骤：

1. 移除 Controller 上的 `@CrossOrigin`。
2. 将 allowed origins 配置化，例如 `APP_CORS_ALLOWED_ORIGINS`。
3. 保留 `GET, POST, PUT, DELETE, PATCH, OPTIONS`。

验收标准：

- CORS 只在一个位置维护。
- PATCH 预检正常。

### 3.6 清理前端日志和 token 工具

问题：前端仍有 `console.log`；`utils/storage.ts` 与 Zustand persist 的 token 存储方式不一致。

修复步骤：

1. 删除生产无用 `console.log`。
2. 删除未使用的 `utils/storage.ts`，或统一到 `authStore`。
3. 保留必要错误日志时按环境变量控制。
4. 中长期评估 HttpOnly Cookie 替代 localStorage token。

验收标准：

- 生产构建无 token、路由、文件列表调试输出。
- 项目只有一套 token 存储读取方式。

### 3.7 统一中文编码和关键字段常量

问题：乱码仍存在，快递字段名又依赖中文匹配。

修复步骤：

1. 确认所有源码文件统一 UTF-8。
2. 对 UI 文案、后端 message、注释逐步恢复可读中文。
3. 建立 `ExpressFieldNames` 或配置表，集中维护寄件时间、签收时间、地址、费用字段别名。
4. Java 源码中关键中文字段可使用 Unicode escape，降低编辑器编码风险。

验收标准：

- 页面核心文案和接口错误可读。
- 快递字段别名不再散落在 SQL、Java、TS 多处。

## 4. P2：中期改进

### 4.1 财务/采购统计后端化

问题：前端统计主要基于当前页数据。

修复步骤：

1. 增加 `/api/finance/statistics`。
2. 增加 `/api/purchases/statistics`。
3. 与列表接口使用相同筛选条件。
4. 前端统计卡片改用统计接口。

验收标准：

- 翻页不影响统计总数。
- 统计结果与数据库聚合一致。

### 4.2 引入数据库迁移

问题：结构变更依赖 `ddl-auto:update`，没有迁移脚本。

修复步骤：

1. 引入 Flyway 或 Liquibase。
2. 固化当前 schema baseline。
3. 为用户 role、规格映射唯一约束、快递批次等变更写迁移。
4. 生产 profile 禁用 `ddl-auto:update`。

验收标准：

- schema 变化可审计、可回滚。
- 生产不依赖 Hibernate 自动改表。

### 4.3 完善自动化测试

优先测试：

- 登录、注册开关、首个管理员。
- 管理员权限和普通用户 403。
- 最后管理员保护。
- API 配置脱敏、保留旧 secret、清空 dynamicConfig。
- 采购空明细和金额计算。
- 规格引用删除、重复映射。
- Excel 表头、空行、日期解析。
- 快递日期过滤、地址过滤、平均运费。

验收标准：

- 核心服务层和关键 Controller 有覆盖。
- CI 可执行前后端检查。

## 5. P3：需求型功能，暂缓

这些不是当前 bug 修复优先项：

- 实现真实定时任务调度和执行器。
- 实现第三方平台 API 调用链路。
- 首页复杂统计和运营 BI。
- 完整审计日志系统。
- 完整权限后台管理界面。

最低要求：

- UI 上明确标注 API 配置和定时任务目前只是“配置管理”，不要暗示已经自动执行。

## 6. 验证矩阵

构建验证：

- `frontend`: `npx tsc --noEmit`
- `frontend`: `npm run build`
- `backend`: `mvn -DskipTests package`
- `backend`: `mvn test`

安全验证：

- 未登录访问业务接口返回 401。
- 普通用户访问用户管理、API 配置、定时任务、快递清空/按文件删除返回 403。
- 关闭注册后注册接口返回明确业务错误。
- API 配置列表和详情不返回明文 secret。
- 非 dev 环境默认 JWT secret 无法启动。

参数验证：

- `page=-1` 返回 400。
- `size=0` 返回 400。
- `size=999999` 返回 400 或被明确限制。
- `sortBy=notExists` 返回 400。

快递验证：

- 有表头 Excel 不把表头计为失败行。
- 空行不入库。
- 纯日期、日期时间、Excel 数字日期可解析。
- 寄件日期范围筛选真实生效。
- 地址筛选只匹配收件地址语义，不误匹配任意 JSON 字段。
- 平均运费统计正确且不全表 JSON 扫描。
- 大文件导入内存可控。

数据一致性验证：

- 空明细采购单无法创建或更新。
- 重复规格映射无法创建。
- 被引用规格无法删除。
- 重复手机号无法创建或更新。
- 删除当前用户被拒绝。
- 删除/禁用/降级最后管理员被拒绝。

前端验证：

- 登录刷新后仍可访问受保护页面。
- 401 会清理登录态并跳转登录页。
- API 配置编辑时不提交脱敏 key/secret。
- 删除全部动态字段后可真实清空。
- 生产构建无调试日志。

## 7. 推荐执行顺序

1. 后端 Maven 编译验证。
2. 修复快递寄件日期过滤。
3. 加强 JWT secret 和管理员初始化策略。
4. API secret 加密存储。
5. 快递统计性能优化。
6. Excel 批量保存和导入批次。
7. 清理 CORS、前端日志、token 工具。
8. 采购明细 LAZY 化。
9. 数据库迁移和自动化测试。

## 8. 当前交付标准

下一轮修复完成后，应至少满足：

- 前端 `npx tsc --noEmit` 和 `npm run build` 在正常环境通过。
- 后端 `mvn -DskipTests package` 通过。
- P0 全部关闭。
- P1 中快递统计全表扫描、Excel 内存风险、CORS 重复、前端调试日志至少完成止血。
- `research.md` 和 `plan.md` 与当前代码状态保持一致。
