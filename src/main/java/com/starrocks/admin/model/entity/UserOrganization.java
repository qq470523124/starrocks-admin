package com.starrocks.admin.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_organizations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserOrganization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "created_at", nullable = false)
    private java.time.OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = java.time.OffsetDateTime.now();
    }
}
