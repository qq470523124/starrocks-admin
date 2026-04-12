package com.starrocks.admin.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminCreateUserRequest {

    @NotBlank @Size(min = 2, max = 50)
    private String username;

    @NotBlank @Size(min = 4, max = 100)
    private String password;

    private String email;
    private String avatar;
    private java.util.List<Long> roleIds;
    private Long organizationId;
}
