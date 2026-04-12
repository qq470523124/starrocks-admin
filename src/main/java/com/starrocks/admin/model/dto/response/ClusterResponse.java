package com.starrocks.admin.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClusterResponse {
    private Long id;
    private String name;
    private String description;
    private String feHost;
    private Integer feHttpPort;
    private Integer feQueryPort;
    private String username;
    private Boolean enableSsl;
    private Integer connectionTimeout;
    private java.util.List<String> tags;
    private String catalog;
    private Boolean isActive;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Long organizationId;
    private String deploymentMode;

    public static ClusterResponse from(com.starrocks.admin.model.entity.Cluster cluster) {
        java.util.List<String> tags = null;
        if (cluster.getTags() != null && !cluster.getTags().isBlank()) {
            tags = java.util.Arrays.asList(cluster.getTags().split(","));
        }

        return ClusterResponse.builder()
                .id(cluster.getId())
                .name(cluster.getName())
                .description(cluster.getDescription())
                .feHost(cluster.getFeHost())
                .feHttpPort(cluster.getFeHttpPort())
                .feQueryPort(cluster.getFeQueryPort())
                .username(cluster.getUsername())
                .enableSsl(cluster.getEnableSsl())
                .connectionTimeout(cluster.getConnectionTimeout())
                .tags(tags)
                .catalog(cluster.getCatalog())
                .isActive(cluster.getIsActive())
                .createdAt(cluster.getCreatedAt())
                .updatedAt(cluster.getUpdatedAt())
                .organizationId(cluster.getOrganizationId())
                .deploymentMode(cluster.getDeploymentMode())
                .build();
    }
}
