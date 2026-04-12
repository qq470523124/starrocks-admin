package com.starrocks.admin.model.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class UpdateRolePermissionsRequest {
    @NotEmpty
    private java.util.List<Long> permissionIds;
}
