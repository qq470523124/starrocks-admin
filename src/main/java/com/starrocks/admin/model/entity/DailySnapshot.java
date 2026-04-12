package com.starrocks.admin.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "daily_snapshots")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DailySnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cluster_id", nullable = false)
    private Long clusterId;

    @Column(name = "snapshot_date", nullable = false)
    private String snapshotDate;

    private Double avgQps;
    private Double maxQps;
    private Double avgLatencyP99;
    private Double maxLatencyP99;
    private Long totalQueries;
    private Long errorQueries;
    private Double avgCpuUsage;
    private Double maxCpuUsage;
    private Double avgMemoryUsage;
    private Double maxMemoryUsage;
    private Long avgDiskUsedBytes;
    private Long maxDiskUsedBytes;
    private Long avgDataUsedBytes;
    private Long maxDataUsedBytes;
    private Long avgCompactionScore;
    private Long maxCompactionScore;
    private Long totalStreamLoads;
    private Long totalRoutineLoads;
    private Long totalTransactions;
    private Long totalSchemaChanges;
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
