package com.starrocks.admin.service;

import com.starrocks.admin.client.MySQLClient;
import com.starrocks.admin.client.MySQLPoolManager;
import com.starrocks.admin.client.StarRocksHttpClient;
import com.starrocks.admin.model.dto.response.QueryHistoryResponse;
import com.starrocks.admin.model.entity.Cluster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueryHistoryService {

    private final MySQLPoolManager mysqlPoolManager;
    private final StarRocksHttpClient starRocksHttpClient;

    public QueryHistoryResponse listQueryHistory(Cluster cluster, int limit, int offset,
                                                  String keyword, String startTime, String endTime) {
        StringBuilder sql = new StringBuilder("SELECT query_id, user, default_db, stmt, query_type, start_time, total_ms, state, warehouse FROM starrocks_audit_tbl__ WHERE 1=1");

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (query_id LIKE '%").append(keyword.replace("'", ""))
               .append("%' OR stmt LIKE '%").append(keyword.replace("'", ""))
               .append("%' OR user LIKE '%").append(keyword.replace("'", "")).append("%')");
        }
        if (startTime != null && !startTime.isBlank()) {
            sql.append(" AND start_time >= '").append(startTime).append("'");
        }
        if (endTime != null && !endTime.isBlank()) {
            sql.append(" AND start_time <= '").append(endTime).append("'");
        }

        // Count total
        String countSql = sql.toString().replace(
                "SELECT query_id, user, default_db, stmt, query_type, start_time, total_ms, state, warehouse",
                "SELECT COUNT(*)");

        try (MySQLClient client = mysqlPoolManager.createClient(cluster)) {
            MySQLClient.QueryResult countResult = client.queryRaw(countSql);
            long total = 0;
            if (!countResult.rows().isEmpty() && !countResult.rows().get(0).isEmpty()) {
                try { total = Long.parseLong(countResult.rows().get(0).get(0)); } catch (NumberFormatException ignored) {}
            }

            sql.append(" ORDER BY start_time DESC LIMIT ").append(limit).append(" OFFSET ").append(offset);
            MySQLClient.QueryResult result = client.queryRaw(sql.toString());

            List<QueryHistoryResponse.QueryHistoryItem> items = new ArrayList<>();
            for (List<String> row : result.rows()) {
                long totalMs = 0;
                try { totalMs = Long.parseLong(row.get(6)); } catch (NumberFormatException ignored) {}

                items.add(new QueryHistoryResponse.QueryHistoryItem(
                        getCol(row, 0), getCol(row, 1), getCol(row, 2),
                        getCol(row, 3), getCol(row, 4), getCol(row, 5),
                        "", totalMs, getCol(row, 7), getCol(row, 8)
                ));
            }

            long page = (offset / limit) + 1;
            return new QueryHistoryResponse(items, total, page, limit);
        }
    }

    private String getCol(List<String> row, int index) {
        return index < row.size() ? row.get(index) : "";
    }

    /**
     * List current running queries.
     * Tries HTTP API (SHOW PROC '/current_queries') first for richer fields,
     * falls back to SHOW PROCESSLIST via MySQL protocol.
     */
    public List<Map<String, String>> listCurrentQueries(Cluster cluster) {
        // Try HTTP API first for richer fields (ScanBytes, ProcessRows, CPUTime, ExecTime)
        List<Map<String, Object>> httpResult = starRocksHttpClient.getCurrentQueries(cluster);
        if (httpResult != null) {
            return httpResult.stream()
                    .map(row -> {
                        Map<String, String> mapped = new HashMap<>();
                        mapped.put("QueryId", strVal(row.get("QueryId")));
                        mapped.put("ConnectionId", strVal(row.get("ConnectionId")));
                        mapped.put("Database", strVal(row.get("Database")));
                        mapped.put("User", strVal(row.get("User")));
                        mapped.put("ScanBytes", strVal(row.get("ScanBytes")));
                        mapped.put("ProcessRows", strVal(row.getOrDefault("ProcessRows", row.get("ScanRows"))));
                        mapped.put("CPUTime", strVal(row.get("CPUTime")));
                        mapped.put("ExecTime", strVal(row.get("ExecTime")));
                        mapped.put("Sql", strVal(row.get("Sql")));
                        return mapped;
                    })
                    .collect(Collectors.toList());
        }

        // Fallback to SHOW PROCESSLIST
        try (MySQLClient client = mysqlPoolManager.createClient(cluster)) {
            MySQLClient.QueryResult result = client.queryRaw("SHOW PROCESSLIST");
            return result.rows().stream()
                    .filter(row -> row.size() >= 8)
                    .map(row -> {
                        Map<String, String> mapped = new HashMap<>();
                        mapped.put("QueryId", getCol(row, 0));
                        mapped.put("ConnectionId", getCol(row, 0));
                        mapped.put("User", getCol(row, 1));
                        mapped.put("Database", getCol(row, 3));
                        mapped.put("ScanBytes", "");
                        mapped.put("ProcessRows", "");
                        mapped.put("CPUTime", "");
                        mapped.put("ExecTime", getCol(row, 5));
                        mapped.put("Sql", getCol(row, 7));
                        return mapped;
                    })
                    .collect(Collectors.toList());
        }
    }

    private String strVal(Object val) {
        return val != null ? val.toString() : "";
    }

    public Map<String, Object> killQuery(Cluster cluster, String queryId) {
        try (MySQLClient client = mysqlPoolManager.createClient(cluster)) {
            client.execute("KILL QUERY '" + queryId.replace("'", "") + "'");
            return Map.of("success", true, "query_id", queryId);
        }
    }

    public Map<String, Object> getQueryProfile(Cluster cluster, String queryId) {
        try (MySQLClient client = mysqlPoolManager.createClient(cluster)) {
            MySQLClient.QueryResult result = client.queryRaw(
                    "SHOW QUERY PROFILE '" + queryId.replace("'", "") + "'");
            return Map.of("query_id", queryId, "profile", result.rows());
        }
    }
}
