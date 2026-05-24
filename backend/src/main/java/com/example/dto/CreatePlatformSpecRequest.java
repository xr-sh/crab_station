package com.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreatePlatformSpecRequest {

    @NotBlank(message = "规格名称不能为空")
    @Pattern(regexp = "\\d+(\\.\\d+)?-\\d+(\\.\\d+)?", message = "规格范围格式必须为数字-数字，例如2.3-2.6")
    private String name;

    private String category;

    private Integer status;

    private String remark;
}
