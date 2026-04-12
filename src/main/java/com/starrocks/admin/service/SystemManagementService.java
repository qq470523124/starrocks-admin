package com.starrocks.admin.service;

import com.starrocks.admin.client.MySQLClient;
import com.starrocks.admin.client.MySQLPoolManager;
import com.starrocks.admin.client.StarRocksHttpClient;
import com.starrocks.admin.model.dto.response.SystemFunctionDetailResponse;
import com.starrocks.admin.model.entity.Cluster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemManagementService {

    private final MySQLPoolManager mysqlPoolManager;
    private final StarRocksHttpClient starRocksHttpClient;

    private static final Map<String, String> SYSTEM_FUNCTIONS = Map.ofEntries(
            Map.entry("frontends", "Frontend nodes"),
            Map.entry("backends", "Backend nodes"),
            Map.entry("current_queries", "Running queries"),
            Map.entry("stream_loads", "Stream loads"),
            Map.entry("routine_loads", "Routine loads"),
            Map.entry("transactions", "Transactions"),
            Map.entry("schema_changes", "Schema changes"),
            Map.entry("tasks", "Tasks"),
            Map.entry("load_error_hub", "Load error hub"),
            Map.entry("brokers", "Brokers"),
            Map.entry("compute_nodes", "Compute nodes"),
            Map.entry("replications", "Replications"),
            Map.entry("current_backend_instances", "Current backend instances"),
            Map.entry("historical_nodes", "Historical nodes"),
            Map.entry("compactions", "Compactions"),
            Map.entry("colocation_group", "Colocation groups"),
            Map.entry("catalog", "External catalogs"),
            Map.entry("cluster_balance", "Cluster balance"),
            Map.entry("meta_recovery", "Meta recovery"),
            Map.entry("global_current_queries", "Global current queries"),
            Map.entry("statistic", "Statistics"),
            Map.entry("jobs", "Jobs"),
            Map.entry("warehouses", "Warehouses"),
            Map.entry("resources", "Resources"),
            Map.entry("dbs", "Databases")
    );

    public List<Map<String, String>> listSystemFunctions() {
        List<Map<String, String>> functions = new ArrayList<>();
        for (Map.Entry<String, String> entry : SYSTEM_FUNCTIONS.entrySet()) {
            functions.add(Map.of(
                    "name", entry.getKey(),
                    "description", entry.getValue(),
                    "category", "system",
                    "status", "available",
                    "last_updated", OffsetDateTime.now().toString()
            ));
        }
        return functions;
    }

    public SystemFunctionDetailResponse getSystemFunctionDetail(Cluster cluster, String functionName,
                                                                int limit, int offset, String filter) {
        String sql = buildSystemFunctionSql(functionName, limit, offset, filter);

        try (MySQLClient client = mysqlPoolManager.createClient(cluster)) {
            MySQLClient.QueryResult result = client.queryRaw(sql);

            List<Map<String, String>> data = new ArrayList<>();
            for (List<String> row : result.rows()) {
                Map<String, String> rowMap = new LinkedHashMap<>();
                for (int i = 0; i < result.columns().size() && i < row.size(); i++) {
                    rowMap.put(result.columns().get(i), row.get(i));
                }
                data.add(rowMap);
            }

            return SystemFunctionDetailResponse.builder()
                    .functionName(functionName)
                    .description(SYSTEM_FUNCTIONS.getOrDefault(functionName, "System function"))
                    .data(data)
                    .totalCount(data.size())
                    .lastUpdated(OffsetDateTime.now())
                    .build();
        }
    }

    private String buildSystemFunctionSql(String functionName, int limit, int offset, String filter) {
        String sql = switch (functionName) {
            case "frontends" -> "SHOW FRONTENDS";
            case "backends" -> "SHOW BACKENDS";
            case "current_queries" -> "SHOW PROCESSLIST";
            case "stream_loads" -> "SHOW LOAD ORDER BY CreateTime DESC";
            case "routine_loads" -> "SHOW ROUTINE LOAD";
            case "transactions" -> "SHOW TRANSACTION ORDER BY CreateTime DESC";
            case "schema_changes" -> "SHOW ALTER TABLE COLUMN ORDER BY CreateTime DESC";
            case "tasks" -> "SHOW TASKS ORDER BY CreateTime DESC";
            case "brokers" -> "SHOW BROKER";
            case "compute_nodes" -> "SHOW COMPUTE NODES";
            case "replications" -> "SHOW REPLICATION STATUS";
            case "current_backend_instances" -> "SHOW BACKENDS";
            case "historical_nodes" -> "SHOW BACKENDS";
            case "compactions" -> "SHOW COMPACTION";
            case "colocation_group" -> "SHOW COLOCATION GROUP";
            case "catalog" -> "SHOW CATALOGS";
            case "cluster_balance" -> "SHOW PROC '/cluster_balance'";
            case "load_error_hub" -> "SHOW LOAD_ERROR_HUB";
            case "meta_recovery" -> "SHOW PROC '/meta_recovery'";
            case "global_current_queries" -> "SHOW PROCESSLIST";
            case "statistic" -> "SHOW STATS";
            case "jobs" -> "SHOW JOBS";
            case "warehouses" -> "SHOW WAREHOUSES";
            case "resources" -> "SHOW RESOURCES";
            case "dbs" -> "SHOW DATABASES";
            default -> "SHOW PROC '/" + functionName + "'";
        };

        if (limit > 0) {
            sql += " LIMIT " + limit + " OFFSET " + offset;
        }
        return sql;
    }
}
