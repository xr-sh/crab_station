package com.example.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.example.entity.ExpressAnalysis;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
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
     * 解析每一行数据
     */
    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext context) {
        totalRows++;
        
        try {
            // 第一行作为表头
            if (!headerParsed) {
                parseHeader(data);
                headerParsed = true;
                return;
            }

            // 解析数据行
            Map<String, Object> dynamicFields = new HashMap<>();
            for (int i = 0; i < headers.size(); i++) {
                String header = headers.get(i);
                String value = data.get(i);
                if (header != null && !header.trim().isEmpty()) {
                    dynamicFields.put(header.trim(), value != null ? value : "");
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
     */
    private void parseHeader(Map<Integer, String> headMap) {
        headers.clear();
        for (Map.Entry<Integer, String> entry : headMap.entrySet()) {
            String header = entry.getValue();
            if (header != null && !header.trim().isEmpty()) {
                headers.add(header.trim());
            }
        }
        log.info("解析到表头: {}", headers);
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
}