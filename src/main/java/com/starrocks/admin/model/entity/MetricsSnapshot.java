package com.starrocks.admin.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "metrics_snapshots")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MetricsSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cluster_id", nullable = false)
    private Long clusterId;

    @Column(name = "captured_at", nullable = false)
    private OffsetDateTime capturedAt;

    private Double qps;
    private Double queryLatencyP99;
    private Double queryLatencyP95;
    private Double queryLatencyP50;
    private Long runningQueries;
    private Long totalQueries;
    private Long errorQueries;
    private Long beNodesTotal;
    private Long beNodesAlive;
    private Long feNodesTotal;
    private Long feNodesAlive;
    private Double cpuUsagePercent;
    private Double memoryUsagePercent;
    private Double diskUsagePercent;
    private Long diskTotalBytes;
    private Long diskUsedBytes;
    private Long dataTotalBytes;
    private Long dataUsedBytes;
    private Long tabletCount;
    private Long replicaCount;
    private Long compactionScore;
    private Long pendingCompactionTasks;
    private Long streamLoadCount;
    private Long routineLoadCount;
    private Long transactionCount;
    private Long schemaChangeCount;
    private Long materializedViewCount;
    private Long sessionCount;
    private Long connectionCount;
    private Double networkReceiveBytesPerSec;
    private Double networkSendBytesPerSec;
}
