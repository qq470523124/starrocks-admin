package com.starrocks.admin.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "system_function_preferences")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SystemFunctionPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cluster_id", nullable = false)
    private Long clusterId;

    @Column(name = "function_id", nullable = false)
    private Long functionId;

    @Column(name = "category_order", nullable = false)
    private Integer categoryOrder;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "is_favorited", nullable = false)
    private Boolean isFavorited = false;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (updatedAt == null) updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
