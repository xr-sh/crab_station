package com.example.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Email(message = "邮箱格式不正确")
    private String email;

    @Size(max = 20, message = "手机号长度不能超过20个字符")
    private String phone;

    private String avatar;

    private Integer status;

    @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
    private String password;
}
