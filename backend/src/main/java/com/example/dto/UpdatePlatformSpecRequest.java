package com.example.dto;

import lombok.Data;
import jakarta.validation.constraints.Pattern;

@Data
public class UpdatePlatformSpecRequest {

    @Pattern(regexp = "\\d+(\\.\\d+)?-\\d+(\\.\\d+)?", message = "规格范围格式必须为数字-数字，例如2.3-2.6")
    private String name;

    private String category;

    private Integer status;

    private String remark;
}
