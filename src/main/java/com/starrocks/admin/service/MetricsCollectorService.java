package com.starrocks.admin.service;

import com.starrocks.admin.client.StarRocksHttpClient;
import com.starrocks.admin.model.entity.Cluster;
import com.starrocks.admin.model.entity.MetricsSnapshot;
import com.starrocks.admin.repository.MetricsSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsCollectorService {

    private final ClusterService clusterService;
    private final StarRocksHttpClient starRocksHttpClient;
    private final MetricsSnapshotRepository metricsSnapshotRepository;

    @Scheduled(fixedDelayString = "${app.metrics.interval-secs:30}000")
    public void collectMetrics() {
        try {
            List<Cluster> clusters = clusterService.listAllClusters();
            for (Cluster cluster : clusters) {
                if (!Boolean.TRUE.equals(cluster.getIsActive())) continue;
                try {
                    collectForCluster(cluster);
                } catch (Exception e) {
                    log.warn("Failed to collect metrics for cluster {}: {}", cluster.getName(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Metrics collection error: {}", e.getMessage());
        }
    }

    private void collectForCluster(Cluster cluster) {
        String metricsText = starRocksHttpClient.getPrometheusMetrics(cluster);
        Map<String, Double> metrics = starRocksHttpClient.parsePrometheusMetrics(metricsText);

        MetricsSnapshot snapshot = MetricsSnapshot.builder()
                .clusterId(cluster.getId())
                .capturedAt(OffsetDateTime.now())
                .qps(metrics.getOrDefault("starrocks_query_total", 0.0))
                .queryLatencyP99(metrics.getOrDefault("starrocks_query_latency_p99", 0.0))
                .queryLatencyP95(metrics.getOrDefault("starrocks_query_latency_p95", 0.0))
                .queryLatencyP50(metrics.getOrDefault("starrocks_query_latency_p50", 0.0))
                .runningQueries(metrics.getOrDefault("starrocks_query_running", 0.0).longValue())
                .totalQueries(metrics.getOrDefault("starrocks_query_total", 0.0).longValue())
                .errorQueries(metrics.getOrDefault("starrocks_query_error_total", 0.0).longValue())
                .beNodesTotal(metrics.getOrDefault("starrocks_be_alive", 0.0).longValue())
                .beNodesAlive(metrics.getOrDefault("starrocks_be_alive", 0.0).longValue())
                .feNodesTotal(metrics.getOrDefault("starrocks_fe_alive", 0.0).longValue())
                .feNodesAlive(metrics.getOrDefault("starrocks_fe_alive", 0.0).longValue())
                .cpuUsagePercent(metrics.getOrDefault("starrocks_cpu_usage_percent", 0.0))
                .memoryUsagePercent(metrics.getOrDefault("starrocks_mem_usage_percent", 0.0))
                .diskUsagePercent(metrics.getOrDefault("starrocks_disk_usage_percent", 0.0))
                .build();

        metricsSnapshotRepository.save(snapshot);
        log.debug("Collected metrics for cluster {}", cluster.getName());
    }

    public void cleanupOldSnapshots(int retentionDays) {
        OffsetDateTime cutoff = OffsetDateTime.now().minusDays(retentionDays);
        List<Cluster> clusters = clusterService.listAllClusters();
        for (Cluster cluster : clusters) {
            metricsSnapshotRepository.deleteOldSnapshots(cluster.getId(), cutoff);
        }
    }
}
