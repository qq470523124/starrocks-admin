package com.starrocks.admin.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.starrocks.admin.model.enums.HealthStatus;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClusterHealthResponse {
    private HealthStatus status;
    private List<HealthCheck> checks;
    private OffsetDateTime lastCheckTime;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HealthCheck {
        private String name;
        private String status;
        private String message;
    }
}
