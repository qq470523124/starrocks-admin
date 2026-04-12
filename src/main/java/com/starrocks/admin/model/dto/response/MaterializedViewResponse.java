package com.starrocks.admin.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaterializedViewResponse {
    private String id;
    private String name;
    private String databaseName;
    private String refreshType;
    @JsonProperty("is_active")
    private boolean isActive;
    private String partitionType;
    private String taskId;
    private String taskName;
    private String lastRefreshStartTime;
    private String lastRefreshFinishedTime;
    private String lastRefreshDuration;
    private String lastRefreshState;
    private String lastError;
    private String rows;
    private String text;
    private String refreshInterval;
    private String lastRefreshForceRefresh;
    private String partitionStart;
    private String partitionEnd;
}
