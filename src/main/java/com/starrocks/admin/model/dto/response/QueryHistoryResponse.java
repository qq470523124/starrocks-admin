package com.starrocks.admin.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueryHistoryResponse {
    private List<QueryHistoryItem> data;
    private long total;
    private long page;
    private long pageSize;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueryHistoryItem {
        private String queryId;
        private String user;
        private String defaultDb;
        private String sqlStatement;
        private String queryType;
        private String startTime;
        private String endTime;
        private long totalMs;
        private String queryState;
        private String warehouse;
    }
}
