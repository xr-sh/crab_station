package com.example.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.example.entity.ExpressAnalysis;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DateUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class DynamicExcelListener extends AnalysisEventListener<Map<Integer, String>> {

    private static final String[] RECEIVED_TIME_KEYS = {
            "\u7b7e\u6536\u65f6\u95f4", "\u7b7e\u6536\u65e5\u671f", "\u7b7e\u6536",
            "\u6536\u8d27\u65f6\u95f4", "\u6536\u8d27\u65e5\u671f"
    };
    private static final String[] SENT_TIME_KEYS = {
            "\u5bc4\u4ef6\u65f6\u95f4", "\u5bc4\u4ef6\u65e5\u671f", "\u5bc4\u4ef6",
            "\u53d1\u8d27\u65f6\u95f4", "\u53d1\u8d27\u65e5\u671f", "\u53d1\u51fa\u65f6\u95f4", "\u53d1\u51fa\u65e5\u671f"
    };

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Getter
    private final List<String> headers = new ArrayList<>();
    private final List<Integer> headerIndices = new ArrayList<>();
    @Getter
    private final List<ExpressAnalysis> dataList = new ArrayList<>();
    @Getter
    private final List<String> errors = new ArrayList<>();

    private final String category;
    private final String fileName;
    private final boolean hasHeader;
    private String sheetName;
    private int sheetIndex = 0;
    private boolean headerParsed = false;

    @Getter
    private int totalRows = 0;
    @Getter
    private int successRows = 0;

    public DynamicExcelListener(String fileName, String category) {
        this(fileName, category, true);
    }

    public DynamicExcelListener(String fileName, String category, boolean hasHeader) {
        this.fileName = fileName;
        this.category = category;
        this.hasHeader = hasHeader;
    }

    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext context) {
        try {
            if (!headerParsed) {
                if (hasHeader) {
                    parseHeader(data);
                    headerParsed = true;
                    return;
                }
                generateDefaultHeaders(data);
                headerParsed = true;
            }

            Map<String, Object> dynamicFields = buildDynamicFields(data);
            if (isBlankRow(dynamicFields)) {
                return;
            }

            totalRows++;
            Integer durationHours = calculateDurationHours(dynamicFields);
            ExpressAnalysis express = ExpressAnalysis.builder()
                    .category(category)
                    .fileName(fileName)
                    .sheetName(sheetName != null ? sheetName : "Sheet" + sheetIndex)
                    .rowNum(context.readRowHolder().getRowIndex() + 1)
                    .dynamicFields(objectMapper.writeValueAsString(dynamicFields))
                    .durationHours(durationHours)
                    .duration(formatDuration(durationHours))
                    .importedAt(LocalDateTime.now())
                    .build();

            dataList.add(express);
            successRows++;
        } catch (JsonProcessingException e) {
            recordError(context, "JSON parse failed: " + e.getMessage(), e);
        } catch (Exception e) {
            recordError(context, "Row processing failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("Excel parsed, file={}, category={}, sheet={}, totalRows={}, successRows={}, failedRows={}",
                fileName, category, sheetName, totalRows, successRows, getFailedRows());
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public void setSheetIndex(int sheetIndex) {
        this.sheetIndex = sheetIndex;
    }

    public int getFailedRows() {
        return errors.size();
    }

    private void parseHeader(Map<Integer, String> headMap) {
        headers.clear();
        headerIndices.clear();
        headMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String header = entry.getValue();
                    headers.add(header != null && !header.trim().isEmpty() ? header.trim() : "Column" + (entry.getKey() + 1));
                    headerIndices.add(entry.getKey());
                });
    }

    private void generateDefaultHeaders(Map<Integer, String> dataMap) {
        headers.clear();
        headerIndices.clear();
        dataMap.keySet().stream()
                .sorted(Comparator.naturalOrder())
                .forEach(index -> {
                    headers.add("Column" + (index + 1));
                    headerIndices.add(index);
                });
    }

    private Map<String, Object> buildDynamicFields(Map<Integer, String> data) {
        Map<String, Object> dynamicFields = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i);
            Integer columnIndex = headerIndices.get(i);
            String value = data.get(columnIndex);
            dynamicFields.put(header, value != null ? value.trim() : "");
        }
        return dynamicFields;
    }

    private boolean isBlankRow(Map<String, Object> fields) {
        return fields.values().stream().allMatch(value -> value == null || String.valueOf(value).trim().isEmpty());
    }

    private void recordError(AnalysisContext context, String message, Exception e) {
        String error = String.format("Row %d: %s", context.readRowHolder().getRowIndex() + 1, message);
        errors.add(error);
        log.error(error, e);
    }

    private Integer calculateDurationHours(Map<String, Object> dynamicFields) {
        String receivedTime = firstPresent(dynamicFields, RECEIVED_TIME_KEYS);
        String sentTime = firstPresent(dynamicFields, SENT_TIME_KEYS);
        if (receivedTime == null || sentTime == null) {
            return null;
        }

        LocalDateTime receivedDate = parseDateTime(receivedTime);
        LocalDateTime sentDate = parseDateTime(sentTime);
        if (receivedDate == null || sentDate == null) {
            return null;
        }

        long totalHours = ChronoUnit.HOURS.between(sentDate, receivedDate);
        if (totalHours < 0 || totalHours > Integer.MAX_VALUE) {
            return null;
        }
        return (int) totalHours;
    }

    private String firstPresent(Map<String, Object> fields, String... keys) {
        for (String key : keys) {
            Object value = fields.get(key);
            if (value != null && !String.valueOf(value).trim().isEmpty()) {
                return String.valueOf(value).trim();
            }
        }
        return null;
    }

    private String formatDuration(Integer totalHours) {
        if (totalHours == null || totalHours < 0) {
            return null;
        }
        long days = totalHours / 24;
        long hours = totalHours % 24;
        return days + "\u5929" + hours + "\u5c0f\u65f6";
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        String str = stripQuotes(dateTimeStr.trim());

        LocalDateTime dateTime = parseDateTimePatterns(str);
        if (dateTime != null) {
            return dateTime;
        }

        LocalDate date = parseDatePatterns(str);
        if (date != null) {
            return date.atStartOfDay();
        }

        LocalDateTime excelDate = parseExcelDate(str);
        if (excelDate != null) {
            return excelDate;
        }

        try {
            return LocalDateTime.parse(str.replace(" ", "T"));
        } catch (Exception ignored) {
            return null;
        }
    }

    private LocalDateTime parseDateTimePatterns(String str) {
        String[] patterns = {
                "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-M-d HH:mm:ss",
                "yyyy-M-d HH:mm", "yyyy-M-d H:mm:ss", "yyyy-M-d H:mm",
                "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/M/d HH:mm:ss",
                "yyyy/M/d HH:mm", "MM/dd/yyyy HH:mm:ss", "MM/dd/yyyy HH:mm",
                "M/d/yyyy HH:mm:ss", "M/d/yyyy HH:mm", "yyyyMMdd HHmmss"
        };
        for (String pattern : patterns) {
            try {
                return LocalDateTime.parse(str, DateTimeFormatter.ofPattern(pattern));
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private LocalDate parseDatePatterns(String str) {
        String[] patterns = {
                "yyyy-MM-dd", "yyyy-M-d", "yyyy/MM/dd", "yyyy/M/d",
                "yyyyMMdd", "MM/dd/yyyy", "M/d/yyyy"
        };
        for (String pattern : patterns) {
            try {
                return LocalDate.parse(str, DateTimeFormatter.ofPattern(pattern));
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private LocalDateTime parseExcelDate(String str) {
        try {
            if (!str.matches("^\\d+(\\.\\d+)?$")) {
                return null;
            }
            Date date = DateUtil.getJavaDate(Double.parseDouble(str), false);
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        } catch (Exception ignored) {
            return null;
        }
    }

    private String stripQuotes(String value) {
        if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
