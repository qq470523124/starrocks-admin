package com.starrocks.admin.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PermissionTreeResponse {
    private Long id;
    private String code;
    private String name;
    private String type;
    private String resource;
    private String action;
    private String description;
    private List<PermissionTreeResponse> children;
}
