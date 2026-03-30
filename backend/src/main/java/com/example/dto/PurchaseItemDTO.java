package com.example.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PurchaseItemDTO {
    private UUID id;
    private UUID purchaseSpecId;
    private String purchaseSpecName;
    private BigDecimal weight;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
