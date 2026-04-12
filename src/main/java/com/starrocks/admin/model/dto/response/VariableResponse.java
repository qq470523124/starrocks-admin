package com.starrocks.admin.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VariableResponse {
    @JsonProperty("Variable_name")
    private String variableName;
    @JsonProperty("Value")
    private String value;
    @JsonProperty("Default_value")
    private String defaultValue;
    @JsonProperty("Type")
    private String type;
    @JsonProperty("Description")
    private String description;
}
