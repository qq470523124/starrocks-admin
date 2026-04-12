package com.starrocks.admin.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "clusters")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Cluster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "fe_host", nullable = false, length = 255)
    private String feHost;

    @Column(name = "fe_http_port", nullable = false)
    private Integer feHttpPort;

    @Column(name = "fe_query_port", nullable = false)
    private Integer feQueryPort;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(name = "password_encrypted", nullable = false, length = 500)
    private String passwordEncrypted;

    @Column(name = "enable_ssl", nullable = false)
    private Boolean enableSsl = false;

    @Column(name = "connection_timeout")
    private Integer connectionTimeout = 10;

    @Column(length = 100)
    private String catalog = "default_catalog";

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;

    @Column(length = 500)
    private String tags;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "deployment_mode", nullable = false, length = 50)
    private String deploymentMode = "shared_nothing";

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public boolean isSharedData() {
        return "shared_data".equals(deploymentMode);
    }

    public boolean isSharedNothing() {
        return "shared_nothing".equals(deploymentMode);
    }
}
