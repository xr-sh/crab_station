package com.example.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class UpdatePurchaseItemRequest {

    private UUID id;

    @NotNull(message = "规格不能为空")
    private UUID purchaseSpecId;

    @NotNull(message = "重量不能为空")
    @Positive(message = "重量必须大于0")
    private BigDecimal weight;

    @NotNull(message = "单价不能为空")
    @Positive(message = "单价必须大于0")
    private BigDecimal unitPrice;
}
