package com.example.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PlatformPackageItemDTO {
    private UUID id;
    private UUID platformSpecId;
    private String platformSpecName;
    private String platformSpecCategory;
    private Integer sortNo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
