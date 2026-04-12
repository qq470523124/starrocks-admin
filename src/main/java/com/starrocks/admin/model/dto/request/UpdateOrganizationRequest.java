package com.starrocks.admin.model.dto.request;

import lombok.Data;

@Data
public class UpdateOrganizationRequest {
    private String name;
    private String description;
    private Long adminUserId;
}
