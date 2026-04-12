package com.starrocks.admin.model.dto.request;

import lombok.Data;

@Data
public class HealthCheckRequest {
    private String feHost;
    private Integer feHttpPort;
    private Integer feQueryPort;
    private String username;
    private String password;
    private Boolean enableSsl;
    private String catalog;
}
