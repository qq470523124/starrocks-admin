package com.starrocks.admin.model.dto.request;

import lombok.Data;

@Data
public class QueryExecuteRequest {
    private String sql;
    private String database;
    private Integer limit = 500;
    private Boolean autoLimit = true;
}
