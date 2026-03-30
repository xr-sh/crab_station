package com.example.dto;

import lombok.Data;

@Data
public class UpdatePlatformSpecRequest {

    private String name;

    private Integer status;

    private String remark;
}