package com.starrocks.admin.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SystemFunctionDetailResponse {
    private String functionName;
    private String description;
    private List<Map<String, String>> data;
    private int totalCount;
    private java.time.OffsetDateTime lastUpdated;
}
