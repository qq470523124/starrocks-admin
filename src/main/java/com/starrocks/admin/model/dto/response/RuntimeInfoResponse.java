package com.starrocks.admin.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RuntimeInfoResponse {
    private Map<String, Object> beNodes;
    private Map<String, Object> feNodes;
    private Map<String, Object> computeNodes;
    private Map<String, Object> systemInfo;
    private Map<String, Object> versionInfo;
}
