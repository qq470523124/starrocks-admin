package com.starrocks.admin.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PermissionResponse {
    private Long id;
    private String code;
    private String name;
    private String type;
    private String resource;
    private String action;
    private Long parentId;
    private String description;

    public static PermissionResponse from(com.starrocks.admin.model.entity.Permission p) {
        return PermissionResponse.builder()
                .id(p.getId())
                .code(p.getCode())
                .name(p.getName())
                .type(p.getType())
                .resource(p.getResource())
                .action(p.getAction())
                .parentId(p.getParentId())
                .description(p.getDescription())
                .build();
    }
}
