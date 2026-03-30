package com.example.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePlatformSpecRequest {

    @NotBlank(message = "规格名称不能为空")
    private String name;

    private Integer status;

    private String remark;
}