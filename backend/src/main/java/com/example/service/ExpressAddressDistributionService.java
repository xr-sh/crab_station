package com.example.service;

import com.example.entity.ExpressAnalysis;
import com.example.repository.ExpressAnalysisRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpressAddressDistributionService {

    private static final List<String> RECEIVER_ADDRESS_FIELD_NAMES = Arrays.asList(
            "\u6536\u4ef6\u5730\u5740", "\u6536\u4ef6\u4eba\u5730\u5740", "\u6536\u8d27\u5730\u5740",
            "\u6536\u8d27\u4eba\u5730\u5740", "\u6536\u4ef6\u4eba\u8be6\u7ec6\u5730\u5740",
            "receiverAddress", "receiver_address", "address");

    private static final List<String> MUNICIPALITIES = Arrays.asList(
            "\u5317\u4eac", "\u4e0a\u6d77", "\u5929\u6d25", "\u91cd\u5e86");

    private static final List<String> PROVINCE_NAMES = Arrays.asList(
            "\u5317\u4eac", "\u5929\u6d25", "\u4e0a\u6d77", "\u91cd\u5e86", "\u6cb3\u5317", "\u5c71\u897f",
            "\u8fbd\u5b81", "\u5409\u6797", "\u9ed1\u9f99\u6c5f", "\u6c5f\u82cf", "\u6d59\u6c5f", "\u5b89\u5fbd",
            "\u798f\u5efa", "\u6c5f\u897f", "\u5c71\u4e1c", "\u6cb3\u5357", "\u6e56\u5317", "\u6e56\u5357",
            "\u5e7f\u4e1c", "\u6d77\u5357", "\u56db\u5ddd", "\u8d35\u5dde", "\u4e91\u5357", "\u9655\u897f",
            "\u7518\u8083", "\u9752\u6d77", "\u53f0\u6e7e", "\u5185\u8499\u53e4", "\u5e7f\u897f", "\u897f\u85cf",
            "\u5b81\u590f", "\u65b0\u7586", "\u9999\u6e2f", "\u6fb3\u95e8");

    private final ExpressAnalysisRepository expressAnalysisRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public Map<String, Object> getReceiverAddressDistribution() {
        Map<String, Long> provinceStats = new HashMap<>();
        Map<String, Long> cityStats = new HashMap<>();
        long matchedCount = 0;
        long unmatchedCount = 0;

        for (ExpressAnalysis entity : expressAnalysisRepository.findAll()) {
            Optional<AddressParts> addressParts = extractReceiverAddress(entity).flatMap(this::parseAddressParts);
            if (addressParts.isPresent()) {
                AddressParts parts = addressParts.get();
                provinceStats.merge(parts.province(), 1L, Long::sum);
                cityStats.merge(parts.city(), 1L, Long::sum);
                matchedCount++;
            } else {
                unmatchedCount++;
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("provinceStats", sortStats(provinceStats));
        result.put("cityStats", sortStats(cityStats));
        result.put("matchedCount", matchedCount);
        result.put("unmatchedCount", unmatchedCount);
        return result;
    }

    private Optional<String> extractReceiverAddress(ExpressAnalysis entity) {
        Map<String, Object> fields = parseDynamicFields(entity);
        return RECEIVER_ADDRESS_FIELD_NAMES.stream()
                .map(fields::get)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .findFirst();
    }

    private Optional<AddressParts> parseAddressParts(String address) {
        String normalized = address.replaceAll("\\s+", "");
        Optional<String> province = PROVINCE_NAMES.stream()
                .filter(normalized::contains)
                .findFirst();

        if (province.isEmpty()) {
            return Optional.empty();
        }

        String provinceName = province.get();
        if (MUNICIPALITIES.contains(provinceName)) {
            return Optional.of(new AddressParts(provinceName, provinceName));
        }

        int provinceIndex = normalized.indexOf(provinceName);
        String remaining = stripProvinceSuffix(normalized.substring(provinceIndex + provinceName.length()));
        String city = extractCity(remaining).orElse(provinceName);
        return Optional.of(new AddressParts(provinceName, city));
    }

    private String stripProvinceSuffix(String text) {
        return text.replaceFirst("^(\u7ef4\u543e\u5c14\u81ea\u6cbb\u533a|\u58ee\u65cf\u81ea\u6cbb\u533a|\u56de\u65cf\u81ea\u6cbb\u533a|\u81ea\u6cbb\u533a|\u7279\u522b\u884c\u653f\u533a|\u7701|\u5e02)", "");
    }

    private Optional<String> extractCity(String text) {
        if (text.isBlank()) {
            return Optional.empty();
        }

        int cityEnd = firstPositiveIndex(
                text.indexOf("\u5e02"),
                text.indexOf("\u5dde"),
                text.indexOf("\u76df"));
        if (cityEnd < 0) {
            return Optional.empty();
        }

        return Optional.of(text.substring(0, cityEnd + 1));
    }

    private int firstPositiveIndex(int... indexes) {
        return Arrays.stream(indexes)
                .filter(index -> index >= 0)
                .min()
                .orElse(-1);
    }

    private Map<String, Long> sortStats(Map<String, Long> stats) {
        if (stats.isEmpty()) {
            return Collections.emptyMap();
        }

        return stats.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (left, right) -> left,
                        LinkedHashMap::new));
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

    private record AddressParts(String province, String city) {
    }
}
