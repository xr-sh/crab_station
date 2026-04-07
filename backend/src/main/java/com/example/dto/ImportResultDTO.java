package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Excel导入结果DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportResultDTO {

    /**
     * 快递类别（顺丰/京东）
     */
    private String category;

    /**
     * 导入的文件名
     */
    private String fileName;

    /**
     * 总行数
     */
    private Integer totalRows;

    /**
     * 成功导入行数
     */
    private Integer successRows;

    /**
     * 失败行数
     */
    private Integer failedRows;

    /**
     * 识别到的列名列表
     */
    @Builder.Default
    private List<String> columns = new ArrayList<>();

    /**
     * 错误信息列表
     */
    @Builder.Default
    private List<String> errors = new ArrayList<>();

    /**
     * 导入是否成功
     */
    private Boolean success;

    /**
     * 提示消息
     */
    private String message;
}