package com.example.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class PurchaseRecordDTO {
    private UUID id;
    private LocalDate purchaseDate;
    private String supplier;
    private BigDecimal totalWeight;
    private BigDecimal totalAmount;
    private String remark;
    private List<PurchaseItemDTO> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
