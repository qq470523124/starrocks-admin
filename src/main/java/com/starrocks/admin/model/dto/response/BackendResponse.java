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
public class BackendResponse {
    @JsonProperty("BackendId")
    private Long backendId;
    @JsonProperty("Host")
    private String host;
    @JsonProperty("HeartbeatPort")
    private Integer heartbeatPort;
    @JsonProperty("BePort")
    private Integer bePort;
    @JsonProperty("HttpPort")
    private Integer httpPort;
    @JsonProperty("BrpcPort")
    private Integer brpcPort;
    @JsonProperty("Alive")
    private Boolean alive;
    @JsonProperty("TabletNum")
    private Long tabletNum;
    @JsonProperty("DataUsedCapacity")
    private String dataUsedCapacity;
    @JsonProperty("AvailCapacity")
    private String availCapacity;
    @JsonProperty("TotalCapacity")
    private String totalCapacity;
    @JsonProperty("UsedPct")
    private String usedPct;
    @JsonProperty("CpuCores")
    private Integer cpuCores;
    @JsonProperty("MemUsedPct")
    private String memUsedPct;
    @JsonProperty("DiskUsedPct")
    private String diskUsedPct;
    @JsonProperty("NodeRole")
    private String nodeRole;
    @JsonProperty("ClusterName")
    private String clusterName;
    @JsonProperty("StartTime")
    private String startTime;
    @JsonProperty("Version")
    private String version;
}
