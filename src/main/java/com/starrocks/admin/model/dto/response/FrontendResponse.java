package com.starrocks.admin.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FrontendResponse {
    @JsonProperty("Name")
    private String name;
    @JsonProperty("Host")
    private String host;
    @JsonProperty("EditLogPort")
    private Integer editLogPort;
    @JsonProperty("HttpPort")
    private Integer httpPort;
    @JsonProperty("QueryPort")
    private Integer queryPort;
    @JsonProperty("RpcPort")
    private Integer rpcPort;
    @JsonProperty("Role")
    private String role;
    @JsonProperty("IsMaster")
    private Boolean isMaster;
    @JsonProperty("ClusterId")
    private String clusterId;
    @JsonProperty("JoinTime")
    private String joinTime;
    @JsonProperty("StartTime")
    private String startTime;
    @JsonProperty("HeartbeatPort")
    private Integer heartbeatPort;
    @JsonProperty("Alive")
    private Boolean alive;
    @JsonProperty("ReplayedJournalId")
    private Long replayedJournalId;
}
