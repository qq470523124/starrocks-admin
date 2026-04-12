package com.starrocks.admin.client;

import com.starrocks.admin.exception.ApiException;
import com.starrocks.admin.model.entity.Cluster;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.*;

@Slf4j
@Component
public class MySQLClient {

    private final Connection connection;

    public MySQLClient(Connection connection) {
        this.connection = connection;
    }

    /**
     * Execute a query and return results as (column_names, rows)
     */
    public QueryResult queryRaw(String sql) {
        try (Statement stmt = connection.createStatement()) {
            boolean hasResultSet = stmt.execute(sql);

            if (hasResultSet) {
                ResultSet rs = stmt.getResultSet();
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();

                List<String> columns = new ArrayList<>();
                for (int i = 1; i <= colCount; i++) {
                    columns.add(meta.getColumnLabel(i));
                }

                List<List<String>> rows = new ArrayList<>();
                while (rs.next()) {
                    List<String> row = new ArrayList<>();
                    for (int i = 1; i <= colCount; i++) {
                        String val = rs.getString(i);
                        row.add(val != null ? val : "NULL");
                    }
                    rows.add(row);
                }

                return new QueryResult(columns, rows, null);
            } else {
                long affectedRows = stmt.getUpdateCount();
                return new QueryResult(List.of(), List.of(), affectedRows);
            }
        } catch (SQLException e) {
            log.error("MySQL query failed: {}", e.getMessage());
            throw ApiException.clusterConnectionFailed(e.getMessage());
        }
    }

    /**
     * Execute a SQL statement (INSERT, UPDATE, DELETE, etc.)
     */
    public void execute(String sql) {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            log.error("MySQL execute failed: {}", e.getMessage());
            throw ApiException.clusterConnectionFailed(e.getMessage());
        }
    }

    /**
     * Execute a query and return the first column of the first row
     */
    public Optional<String> queryScalar(String sql) {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return Optional.ofNullable(rs.getString(1));
            }
            return Optional.empty();
        } catch (SQLException e) {
            log.error("MySQL scalar query failed: {}", e.getMessage());
            throw ApiException.clusterConnectionFailed(e.getMessage());
        }
    }

    /**
     * Execute USE database
     */
    public void useDatabase(String database) {
        execute("USE " + database);
    }

    public record QueryResult(List<String> columns, List<List<String>> rows, Long affectedRows) {}
}
