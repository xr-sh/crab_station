package com.example.dto;

import lombok.Data;

@Data
public class UpdatePlatformSpecRequest {

    private String name;

    private String category;

    private Integer status;

    private String remark;
}
