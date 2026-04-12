package com.starrocks.admin.model.dto.request;

import lombok.Data;

@Data
public class UpdateVariableRequest {
    private String value;
    private String scope = "GLOBAL";
}
