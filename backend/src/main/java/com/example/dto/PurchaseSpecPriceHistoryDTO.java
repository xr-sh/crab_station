package com.example.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PurchaseSpecPriceHistoryDTO {
    private UUID id;
    private UUID purchaseSpecId;
    private String purchaseSpecName;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private String changeReason;
    private UUID changedBy;
    private LocalDateTime changedAt;
    private LocalDateTime createdAt;
}
