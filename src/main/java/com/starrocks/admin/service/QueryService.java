package com.starrocks.admin.service;

import com.starrocks.admin.client.MySQLClient;
import com.starrocks.admin.client.MySQLPoolManager;
import com.starrocks.admin.exception.ApiException;
import com.starrocks.admin.model.dto.request.QueryExecuteRequest;
import com.starrocks.admin.model.dto.response.*;
import com.starrocks.admin.model.entity.Cluster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueryService {

    private final MySQLPoolManager mysqlPoolManager;
    private final ClusterService clusterService;

    public List<String> listCatalogs(Cluster cluster) {
        try (MySQLClient client = mysqlPoolManager.createClient(cluster)) {
            MySQLClient.QueryResult result = client.queryRaw("SHOW CATALOGS");
            return result.rows().stream()
                    .map(row -> row.get(0))
                    .filter(Objects::nonNull)
                    .toList();
        }
    }

    public CatalogsWithDatabasesResponse getCatalogsWithDatabases(Cluster cluster) {
        try (MySQLClient client = mysqlPoolManager.createClient(cluster)) {
            List<String> catalogs = listCatalogs(cluster);
            List<CatalogsWithDatabasesResponse.CatalogWithDatabases> catalogList = new ArrayList<>();

            for (String catalog : catalogs) {
                try {
                    MySQLClient.QueryResult dbResult = client.queryRaw("SHOW DATABASES FROM " + catalog);
                    List<String> databases = dbResult.rows().stream()
                            .map(row -> row.get(0))
                            .filter(Objects::nonNull)
                            .toList();
                    catalogList.add(new CatalogsWithDatabasesResponse.CatalogWithDatabases(catalog, databases));
                } catch (Exception e) {
                    log.warn("Failed to get databases for catalog {}: {}", catalog, e.getMessage());
                    catalogList.add(new CatalogsWithDatabasesResponse.CatalogWithDatabases(catalog, List.of()));
                }
            }

            return CatalogsWithDatabasesResponse.builder().catalogs(catalogList).build();
        }
    }

    public List<String> listDatabases(Cluster cluster, String catalog) {
        try (MySQLClient client = mysqlPoolManager.createClient(cluster)) {
            String sql = (catalog != null && !catalog.isBlank())
                    ? "SHOW DATABASES FROM " + catalog
                    : "SHOW DATABASES";
            MySQLClient.QueryResult result = client.queryRaw(sql);
            return result.rows().stream()
                    .map(row -> row.get(0))
                    .filter(Objects::nonNull)
                    .toList();
        }
    }

    public List<Map<String, String>> listTables(Cluster cluster, String database) {
        try (MySQLClient client = mysqlPoolManager.createClient(cluster)) {
            client.useDatabase(database);
            MySQLClient.QueryResult result = client.queryRaw("SHOW TABLES");
            return result.rows().stream()
                    .map(row -> Map.of("name", row.get(0), "object_type", "TABLE"))
                    .toList();
        }
    }

    public QueryExecuteResponse executeQuery(Cluster cluster, QueryExecuteRequest req) {
        long startTime = System.currentTimeMillis();
        List<QueryResultResponse> results = new ArrayList<>();

        try (MySQLClient client = mysqlPoolManager.createClient(cluster)) {
            if (req.getDatabase() != null && !req.getDatabase().isBlank()) {
                client.useDatabase(req.getDatabase());
            }

            List<String> statements = splitStatements(req.getSql());
            for (String sql : statements) {
                String trimmedSql = sql.trim();
                if (trimmedSql.isEmpty()) continue;

                String finalSql = trimmedSql;
                if (Boolean.TRUE.equals(req.getAutoLimit()) && req.getLimit() != null) {
                    finalSql = applyQueryLimit(trimmedSql, req.getLimit());
                }

                long stmtStart = System.currentTimeMillis();
                MySQLClient.QueryResult qr = client.queryRaw(finalSql);
                long stmtTime = System.currentTimeMillis() - stmtStart;

                QueryResultResponse resp = QueryResultResponse.builder()
                        .sql(trimmedSql)
                        .columns(qr.columns())
                        .rows(qr.rows())
                        .rowCount(qr.rows() != null ? (long) qr.rows().size() : 0L)
                        .affectedRows(qr.affectedRows())
                        .executionTimeMs(stmtTime)
                        .success(true)
                        .build();
                results.add(resp);
            }
        }

        long totalTime = System.currentTimeMillis() - startTime;
        return QueryExecuteResponse.builder()
                .results(results)
                .totalExecutionTimeMs(totalTime)
                .build();
    }

    public String getTableDDL(Cluster cluster, String database, String table) {
        try (MySQLClient client = mysqlPoolManager.createClient(cluster)) {
            client.useDatabase(database);
            return client.queryScalar("SHOW CREATE TABLE " + table).orElse("");
        }
    }

    private List<String> splitStatements(String sql) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;

        for (char ch : sql.toCharArray()) {
            if (ch == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
                current.append(ch);
            } else if (ch == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
                current.append(ch);
            } else if (ch == '-' && !inSingleQuote && !inDoubleQuote) {
                current.append(ch);
            } else if (ch == ';' && !inSingleQuote && !inDoubleQuote) {
                String trimmed = current.toString().trim();
                if (!trimmed.isEmpty()) {
                    statements.add(trimmed);
                }
                current = new StringBuilder();
            } else {
                current.append(ch);
            }
        }

        String trimmed = current.toString().trim();
        if (!trimmed.isEmpty()) {
            statements.add(trimmed);
        }

        return statements;
    }

    private String applyQueryLimit(String sql, int limit) {
        String trimmed = sql.trim();
        String upper = trimmed.toUpperCase();

        if (upper.contains("LIMIT")) {
            return trimmed;
        }

        if (upper.startsWith("SELECT")) {
            if (upper.contains("GET_QUERY_PROFILE")
                    || upper.contains("SHOW_PROFILE")
                    || upper.contains("EXPLAIN")) {
                return trimmed;
            }
            String withoutSemicolon = trimmed.endsWith(";")
                    ? trimmed.substring(0, trimmed.length() - 1).trim()
                    : trimmed;
            return withoutSemicolon + " LIMIT " + limit;
        }

        return trimmed;
    }
}
