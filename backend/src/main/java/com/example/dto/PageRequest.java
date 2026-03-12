package com.example.dto;

import lombok.Data;

@Data
public class PageRequest {
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "id";
    private String sortDirection = "DESC";
}
