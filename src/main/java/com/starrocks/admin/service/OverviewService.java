package com.starrocks.admin.service;

import com.starrocks.admin.client.StarRocksHttpClient;
import com.starrocks.admin.client.MySQLPoolManager;
import com.starrocks.admin.model.dto.response.ClusterHealthResponse;
import com.starrocks.admin.model.entity.Cluster;
import com.starrocks.admin.model.entity.MetricsSnapshot;
import com.starrocks.admin.model.enums.HealthStatus;
import com.starrocks.admin.model.enums.TimeRange;
import com.starrocks.admin.repository.MetricsSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OverviewService {

    private final ClusterService clusterService;
    private final StarRocksHttpClient starRocksHttpClient;
    private final MySQLPoolManager mysqlPoolManager;
    private final MetricsSnapshotRepository metricsSnapshotRepository;

    public Map<String, Object> getClusterOverview(Cluster cluster, TimeRange timeRange) {
        Map<String, Object> overview = new LinkedHashMap<>();

        // Get latest snapshot
        List<MetricsSnapshot> recent = metricsSnapshotRepository
                .findRecentByCluster(cluster.getId(),
                        OffsetDateTime.now().minus(timeRange.toDuration()));

        MetricsSnapshot latest = recent.isEmpty() ? null : recent.get(recent.size() - 1);

        // Health card
        ClusterHealthResponse health = clusterService.getClusterHealth(cluster.getId());
        overview.put("health", health);

        // KPIs
        Map<String, Object> kpis = new LinkedHashMap<>();
        if (latest != null) {
            kpis.put("qps", latest.getQps());
            kpis.put("queryLatencyP99", latest.getQueryLatencyP99());
            kpis.put("runningQueries", latest.getRunningQueries());
            kpis.put("totalQueries", latest.getTotalQueries());
            kpis.put("errorQueries", latest.getErrorQueries());
        }
        overview.put("kpis", kpis);

        // Resource metrics
        Map<String, Object> resources = new LinkedHashMap<>();
        if (latest != null) {
            resources.put("cpuUsagePercent", latest.getCpuUsagePercent());
            resources.put("memoryUsagePercent", latest.getMemoryUsagePercent());
            resources.put("diskUsagePercent", latest.getDiskUsagePercent());
            resources.put("diskUsedBytes", latest.getDiskUsedBytes());
            resources.put("dataUsedBytes", latest.getDataUsedBytes());
        }
        overview.put("resources", resources);

        // Node info
        Map<String, Object> nodes = new LinkedHashMap<>();
        if (latest != null) {
            nodes.put("feNodesTotal", latest.getFeNodesTotal());
            nodes.put("feNodesAlive", latest.getFeNodesAlive());
            nodes.put("beNodesTotal", latest.getBeNodesTotal());
            nodes.put("beNodesAlive", latest.getBeNodesAlive());
        }
        overview.put("nodes", nodes);

        // Trends
        List<Map<String, Object>> trends = new ArrayList<>();
        for (MetricsSnapshot s : recent) {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("timestamp", s.getCapturedAt().toString());
            point.put("qps", s.getQps());
            point.put("queryLatencyP99", s.getQueryLatencyP99());
            point.put("cpuUsagePercent", s.getCpuUsagePercent());
            point.put("memoryUsagePercent", s.getMemoryUsagePercent());
            trends.add(point);
        }
        overview.put("trends", trends);

        return overview;
    }

    public Map<String, Object> getPerformanceTrends(Cluster cluster, TimeRange timeRange) {
        List<MetricsSnapshot> snapshots = metricsSnapshotRepository
                .findRecentByCluster(cluster.getId(),
                        OffsetDateTime.now().minus(timeRange.toDuration()));

        List<Map<String, Object>> trends = new ArrayList<>();
        for (MetricsSnapshot s : snapshots) {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("timestamp", s.getCapturedAt().toString());
            point.put("qps", s.getQps());
            point.put("queryLatencyP99", s.getQueryLatencyP99());
            point.put("queryLatencyP95", s.getQueryLatencyP95());
            point.put("queryLatencyP50", s.getQueryLatencyP50());
            point.put("runningQueries", s.getRunningQueries());
            point.put("errorQueries", s.getErrorQueries());
            trends.add(point);
        }
        return Map.of("trends", trends);
    }

    public Map<String, Object> getResourceTrends(Cluster cluster, TimeRange timeRange) {
        List<MetricsSnapshot> snapshots = metricsSnapshotRepository
                .findRecentByCluster(cluster.getId(),
                        OffsetDateTime.now().minus(timeRange.toDuration()));

        List<Map<String, Object>> trends = new ArrayList<>();
        for (MetricsSnapshot s : snapshots) {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("timestamp", s.getCapturedAt().toString());
            point.put("cpuUsagePercent", s.getCpuUsagePercent());
            point.put("memoryUsagePercent", s.getMemoryUsagePercent());
            point.put("diskUsagePercent", s.getDiskUsagePercent());
            point.put("diskUsedBytes", s.getDiskUsedBytes());
            point.put("dataUsedBytes", s.getDataUsedBytes());
            trends.add(point);
        }
        return Map.of("trends", trends);
    }

    public Map<String, Object> getCompactionDetailStats(Cluster cluster, String timeRange) {
        // Simplified compaction stats from metrics
        List<MetricsSnapshot> snapshots = metricsSnapshotRepository
                .findRecentByCluster(cluster.getId(),
                        OffsetDateTime.now().minusHours(24));

        Map<String, Object> stats = new LinkedHashMap<>();
        if (!snapshots.isEmpty()) {
            MetricsSnapshot latest = snapshots.get(snapshots.size() - 1);
            stats.put("compactionScore", latest.getCompactionScore());
            stats.put("pendingCompactionTasks", latest.getPendingCompactionTasks());
        }
        return stats;
    }
}
