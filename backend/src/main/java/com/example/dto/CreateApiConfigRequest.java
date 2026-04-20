package com.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

@Data
public class CreateApiConfigRequest {
    
    @NotBlank(message = "平台名称不能为空")
    @Size(max = 100, message = "平台名称长度不能超过100")
    private String platformName;

    @NotBlank(message = "API Key不能为空")
    @Size(max = 200, message = "API Key长度不能超过200")
    private String apiKey;

    @Size(max = 200, message = "Secret长度不能超过200")
    private String secret;

    @Size(max = 500, message = "Base URL长度不能超过500")
    private String baseUrl;

    private Map<String, Object> dynamicConfig;

    private Integer status;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}