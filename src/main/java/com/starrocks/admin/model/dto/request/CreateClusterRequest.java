package com.starrocks.admin.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateClusterRequest {

    @NotBlank @Size(max = 100)
    private String name;

    private String description;

    @NotBlank
    private String feHost;

    private Integer feHttpPort = 8030;

    private Integer feQueryPort = 9030;

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    private Boolean enableSsl = false;

    private Integer connectionTimeout = 10;

    private String catalog = "default_catalog";

    private String tags;

    private String deploymentMode = "shared_nothing";
}
