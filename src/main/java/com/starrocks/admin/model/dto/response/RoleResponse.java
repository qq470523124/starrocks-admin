package com.starrocks.admin.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Boolean isSystem;
    private Long organizationId;
    private OffsetDateTime createdAt;

    public static RoleResponse from(com.starrocks.admin.model.entity.Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .description(role.getDescription())
                .isSystem(role.getIsSystem())
                .organizationId(role.getOrganizationId())
                .createdAt(role.getCreatedAt())
                .build();
    }
}
