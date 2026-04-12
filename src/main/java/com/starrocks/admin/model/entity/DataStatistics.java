package com.starrocks.admin.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "data_statistics")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DataStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cluster_id", nullable = false)
    private Long clusterId;

    @Column(name = "database_name", nullable = false, length = 100)
    private String databaseName;

    @Column(name = "table_name", nullable = false, length = 200)
    private String tableName;

    @Column(name = "table_size_bytes")
    private Long tableSizeBytes;

    @Column(name = "row_count")
    private Long rowCount;

    @Column(name = "tablet_count")
    private Long tabletCount;

    @Column(name = "replica_count")
    private Long replicaCount;

    @Column(name = "last_access_time")
    private OffsetDateTime lastAccessTime;

    @Column(name = "access_count")
    private Long accessCount = 0L;

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
