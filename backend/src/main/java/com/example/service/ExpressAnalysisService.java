package com.example.service;

import com.example.dto.ExpressAnalysisDTO;
import com.example.entity.ExpressAnalysis;
import com.example.repository.ExpressAnalysisRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpressAnalysisService {

    private static final List<String> FEE_FIELD_NAMES = Arrays.asList(
            "\u8fd0\u8d39", "\u8d39\u7528", "\u5feb\u9012\u8d39", "\u7269\u6d41\u8d39", "\u5feb\u9012\u8d39\u7528",
            "fee", "shippingFee", "expressFee");

    private final ExpressAnalysisRepository expressAnalysisRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public Page<ExpressAnalysisDTO> getExpressAnalysisList(
            String category,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String receiverAddress,
            LocalDate sentTimeStart,
            LocalDate sentTimeEnd,
            Pageable pageable) {

        String sentTimeStartStr = sentTimeStart != null ? sentTimeStart.format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
        String sentTimeEndStr = sentTimeEnd != null ? sentTimeEnd.format(DateTimeFormatter.ISO_LOCAL_DATE) : null;

        return expressAnalysisRepository.findByFilters(
                category, startDate, endDate, receiverAddress, sentTimeStartStr, sentTimeEndStr, pageable
        ).map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public ExpressAnalysisDTO getById(UUID id) {
        ExpressAnalysis entity = expressAnalysisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record does not exist"));
        return convertToDTO(entity);
    }

    @Transactional(readOnly = true)
    public List<String> getAllColumnNames() {
        return expressAnalysisRepository.findAll().stream()
                .flatMap(record -> parseDynamicFields(record).keySet().stream())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getAllFileNames() {
        return expressAnalysisRepository.findAllFileNames();
    }

    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return expressAnalysisRepository.findAllCategories();
    }

    @Transactional
    public void delete(UUID id) {
        ExpressAnalysis entity = expressAnalysisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record does not exist"));
        expressAnalysisRepository.delete(entity);
    }

    @Transactional
    public void clearAll() {
        expressAnalysisRepository.deleteAllInBatch();
        log.info("Cleared all express analysis data");
    }

    @Transactional
    public void deleteByFileName(String fileName) {
        expressAnalysisRepository.deleteByFileName(fileName);
        log.info("Deleted express analysis records for file={}", fileName);
    }

    @Transactional(readOnly = true)
    public long count() {
        return expressAnalysisRepository.count();
    }

    @Transactional(readOnly = true)
    public long countByCategory(String category) {
        return expressAnalysisRepository.countByCategory(category);
    }

    @Transactional(readOnly = true)
    public Double getAverageFee() {
        List<Double> fees = expressAnalysisRepository.findAll().stream()
                .map(this::extractFee)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());

        if (fees.isEmpty()) {
            return 0.0;
        }

        double total = fees.stream().mapToDouble(Double::doubleValue).sum();
        return total / fees.size();
    }

    private Optional<Double> extractFee(ExpressAnalysis entity) {
        Map<String, Object> fields = parseDynamicFields(entity);
        return FEE_FIELD_NAMES.stream()
                .map(fields::get)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .map(this::parseAmount)
                .flatMap(Optional::stream)
                .findFirst();
    }

    private Optional<Double> parseAmount(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        String normalized = value.replaceAll("[^0-9.\\-]", "");
        if (normalized.isBlank() || "-".equals(normalized) || ".".equals(normalized)) {
            return Optional.empty();
        }

        try {
            return Optional.of(Double.parseDouble(normalized));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private ExpressAnalysisDTO convertToDTO(ExpressAnalysis entity) {
        return ExpressAnalysisDTO.builder()
                .id(entity.getId())
                .category(entity.getCategory())
                .fileName(entity.getFileName())
                .sheetName(entity.getSheetName())
                .rowNum(entity.getRowNum())
                .duration(entity.getDuration())
                .durationHours(entity.getDurationHours())
                .dynamicFields(parseDynamicFields(entity))
                .importedAt(entity.getImportedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private Map<String, Object> parseDynamicFields(ExpressAnalysis entity) {
        if (entity.getDynamicFields() == null || entity.getDynamicFields().isBlank()) {
            return new HashMap<>();
        }

        try {
            return objectMapper.readValue(entity.getDynamicFields(), new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse dynamic fields for express record={}", entity.getId(), e);
            return new HashMap<>();
        }
    }
}
