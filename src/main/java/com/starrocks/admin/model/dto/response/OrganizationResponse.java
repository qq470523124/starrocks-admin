package com.starrocks.admin.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrganizationResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Boolean isSystem;
    private Long adminUserId;
    private OffsetDateTime createdAt;
}
