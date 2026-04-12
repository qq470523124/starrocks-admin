package com.starrocks.admin.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateRoleRequest {
    @NotBlank @Size(max = 50)
    private String code;
    @NotBlank @Size(max = 100)
    private String name;
    private String description;
    private Long organizationId;
}
