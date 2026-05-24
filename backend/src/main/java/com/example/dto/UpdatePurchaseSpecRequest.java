package com.example.dto;

import lombok.Data;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

@Data
public class UpdatePurchaseSpecRequest {

    @Pattern(regexp = "\\d+(\\.\\d+)?-\\d+(\\.\\d+)?", message = "规格范围格式必须为数字-数字，例如2.3-2.6")
    private String name;

    private String category;

    private BigDecimal price;

    private String priceChangeReason;

    private Integer status;

    private String remark;
}
