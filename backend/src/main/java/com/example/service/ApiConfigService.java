package com.example.service;

import com.example.dto.CreateApiConfigRequest;
import com.example.dto.UpdateApiConfigRequest;
import com.example.entity.ApiConfig;
import com.example.repository.ApiConfigRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApiConfigService {

    private final ApiConfigRepository apiConfigRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public Page<ApiConfig> getApiConfigs(String platformName, Integer status, Pageable pageable) {
        return apiConfigRepository.findByFilters(platformName, status, pageable);
    }

    @Transactional(readOnly = true)
    public List<ApiConfig> getAllApiConfigs() {
        return apiConfigRepository.findByStatusOrderByCreatedAtDesc(1);
    }

    @Transactional(readOnly = true)
    public ApiConfig getApiConfigById(UUID id) {
        return apiConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("API配置不存在"));
    }

    @Transactional(readOnly = true)
    public ApiConfig getApiConfigByPlatformName(String platformName) {
        return apiConfigRepository.findByPlatformName(platformName)
                .orElseThrow(() -> new RuntimeException("平台API配置不存在: " + platformName));
    }

    @Transactional
    public ApiConfig createApiConfig(CreateApiConfigRequest request) {
        if (apiConfigRepository.existsByPlatformName(request.getPlatformName())) {
            throw new RuntimeException("该平台的API配置已存在");
        }

        ApiConfig config = ApiConfig.builder()
                .platformName(request.getPlatformName())
                .apiKey(request.getApiKey())
                .secret(request.getSecret())
                .baseUrl(request.getBaseUrl())
                .dynamicConfig(convertDynamicConfigToJson(request.getDynamicConfig()))
                .status(request.getStatus() != null ? request.getStatus() : 1)
                .remark(request.getRemark())
                .build();
        return apiConfigRepository.save(config);
    }

    @Transactional
    public ApiConfig updateApiConfig(UUID id, UpdateApiConfigRequest request) {
        ApiConfig config = getApiConfigById(id);

        if (request.getPlatformName() != null && !request.getPlatformName().equals(config.getPlatformName())) {
            if (apiConfigRepository.existsByPlatformName(request.getPlatformName())) {
                throw new RuntimeException("该平台的API配置已存在");
            }
            config.setPlatformName(request.getPlatformName());
        }

        if (request.getApiKey() != null) {
            config.setApiKey(request.getApiKey());
        }
        if (request.getSecret() != null) {
            config.setSecret(request.getSecret());
        }
        if (request.getBaseUrl() != null) {
            config.setBaseUrl(request.getBaseUrl());
        }
        if (request.getDynamicConfig() != null) {
            config.setDynamicConfig(convertDynamicConfigToJson(request.getDynamicConfig()));
        }
        if (request.getStatus() != null) {
            config.setStatus(request.getStatus());
        }
        if (request.getRemark() != null) {
            config.setRemark(request.getRemark());
        }

        return apiConfigRepository.save(config);
    }

    @Transactional
    public void deleteApiConfig(UUID id) {
        ApiConfig config = getApiConfigById(id);
        apiConfigRepository.delete(config);
    }

    private String convertDynamicConfigToJson(Map<String, Object> dynamicConfig) {
        if (dynamicConfig == null || dynamicConfig.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(dynamicConfig);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("动态配置JSON转换失败: " + e.getMessage());
        }
    }

    public Map<String, Object> parseDynamicConfig(String dynamicConfigJson) {
        if (dynamicConfigJson == null || dynamicConfigJson.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(dynamicConfigJson, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("动态配置JSON解析失败: " + e.getMessage());
        }
    }
}