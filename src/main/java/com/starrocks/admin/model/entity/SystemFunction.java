package com.starrocks.admin.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "system_functions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SystemFunction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cluster_id", nullable = false)
    private Long clusterId;

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @Column(name = "function_name", nullable = false, length = 100)
    private String functionName;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "sql_query", nullable = false, columnDefinition = "TEXT")
    private String sqlQuery;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "category_order", nullable = false)
    private Integer categoryOrder;

    @Column(name = "is_favorited", nullable = false)
    private Boolean isFavorited = false;

    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = false;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

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
}
