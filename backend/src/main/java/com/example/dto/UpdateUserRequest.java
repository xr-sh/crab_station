package com.example.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Email(message = "Invalid email format")
    private String email;

    @Size(max = 20, message = "Phone length must be at most 20 characters")
    private String phone;

    private String avatar;

    private Integer status;

    private String role;

    @Size(min = 6, max = 100, message = "Password length must be between 6 and 100 characters")
    private String password;
}
