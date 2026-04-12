package com.starrocks.admin.client;

import com.starrocks.admin.exception.ApiException;
import com.starrocks.admin.model.entity.Cluster;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class MySQLPoolManager {

    private final DataSource starrocksDataSource;
    private final Map<Long, javax.sql.DataSource> poolCache = new ConcurrentHashMap<>();

    public MySQLPoolManager() {
        // StarRocks connections are created dynamically per cluster
        // This manager creates HikariCP DataSource instances on demand
        this.starrocksDataSource = null;
    }

    /**
     * Get or create a connection for the given cluster
     */
    public Connection getConnection(Cluster cluster) {
        try {
            String url = buildJdbcUrl(cluster);
            return java.sql.DriverManager.getConnection(
                    url,
                    cluster.getUsername(),
                    cluster.getPasswordEncrypted()
            );
        } catch (SQLException e) {
            log.error("Failed to connect to cluster {}: {}", cluster.getName(), e.getMessage());
            throw ApiException.clusterConnectionFailed(e.getMessage());
        }
    }

    /**
     * Create a MySQLClient for the given cluster
     */
    public MySQLClient createClient(Cluster cluster) {
        Connection conn = getConnection(cluster);
        return new MySQLClient(conn);
    }

    private String buildJdbcUrl(Cluster cluster) {
        String ssl = Boolean.TRUE.equals(cluster.getEnableSsl()) ? "?useSSL=true" : "?useSSL=false";
        return "jdbc:mysql://" + cluster.getFeHost() + ":" + cluster.getFeQueryPort() + ssl +
                "&allowPublicKeyRetrieval=true&connectTimeout=" + (cluster.getConnectionTimeout() * 1000);
    }
}
