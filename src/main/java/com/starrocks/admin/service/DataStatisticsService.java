package com.starrocks.admin.service;

import com.starrocks.admin.client.MySQLClient;
import com.starrocks.admin.client.MySQLPoolManager;
import com.starrocks.admin.model.entity.Cluster;
import com.starrocks.admin.model.entity.DataStatistics;
import com.starrocks.admin.repository.DataStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataStatisticsService {

    private final MySQLPoolManager mysqlPoolManager;
    private final DataStatisticsRepository dataStatisticsRepository;

    public void collectStatistics(Cluster cluster) {
        try (MySQLClient client = mysqlPoolManager.createClient(cluster)) {
            // Get all databases
            MySQLClient.QueryResult dbResult = client.queryRaw("SHOW DATABASES");
            for (List<String> dbRow : dbResult.rows()) {
                String dbName = dbRow.get(0);
                if ("information_schema".equals(dbName) || "_statistics_".equals(dbName)) continue;

                try {
                    client.useDatabase(dbName);
                    MySQLClient.QueryResult tableResult = client.queryRaw("SHOW TABLES");
                    for (List<String> tableRow : tableResult.rows()) {
                        String tableName = tableRow.get(0);
                        collectTableStats(cluster, client, dbName, tableName);
                    }
                } catch (Exception e) {
                    log.debug("Failed to collect stats for database {}: {}", dbName, e.getMessage());
                }
            }
        }
    }

    private void collectTableStats(Cluster cluster, MySQLClient client, String dbName, String tableName) {
        try {
            MySQLClient.QueryResult sizeResult = client.queryRaw(
                    "SHOW DATA FROM " + tableName);
            if (!sizeResult.rows().isEmpty()) {
                List<String> row = sizeResult.rows().get(0);
                long sizeBytes = parseBytes(row.size() > 2 ? row.get(2) : "0");
                long rowCount = parseLong(row.size() > 1 ? row.get(1) : "0");

                DataStatistics stats = DataStatistics.builder()
                        .clusterId(cluster.getId())
                        .databaseName(dbName)
                        .tableName(tableName)
                        .tableSizeBytes(sizeBytes)
                        .rowCount(rowCount)
                        .updatedAt(OffsetDateTime.now())
                        .build();

                dataStatisticsRepository.save(stats);
            }
        } catch (Exception e) {
            log.debug("Failed to collect stats for {}.{}: {}", dbName, tableName, e.getMessage());
        }
    }

    public List<DataStatistics> getTopTablesBySize(Long clusterId) {
        return dataStatisticsRepository.findTopTablesBySize(clusterId);
    }

    public List<DataStatistics> getTopTablesByAccess(Long clusterId) {
        return dataStatisticsRepository.findTopTablesByAccess(clusterId);
    }

    private long parseBytes(String value) {
        if (value == null || value.isBlank()) return 0;
        value = value.trim().toUpperCase();
        try {
            if (value.endsWith("TB")) return (long)(Double.parseDouble(value.replace("TB", "")) * 1024L * 1024 * 1024 * 1024);
            if (value.endsWith("GB")) return (long)(Double.parseDouble(value.replace("GB", "")) * 1024L * 1024 * 1024);
            if (value.endsWith("MB")) return (long)(Double.parseDouble(value.replace("MB", "")) * 1024L * 1024);
            if (value.endsWith("KB")) return (long)(Double.parseDouble(value.replace("KB", "")) * 1024L);
            if (value.endsWith("B")) return (long)(Double.parseDouble(value.replace("B", "")));
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private long parseLong(String value) {
        try { return Long.parseLong(value.replaceAll("[^0-9-]", "")); }
        catch (NumberFormatException e) { return 0; }
    }
}
