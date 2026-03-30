package com.example.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class SpecificationMappingDTO {
    private UUID id;
    private UUID purchaseSpecId;
    private String purchaseSpecName;
    private UUID platformSpecId;
    private String platformSpecName;
    private Integer status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}