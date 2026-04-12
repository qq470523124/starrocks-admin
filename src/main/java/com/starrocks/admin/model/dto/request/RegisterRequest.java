package com.starrocks.admin.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 2, max = 50)
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 4, max = 100)
    private String password;

    @Size(max = 100)
    private String email;

    private String avatar;
}
