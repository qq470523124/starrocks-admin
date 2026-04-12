package com.starrocks.admin.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String avatar;
    private OffsetDateTime createdAt;
    private Long organizationId;
    private String organizationName;

    @JsonProperty("is_super_admin")
    private boolean isSuperAdmin;

    @JsonProperty("is_org_admin")
    private boolean isOrgAdmin;

    public static UserResponse from(com.starrocks.admin.model.entity.User user,
                                     boolean isSuperAdmin, boolean isOrgAdmin) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .createdAt(user.getCreatedAt())
                .organizationId(user.getOrganizationId())
                .isSuperAdmin(isSuperAdmin)
                .isOrgAdmin(isOrgAdmin)
                .build();
    }

    public static UserResponse fromWithOrg(com.starrocks.admin.model.entity.User user,
                                           String organizationName,
                                           boolean isSuperAdmin, boolean isOrgAdmin) {
        UserResponse resp = from(user, isSuperAdmin, isOrgAdmin);
        resp.setOrganizationName(organizationName);
        return resp;
    }
}
