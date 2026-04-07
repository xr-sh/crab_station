package com.example.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.example.dto.ImportResultDTO;
import com.example.entity.ExpressAnalysis;
import com.example.listener.DynamicExcelListener;
import com.example.repository.ExpressAnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Excel导入服务
 * 支持多文件上传和动态列识别
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelImportService {

    private final ExpressAnalysisRepository expressAnalysisRepository;

    /**
     * 批量导入Excel文件
     * 
     * @param files 上传的文件列表
     * @param category 快递类别（顺丰/京东）
     * @return 导入结果列表
     */
    @Transactional
    public List<ImportResultDTO> importExcelFiles(MultipartFile[] files, String category) {
        List<ImportResultDTO> results = new ArrayList<>();

        for (MultipartFile file : files) {
            ImportResultDTO result = importSingleFile(file, category);
            results.add(result);
        }

        return results;
    }

    /**
     * 导入单个Excel文件
     */
    @Transactional
    public ImportResultDTO importSingleFile(MultipartFile file, String category) {
        String fileName = file.getOriginalFilename();
        
        try {
            log.info("开始导入文件: {}, 类别: {}", fileName, category);

            // 创建监听器
            DynamicExcelListener listener = new DynamicExcelListener(fileName, category);

            // 使用EasyExcel读取文件
            ExcelReader excelReader = EasyExcel.read(file.getInputStream(), listener).build();

            // 获取所有Sheet
            List<ReadSheet> sheets = excelReader.excelExecutor().sheetList();
            
            int totalRows = 0;
            int successRows = 0;
            int failedRows = 0;
            Set<String> allColumns = new HashSet<>();
            List<String> allErrors = new ArrayList<>();

            // 遍历所有Sheet
            for (int i = 0; i < sheets.size(); i++) {
                ReadSheet sheet = sheets.get(i);
                
                // 为每个Sheet创建新的监听器实例
                DynamicExcelListener sheetListener = new DynamicExcelListener(fileName, category);
                sheetListener.setSheetName(sheet.getSheetName());
                sheetListener.setSheetIndex(i);

                // 重新创建reader并读取当前sheet
                ExcelReader sheetReader = EasyExcel.read(file.getInputStream(), sheetListener).build();
                sheetReader.read(sheet);

                // 收集数据
                List<ExpressAnalysis> sheetData = sheetListener.getDataList();
                if (!sheetData.isEmpty()) {
                    // 批量保存
                    expressAnalysisRepository.saveAll(sheetData);
                    log.info("Sheet [{}] 保存了 {} 条数据", sheet.getSheetName(), sheetData.size());
                }

                // 统计
                totalRows += sheetListener.getTotalRows();
                successRows += sheetListener.getSuccessRows();
                failedRows += sheetListener.getFailedRows();
                allColumns.addAll(sheetListener.getHeaders());
                allErrors.addAll(sheetListener.getErrors());

                sheetReader.finish();
            }

            excelReader.finish();

            // 构建结果
            return ImportResultDTO.builder()
                    .category(category)
                    .fileName(fileName)
                    .totalRows(totalRows)
                    .successRows(successRows)
                    .failedRows(failedRows)
                    .columns(new ArrayList<>(allColumns))
                    .errors(allErrors)
                    .success(true)
                    .message(String.format("导入完成: 成功 %d 行, 失败 %d 行", successRows, failedRows))
                    .build();

        } catch (IOException e) {
            log.error("文件读取失败: {}", fileName, e);
            return ImportResultDTO.builder()
                    .category(category)
                    .fileName(fileName)
                    .success(false)
                    .message("文件读取失败: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("导入失败: {}", fileName, e);
            return ImportResultDTO.builder()
                    .category(category)
                    .fileName(fileName)
                    .success(false)
                    .message("导入失败: " + e.getMessage())
                    .build();
        }
    }
}