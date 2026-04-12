package com.starrocks.admin.model.dto.request;

import lombok.Data;

@Data
public class AlterMaterializedViewRequest {
    private String alterClause;
}
