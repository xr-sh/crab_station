# 修复 Excel 导入表头识别问题

## TL;DR

> **Quick Summary**: EasyExcel 默认跳过第一行，导致真正的表头行未被读取。需要在 Reader 配置中设置 `headRowNumber(0)` 从第0行开始读取。
> 
> **Deliverables**: 
> - 修改 ExcelImportService.java 添加 `headRowNumber(0)` 配置
> 
> **Estimated Effort**: Quick (单行代码修改)

---

## Context

### Problem Analysis

**现象**:
- Excel 文件：628 行（第1行：运单号/寄件时间/签收时间...，第2-628行：数据）
- 后端日志：`总行数: 627, 成功: 626, 失败: 1`
- 解析到的表头：`[SF1519455527094, 2025-9-20 16:40, ...]`（这是第2行的数据！）

**根因**:
EasyExcel 默认会跳过第一行，认为第一行是表头行。但我们的监听器 `DynamicExcelListener` 是基于 `Map<Integer, String>` 的原始数据监听器，期望收到所有行包括第一行。

**EasyExcel 的 headRowNumber 参数**:
- 默认值：1（表示第1行是表头，从第2行开始读取数据）
- 我们需要：0（表示第0行是表头，即从第0行开始读取所有行，不跳过）

---

## Work Objectives

### Core Objective
让 EasyExcel 读取 Excel 文件的所有行（包括第一行），由我们的监听器自己判断是否是表头行。

### Concrete Deliverables
- 修改 `ExcelImportService.java` 第67行和第88行

---

## TODOs

- [ ] 1. 修改 ExcelImportService.java 添加 headRowNumber(0) 配置

  **What to do**:
  在创建 ExcelReader 时添加 `.headRowNumber(0)` 配置：
  
  **第67行** - 第一个 reader 创建：
  ```java
  // 修改前
  ExcelReader excelReader = EasyExcel.read(file.getInputStream(), listener).build();
  
  // 修改后
  ExcelReader excelReader = EasyExcel.read(file.getInputStream(), listener)
          .headRowNumber(0)  // 从第0行开始读取，不跳过任何行
          .build();
  ```
  
  **第88行** - 每个 sheet 的 reader 创建：
  ```java
  // 修改前
  ExcelReader sheetReader = EasyExcel.read(file.getInputStream(), sheetListener).build();
  
  // 修改后
  ExcelReader sheetReader = EasyExcel.read(file.getInputStream(), sheetListener)
          .headRowNumber(0)  // 从第0行开始读取，不跳过任何行
          .build();
  ```

  **Must NOT do**:
  - 不要修改监听器逻辑
  - 不要修改其他配置

  **References**:
  - `backend/src/main/java/com/example/service/ExcelImportService.java:67-68` - 第一个 reader 创建位置
  - `backend/src/main/java/com/example/service/ExcelImportService.java:88-89` - sheet reader 创建位置

  **Acceptance Criteria**:
  - [ ] 后端编译通过
  - [ ] 导入测试：日志显示 `解析到表头: [运单号, 寄件时间, 签收时间, 收件人, 收件地址, 运费, 计费重量]`
  - [ ] 导入测试：日志显示 `总行数: 628, 成功: 627`（包含表头行）

  **QA Scenarios**:
  ```
  Scenario: 导入有表头的 Excel 文件
    Preconditions: Excel 文件第1行是表头（运单号、寄件时间...）
    Steps:
      1. 重启后端服务
      2. 前端选择文件、类别（顺丰）、勾选"Excel文件包含表头行"
      3. 点击"开始导入"
      4. 查看后端日志
    Expected Result: 
      - 日志显示：解析到表头: [运单号, 寄件时间, 签收时间, 收件人, 收件地址, 运费, 计费重量]
      - 日志显示：总行数: 628, 成功: 627
    Evidence: 后端日志截图
  ```

  **Commit**: YES
  - Message: `fix(express): 修复Excel导入表头识别问题 - 设置headRowNumber(0)`
  - Files: `backend/src/main/java/com/example/service/ExcelImportService.java`

---

## Final Verification Wave

- [ ] F1. **Plan Compliance Audit**
  验证修改的两行代码是否符合计划要求。

- [ ] F2. **Code Quality Review**
  编译测试：`mvn clean compile`

- [ ] F3. **Real Manual QA**
  使用实际 Excel 文件测试导入功能，验证表头正确识别。

- [ ] F4. **Scope Fidelity Check**
  确认只修改了 ExcelImportService.java，未影响其他文件。

---

## Success Criteria

### Verification Commands
```bash
# 后端编译
cd backend
mvn clean compile

# 启动后端
mvn spring-boot:run

# 查看日志确认
# 应该看到：解析到表头: [运单号, 寄件时间, ...]
```

### Final Checklist
- [ ] 后端编译通过
- [ ] 后端启动成功
- [ ] 导入测试：表头正确显示为"运单号、寄件时间..."而不是数据
- [ ] 导入测试：数据行数正确（627行数据）
- [ ] 前端表格显示正确的列名