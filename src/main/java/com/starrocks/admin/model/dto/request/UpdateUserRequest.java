package com.starrocks.admin.model.dto.request;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String email;
    private String avatar;
    private String currentPassword;
    private String newPassword;
}
