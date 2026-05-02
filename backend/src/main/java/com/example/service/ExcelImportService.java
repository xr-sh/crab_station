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

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelImportService {

    private final ExpressAnalysisRepository expressAnalysisRepository;

    @Transactional
    public List<ImportResultDTO> importExcelFiles(MultipartFile[] files, String category, boolean hasHeader) {
        List<ImportResultDTO> results = new ArrayList<>();

        for (MultipartFile file : files) {
            results.add(importSingleFile(file, category, hasHeader));
        }

        return results;
    }

    @Transactional
    public ImportResultDTO importSingleFile(MultipartFile file, String category, boolean hasHeader) {
        String fileName = file.getOriginalFilename();

        try {
            log.info("Importing file={}, category={}, hasHeader={}", fileName, category, hasHeader);

            DynamicExcelListener listener = new DynamicExcelListener(fileName, category, hasHeader);
            ExcelReader excelReader = EasyExcel.read(file.getInputStream(), listener)
                    .headRowNumber(0)
                    .build();

            List<ReadSheet> sheets = excelReader.excelExecutor().sheetList();

            int totalRows = 0;
            int successRows = 0;
            int failedRows = 0;
            Set<String> allColumns = new HashSet<>();
            List<String> allErrors = new ArrayList<>();

            for (int i = 0; i < sheets.size(); i++) {
                ReadSheet sheet = sheets.get(i);
                DynamicExcelListener sheetListener = new DynamicExcelListener(fileName, category, hasHeader);
                sheetListener.setSheetName(sheet.getSheetName());
                sheetListener.setSheetIndex(i);

                ExcelReader sheetReader = EasyExcel.read(file.getInputStream(), sheetListener)
                        .headRowNumber(0)
                        .build();
                sheetReader.read(sheet);

                List<ExpressAnalysis> sheetData = sheetListener.getDataList();
                if (!sheetData.isEmpty()) {
                    expressAnalysisRepository.saveAll(sheetData);
                    log.info("Saved sheet={}, rows={}", sheet.getSheetName(), sheetData.size());
                }

                totalRows += sheetListener.getTotalRows();
                successRows += sheetListener.getSuccessRows();
                failedRows += sheetListener.getFailedRows();
                allColumns.addAll(sheetListener.getHeaders());
                allErrors.addAll(sheetListener.getErrors());

                sheetReader.finish();
            }

            excelReader.finish();

            return ImportResultDTO.builder()
                    .category(category)
                    .fileName(fileName)
                    .totalRows(totalRows)
                    .successRows(successRows)
                    .failedRows(failedRows)
                    .columns(new ArrayList<>(allColumns))
                    .errors(allErrors)
                    .success(true)
                    .message(String.format("Import completed: success %d rows, failed %d rows", successRows, failedRows))
                    .build();

        } catch (IOException e) {
            log.error("Failed to read file={}", fileName, e);
            return ImportResultDTO.builder()
                    .category(category)
                    .fileName(fileName)
                    .success(false)
                    .message("Failed to read file: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Failed to import file={}", fileName, e);
            return ImportResultDTO.builder()
                    .category(category)
                    .fileName(fileName)
                    .success(false)
                    .message("Failed to import file: " + e.getMessage())
                    .build();
        }
    }
}
