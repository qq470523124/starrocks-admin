package com.starrocks.admin.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileListItemResponse {
    @JsonProperty("QueryId")
    private String queryId;
    @JsonProperty("StartTime")
    private String startTime;
    @JsonProperty("Time")
    private String time;
    @JsonProperty("State")
    private String state;
    @JsonProperty("Statement")
    private String statement;
}
