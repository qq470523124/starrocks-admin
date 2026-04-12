package com.starrocks.admin.service;

import com.starrocks.admin.client.MySQLClient;
import com.starrocks.admin.client.MySQLPoolManager;
import com.starrocks.admin.config.AppConfig;
import com.starrocks.admin.model.entity.Cluster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final MySQLPoolManager mysqlPoolManager;
    private final AppConfig appConfig;

    public void logQuery(Cluster cluster, String queryId, String user, String database,
                        String sql, String queryType, long durationMs, String state) {
        // Audit logging is handled by StarRocks itself via the audit table
        // This service provides helper methods to query audit logs
        log.debug("Audit: queryId={}, user={}, db={}, type={}, duration={}ms, state={}",
                queryId, user, database, queryType, durationMs, state);
    }

    public String getAuditTableName() {
        return appConfig.getAudit().getFullTableName();
    }
}
