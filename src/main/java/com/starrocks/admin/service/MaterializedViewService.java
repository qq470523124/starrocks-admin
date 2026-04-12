package com.starrocks.admin.service;

import com.starrocks.admin.client.MySQLClient;
import com.starrocks.admin.client.MySQLPoolManager;
import com.starrocks.admin.exception.ApiException;
import com.starrocks.admin.model.dto.request.*;
import com.starrocks.admin.model.dto.response.MaterializedViewResponse;
import com.starrocks.admin.model.entity.Cluster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaterializedViewService {

    private final MySQLPoolManager mysqlPoolManager;

    public List<MaterializedViewResponse> listMaterializedViews(Cluster cluster, String database) {
        String sql = "SHOW MATERIALIZED VIEWS";
        if (database != null && !database.isBlank()) {
            sql += " FROM " + database;
        }

        try (MySQLClient client = mysqlPoolManager.createClient(cluster)) {
            MySQLClient.QueryResult result = client.queryRaw(sql);
            List<MaterializedViewResponse> mvs = new ArrayList<>();

            for (List<String> row : result.rows()) {
                MaterializedViewResponse mv = MaterializedViewResponse.builder()
                        .id(getCol(row, 0))
                        .name(getCol(row, 1))
                        .databaseName(getCol(row, 2))
                        .refreshType(getCol(row, 3))
                        .isActive("TRUE".equalsIgnoreCase(getCol(row, 4)))
                        .partitionType(getCol(row, 5))
                        .taskId(getCol(row, 6))
                        .taskName(getCol(row, 7))
                        .lastRefreshStartTime(getCol(row, 8))
                        .lastRefreshFinishedTime(getCol(row, 9))
                        .lastRefreshDuration(getCol(row, 10))
                        .lastRefreshState(getCol(row, 11))
                        .lastError(getCol(row, 12))
                        .rows(getCol(row, 13))
                        .text(getCol(row, 14))
                        .refreshInterval(getCol(row, 15))
                        .build();
                mvs.add(mv);
            }

            return mvs;
        }
    }

    public void createMaterializedView(Cluster cluster, String sql) {
        try (MySQLClient client = mysqlPoolManager.createClient(cluster)) {
            client.execute(sql);
            log.info("Materialized view created successfully");
        }
    }

    public void refreshMaterializedView(Cluster cluster, String mvName, RefreshMaterializedViewRequest req) {
        StringBuilder sql = new StringBuilder("REFRESH MATERIALIZED VIEW ");
        sql.append(mvName);

        if (req.getPartitionStart() != null && !req.getPartitionStart().isBlank()) {
            sql.append(" START('").append(req.getPartitionStart()).append("')");
        }
        if (req.getPartitionEnd() != null && !req.getPartitionEnd().isBlank()) {
            sql.append(" END('").append(req.getPartitionEnd()).append("')");
        }
        if (req.isForce()) {
            sql.append(" FORCE");
        }
        sql.append(" ").append(req.getMode());

        try (MySQLClient client = mysqlPoolManager.createClient(cluster)) {
            client.execute(sql.toString());
            log.info("Materialized view refresh triggered: {}", mvName);
        }
    }

    public void cancelRefresh(Cluster cluster, String mvName, boolean force) {
        String sql = "CANCEL REFRESH MATERIALIZED VIEW " + mvName;
        if (force) {
            sql += " FORCE";
        }

        try (MySQLClient client = mysqlPoolManager.createClient(cluster)) {
            client.execute(sql);
            log.info("Materialized view refresh cancelled: {}", mvName);
        }
    }

    public Map<String, String> getMaterializedViewDDL(Cluster cluster, String mvName) {
        String sql = "SHOW CREATE MATERIALIZED VIEW " + mvName;

        try (MySQLClient client = mysqlPoolManager.createClient(cluster)) {
            MySQLClient.QueryResult result = client.queryRaw(sql);
            if (!result.rows().isEmpty() && !result.rows().get(0).isEmpty()) {
                return Map.of("mvName", mvName, "ddl", result.rows().get(0).get(0));
            }
            throw ApiException.resourceNotFound("Materialized view not found: " + mvName);
        }
    }

    public void alterMaterializedView(Cluster cluster, String mvName, String alterClause) {
        String sql = "ALTER MATERIALIZED VIEW " + mvName + " " + alterClause;

        try (MySQLClient client = mysqlPoolManager.createClient(cluster)) {
            client.execute(sql);
            log.info("Materialized view altered: {}", mvName);
        }
    }

    public void dropMaterializedView(Cluster cluster, String mvName, boolean ifExists) {
        String sql = "DROP MATERIALIZED VIEW ";
        if (ifExists) sql += "IF EXISTS ";
        sql += mvName;

        try (MySQLClient client = mysqlPoolManager.createClient(cluster)) {
            client.execute(sql);
            log.info("Materialized view dropped: {}", mvName);
        }
    }

    private String getCol(List<String> row, int index) {
        return index < row.size() ? row.get(index) : null;
    }
}
