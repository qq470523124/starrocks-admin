package com.starrocks.admin.model.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateFunctionRequest {
    @Size(max = 100)
    private String categoryName;
    @Size(max = 100)
    private String functionName;
    @Size(max = 500)
    private String description;
    private String sqlQuery;
}
