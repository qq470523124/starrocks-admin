package com.starrocks.admin.service;

import com.starrocks.admin.client.MySQLClient;
import com.starrocks.admin.client.MySQLPoolManager;
import com.starrocks.admin.model.dto.response.QueryHistoryResponse;
import com.starrocks.admin.model.entity.Cluster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueryHistoryService {

    private final MySQLPoolManager mysqlPoolManager;

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
}
