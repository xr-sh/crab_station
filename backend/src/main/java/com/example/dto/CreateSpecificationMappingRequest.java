package com.example.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateSpecificationMappingRequest {

    @NotNull(message = "类别不能为空")
    private String category;

    @NotNull(message = "进货规格不能为空")
    private UUID purchaseSpecId;

    @NotNull(message = "平台规格不能为空")
    private UUID platformSpecId;

    private Integer status;

    private String remark;
}
