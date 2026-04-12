package com.starrocks.admin.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrgContext {
    private Long userId;
    private String username;
    private Long organizationId;
    private boolean isSuperAdmin;
}
