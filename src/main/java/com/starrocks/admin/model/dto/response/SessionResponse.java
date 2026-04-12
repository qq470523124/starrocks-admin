package com.starrocks.admin.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SessionResponse {
    @JsonProperty("ThreadId")
    private String threadId;
    @JsonProperty("QueryId")
    private String queryId;
    @JsonProperty("User")
    private String user;
    @JsonProperty("DefaultDb")
    private String defaultDb;
    @JsonProperty("Command")
    private String command;
    @JsonProperty("StartTime")
    private String startTime;
    @JsonProperty("QueryTime")
    private String queryTime;
    @JsonProperty("State")
    private String state;
    @JsonProperty("Info")
    private String info;
    @JsonProperty("ConnectionId")
    private String connectionId;
}
