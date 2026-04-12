package com.starrocks.admin.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateFunctionRequest {
    @NotBlank @Size(max = 100)
    private String categoryName;
    @NotBlank @Size(max = 100)
    private String functionName;
    @NotBlank @Size(max = 500)
    private String description;
    @NotBlank
    private String sqlQuery;
}
