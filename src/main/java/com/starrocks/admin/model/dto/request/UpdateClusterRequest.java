package com.starrocks.admin.model.dto.request;

import lombok.Data;

@Data
public class UpdateClusterRequest {
    private String name;
    private String description;
    private String feHost;
    private Integer feHttpPort;
    private Integer feQueryPort;
    private String username;
    private String password;
    private Boolean enableSsl;
    private Integer connectionTimeout;
    private String catalog;
    private String tags;
    private String deploymentMode;
}
