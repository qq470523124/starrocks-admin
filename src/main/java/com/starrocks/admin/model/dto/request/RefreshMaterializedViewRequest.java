package com.starrocks.admin.model.dto.request;

import lombok.Data;

@Data
public class RefreshMaterializedViewRequest {
    private String partitionStart;
    private String partitionEnd;
    private boolean force = false;
    private String mode = "ASYNC";
}
