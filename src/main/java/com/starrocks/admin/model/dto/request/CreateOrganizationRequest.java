package com.starrocks.admin.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateOrganizationRequest {
    @NotBlank @Size(max = 50)
    private String code;
    @NotBlank @Size(max = 100)
    private String name;
    private String description;
    private String adminUsername;
    private String adminPassword;
    private String adminEmail;
    private Long adminUserId;
}
