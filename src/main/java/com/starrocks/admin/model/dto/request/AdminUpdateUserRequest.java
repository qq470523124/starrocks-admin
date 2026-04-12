package com.starrocks.admin.model.dto.request;

import lombok.Data;

@Data
public class AdminUpdateUserRequest {
    private String email;
    private String avatar;
    private String password;
    private java.util.List<Long> roleIds;
    private Long organizationId;
}
