package com.example.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePurchaseSpecRequest {

    @NotBlank(message = "规格名称不能为空")
    private String name;

    private String category;

    private BigDecimal price;

    private String priceChangeReason;

    private Integer status;

    private String remark;
}
