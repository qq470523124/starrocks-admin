package com.starrocks.admin.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueryResultResponse {
    private List<String> columns;
    private List<List<String>> rows;
    private Long affectedRows;
    private Long executionTimeMs;
    private String error;
    private String queryId;
}
