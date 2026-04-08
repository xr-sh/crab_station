package com.example.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.example.entity.ExpressAnalysis;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 动态Excel监听器
 * 支持动态识别列头，将每行数据存储为JSON格式
 */
@Slf4j
public class DynamicExcelListener extends AnalysisEventListener<Map<Integer, String>> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 表头列表（列名）
     */
    @Getter
    private final List<String> headers = new ArrayList<>();

    /**
     * 表头索引列表（原始列索引）
     */
    private final List<Integer> headerIndices = new ArrayList<>();

    /**
     * 解析后的数据列表
     */
    @Getter
    private final List<ExpressAnalysis> dataList = new ArrayList<>();

    /**
     * 快递类别（顺丰/京东）
     */
    private final String category;

    /**
     * 文件名
     */
    private final String fileName;

    /**
     * Sheet名
     */
    private String sheetName;

    /**
     * 当前Sheet索引
     */
    private int sheetIndex = 0;

    /**
     * 错误信息列表
     */
    @Getter
    private final List<String> errors = new ArrayList<>();

    /**
     * 是否已解析表头
     */
    private boolean headerParsed = false;

    /**
     * 是否包含表头行（默认true）
     */
    private boolean hasHeader = true;

    /**
     * 总行数（不含表头）
     */
    @Getter
    private int totalRows = 0;

    /**
     * 成功解析行数
     */
    @Getter
    private int successRows = 0;

    public DynamicExcelListener(String fileName, String category) {
        this.fileName = fileName;
        this.category = category;
    }

    /**
     * 构造函数（支持指定是否有表头行）
     */
    public DynamicExcelListener(String fileName, String category, boolean hasHeader) {
        this.fileName = fileName;
        this.category = category;
        this.hasHeader = hasHeader;
    }

    /**
     * 解析每一行数据
     */
    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext context) {
        totalRows++;
        
        try {
            // 处理表头
            if (!headerParsed) {
                if (hasHeader) {
                    // 第一行作为表头
                    parseHeader(data);
                    headerParsed = true;
                    log.info("文件 {} 使用第一行作为表头: {}", fileName, headers);
                    return;
                } else {
                    // 没有表头，自动生成列名
                    generateDefaultHeaders(data);
                    headerParsed = true;
                    log.info("文件 {} 无表头行，自动生成列名: {}", fileName, headers);
                    // 继续处理第一行数据（不要return）
                }
            }

            // 解析数据行
            Map<String, Object> dynamicFields = new HashMap<>();
            for (int i = 0; i < headers.size(); i++) {
                String header = headers.get(i);
                Integer columnIndex = headerIndices.get(i);
                String value = data.get(columnIndex);
                
                if (header != null) {
                    dynamicFields.put(header, value != null ? value : "");
                }
            }

            // 跳过空行
            if (dynamicFields.isEmpty()) {
                return;
            }

            // 创建实体
            ExpressAnalysis express = ExpressAnalysis.builder()
                    .category(category)
                    .fileName(fileName)
                    .sheetName(sheetName != null ? sheetName : "Sheet" + sheetIndex)
                    .rowNum(context.readRowHolder().getRowIndex() + 1)
                    .dynamicFields(objectMapper.writeValueAsString(dynamicFields))
                    .durationHours(calculateDurationHours(dynamicFields))
                    .duration(formatDuration(calculateDurationHours(dynamicFields)))
                    .importedAt(LocalDateTime.now())
                    .build();

            dataList.add(express);
            successRows++;

        } catch (JsonProcessingException e) {
            String error = String.format("第%d行解析失败: %s", context.readRowHolder().getRowIndex() + 1, e.getMessage());
            errors.add(error);
            log.error(error, e);
        } catch (Exception e) {
            String error = String.format("第%d行处理异常: %s", context.readRowHolder().getRowIndex() + 1, e.getMessage());
            errors.add(error);
            log.error(error, e);
        }
    }

    /**
     * 解析表头
     * 按列索引顺序提取表头，同时保存原始索引以便数据映射
     */
    private void parseHeader(Map<Integer, String> headMap) {
        headers.clear();
        headerIndices.clear();
        
        // 按列索引从小到大排序，保证顺序正确
        headMap.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                Integer index = entry.getKey();
                String header = entry.getValue();
                
                if (header != null && !header.trim().isEmpty()) {
                    headers.add(header.trim());
                } else {
                    // 空表头用索引占位，防止列错位
                    headers.add("列" + index);
                }
                
                // 保存原始索引
                headerIndices.add(index);
            });
        
        log.info("解析到表头: {}, 索引: {}", headers, headerIndices);
    }

    /**
     * 自动生成默认列名（当Excel没有表头行时）
     * 列名格式：列1、列2、列3...
     */
    private void generateDefaultHeaders(Map<Integer, String> dataMap) {
        headers.clear();
        headerIndices.clear();
        
        // 按列索引从小到大排序
        dataMap.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                Integer index = entry.getKey();
                // 列名从1开始计数（列1、列2...）
                headers.add("列" + (index + 1));
                headerIndices.add(index);
            });
        
        log.info("自动生成列名: {}, 索引: {}", headers, headerIndices);
    }

    /**
     * 所有数据解析完成
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("Excel解析完成，文件: {}, 类别: {}, Sheet: {}, 总行数: {}, 成功: {}, 失败: {}", 
                fileName, category, sheetName, totalRows, successRows, totalRows - successRows);
    }

    /**
     * 设置Sheet名称
     */
    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    /**
     * 设置Sheet索引
     */
    public void setSheetIndex(int sheetIndex) {
        this.sheetIndex = sheetIndex;
    }

    /**
     * 获取失败行数
     */
    public int getFailedRows() {
        return totalRows - successRows;
    }

    /**
     * 计算时效小时数（签收时间 - 寄件时间）
     * 返回总小时数，用于排序和存储
     */
    private Integer calculateDurationHours(Map<String, Object> dynamicFields) {
        log.debug("开始计算时效，动态字段: {}", dynamicFields);
        
        // 尝试常见的签收时间字段名
        String[] receivedTimeKeys = {"签收时间", "签收日期", "签收", "收货时间", "收货日期"};
        String receivedTime = null;
        for (String key : receivedTimeKeys) {
            Object value = dynamicFields.get(key);
            log.debug("查找签收时间字段 [{}]: {}", key, value);
            if (value != null && !String.valueOf(value).trim().isEmpty()) {
                receivedTime = String.valueOf(value).trim();
                log.debug("找到签收时间: {} (字段名: {})", receivedTime, key);
                break;
            }
        }

        // 尝试常见的寄件时间字段名
        String[] sentTimeKeys = {"寄件时间", "寄件日期", "寄件", "发货时间", "发货日期", "发出时间", "发出日期"};
        String sentTime = null;
        for (String key : sentTimeKeys) {
            Object value = dynamicFields.get(key);
            log.debug("查找寄件时间字段 [{}]: {}", key, value);
            if (value != null && !String.valueOf(value).trim().isEmpty()) {
                sentTime = String.valueOf(value).trim();
                log.debug("找到寄件时间: {} (字段名: {})", sentTime, key);
                break;
            }
        }

        if (receivedTime == null || sentTime == null) {
            log.warn("时效计算失败: 未找到时间字段 - 寄件时间={}, 签收时间={}", sentTime, receivedTime);
            return null;
        }

        try {
            LocalDateTime receivedDate = parseDateTime(receivedTime);
            LocalDateTime sentDate = parseDateTime(sentTime);

            if (receivedDate == null || sentDate == null) {
                log.warn("时效计算失败: 时间解析失败 - 寄件时间={}, 签收时间={}", sentTime, receivedTime);
                return null;
            }

            // 计算时间差
            long totalHours = ChronoUnit.HOURS.between(sentDate, receivedDate);
            
            if (totalHours < 0) {
                log.warn("时效计算失败: 签收时间早于寄件时间 - 寄件={}, 签收={}, 时差={}小时", sentDate, receivedDate, totalHours);
                return null;
            }

            log.info("时效计算成功: 寄件={}, 签收={}, 总小时数={}", sentDate, receivedDate, totalHours);
            return (int) totalHours;

        } catch (Exception e) {
            log.warn("时效计算异常: 寄件时间={}, 签收时间={}, 错误={}", sentTime, receivedTime, e.getMessage());
            return null;
        }
    }

    /**
     * 根据小时数格式化时效字符串
     * 格式：x天x小时
     */
    private String formatDuration(Integer totalHours) {
        if (totalHours == null || totalHours < 0) {
            return null;
        }
        
        long days = totalHours / 24;
        long hours = totalHours % 24;
        
        return days + "天" + hours + "小时";
    }

    /**
     * 解析时间字符串，支持多种格式
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }

        String str = dateTimeStr.trim();
        log.debug("尝试解析时间: {}", str);

        // 尝试多种时间格式
        String[] patterns = {
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd",
            // 支持单数字的月和日
            "yyyy-M-d HH:mm:ss",
            "yyyy-M-d HH:mm",
            "yyyy-M-d H:mm:ss",
            "yyyy-M-d H:mm",
            "yyyy-M-d",
            // 支持 / 分隔符
            "yyyy/MM/dd HH:mm:ss",
            "yyyy/MM/dd HH:mm",
            "yyyy/MM/dd",
            "yyyy/M/d HH:mm:ss",
            "yyyy/M/d HH:mm",
            "yyyy/M/d",
            // 支持中文格式
            "yyyy年MM月dd日 HH时mm分ss秒",
            "yyyy年MM月dd日 HH时mm分",
            "yyyy年MM月dd日",
            "yyyy年M月d日 HH时mm分ss秒",
            "yyyy年M月d日 HH时mm分",
            "yyyy年M月d日",
            // 其他格式
            "yyyyMMdd",
            "yyyyMMdd HHmmss",
            "MM/dd/yyyy HH:mm:ss",
            "MM/dd/yyyy HH:mm",
            "MM/dd/yyyy",
            "M/d/yyyy HH:mm:ss",
            "M/d/yyyy HH:mm",
            "M/d/yyyy"
        };

        for (String pattern : patterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                LocalDateTime result = LocalDateTime.parse(str, formatter);
                log.debug("成功解析时间 [{}]: {} -> {}", pattern, str, result);
                return result;
            } catch (Exception e) {
                // 继续尝试下一个格式
            }
        }

        // 尝试处理 Excel 数字格式（Excel 内部用数字表示日期）
        try {
            if (str.matches("^\\d+(\\.\\d+)?$")) {
                double excelDateNum = Double.parseDouble(str);
                // Excel 日期序列号转换（Excel 以 1900-01-01 为基准，但有个bug认为 1900-02-29 存在）
                // 使用 org.apache.poi.ss.usermodel.DateUtil 会更准确，但这里用简单算法
                long days = (long) excelDateNum;
                double fractionalDay = excelDateNum - days;
                long hours = (long) (fractionalDay * 24);
                
                // Excel 基准日期是 1899-12-30（因为 1900 bug）
                LocalDateTime baseDate = LocalDateTime.of(1899, 12, 30, 0, 0);
                LocalDateTime result = baseDate.plusDays(days).plusHours(hours);
                log.debug("成功解析 Excel 数字格式时间: {} -> {}", str, result);
                return result;
            }
        } catch (Exception e) {
            log.debug("Excel 数字格式解析失败: {}", str);
        }

        // 尝试去掉双引号后解析
        if (str.startsWith("\"") && str.endsWith("\"")) {
            String unquoted = str.substring(1, str.length() - 1);
            LocalDateTime result = parseDateTime(unquoted);
            if (result != null) {
                return result;
            }
        }

        // 尝试解析带 "T" 的 ISO 格式
        try {
            LocalDateTime result = LocalDateTime.parse(str.replace(" ", "T"));
            log.debug("成功解析 ISO 格式时间: {} -> {}", str, result);
            return result;
        } catch (Exception e) {
            // 继续
        }

        log.warn("无法解析时间字符串: {}", str);
        return null;
    }
}