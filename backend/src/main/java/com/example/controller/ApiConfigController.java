package com.example.controller;

import com.example.dto.*;
import com.example.entity.ApiConfig;
import com.example.service.ApiConfigService;
import com.example.util.PageRequestUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/api-configs")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"})
@PreAuthorize("hasRole('ADMIN')")
public class ApiConfigController {

    private final ApiConfigService apiConfigService;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "updatedAt", "platformName", "status");

    @GetMapping
    public ResponseEntity<PageResponse<ApiConfigDTO>> getApiConfigs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) String platformName,
            @RequestParam(required = false) Integer status) {

        PageRequest pageRequest = PageRequestUtils.of(page, size, sortBy, sortDirection, ALLOWED_SORT_FIELDS);

        Page<ApiConfig> configPage = apiConfigService.getApiConfigs(platformName, status, pageRequest);

        PageResponse<ApiConfigDTO> response = PageResponse.<ApiConfigDTO>builder()
                .content(configPage.getContent().stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList()))
                .totalPages(configPage.getTotalPages())
                .totalElements(configPage.getTotalElements())
                .size(configPage.getSize())
                .number(configPage.getNumber())
                .first(configPage.isFirst())
                .last(configPage.isLast())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ApiConfigDTO>> getAllApiConfigs() {
        List<ApiConfig> configs = apiConfigService.getAllApiConfigs();
        List<ApiConfigDTO> dtos = configs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiConfigDTO> getApiConfigById(@PathVariable UUID id) {
        ApiConfig config = apiConfigService.getApiConfigById(id);
        return ResponseEntity.ok(convertToDTO(config));
    }

    @GetMapping("/platform/{platformName}")
    public ResponseEntity<ApiConfigDTO> getApiConfigByPlatformName(@PathVariable String platformName) {
        ApiConfig config = apiConfigService.getApiConfigByPlatformName(platformName);
        return ResponseEntity.ok(convertToDTO(config));
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createApiConfig(@Valid @RequestBody CreateApiConfigRequest request) {
        apiConfigService.createApiConfig(request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "API配置创建成功");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> updateApiConfig(@PathVariable UUID id, @Valid @RequestBody UpdateApiConfigRequest request) {
        apiConfigService.updateApiConfig(id, request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "API配置更新成功");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteApiConfig(@PathVariable UUID id) {
        apiConfigService.deleteApiConfig(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "API配置删除成功");
        return ResponseEntity.ok(response);
    }

    private ApiConfigDTO convertToDTO(ApiConfig config) {
        return ApiConfigDTO.builder()
                .id(config.getId())
                .platformName(config.getPlatformName())
                .apiKey(apiConfigService.maskSecret(config.getApiKey()))
                .secret(apiConfigService.maskSecret(config.getSecret()))
                .baseUrl(config.getBaseUrl())
                .dynamicConfig(apiConfigService.parseDynamicConfig(config.getDynamicConfig()))
                .status(config.getStatus())
                .remark(config.getRemark())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }
}
