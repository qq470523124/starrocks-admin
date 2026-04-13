package com.starrocks.admin.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SessionResponse {
    @JsonProperty("id")
    private String id;
    @JsonProperty("user")
    private String user;
    @JsonProperty("host")
    private String host;
    @JsonProperty("db")
    private String db;
    @JsonProperty("command")
    private String command;
    @JsonProperty("time")
    private String time;
    @JsonProperty("state")
    private String state;
    @JsonProperty("info")
    private String info;
}
