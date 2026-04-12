package com.starrocks.admin.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignUserRoleRequest {
    @NotNull
    private Long roleId;
}
