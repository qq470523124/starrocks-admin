package com.starrocks.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppConfig {

    private ServerConfig server = new ServerConfig();
    private DatabaseConfig database = new DatabaseConfig();
    private AuthConfig auth = new AuthConfig();
    private LoggingConfig logging = new LoggingConfig();
    private StaticConfig staticConfig = new StaticConfig();
    private MetricsConfig metrics = new MetricsConfig();
    private AuditLogConfig audit = new AuditLogConfig();

    @Data
    public static class ServerConfig {
        private String host = "0.0.0.0";
        private int port = 8080;
    }

    @Data
    public static class DatabaseConfig {
        private String path = "./data/starrocks_admin.db";
    }

    @Data
    public static class AuthConfig {
        private String jwtSecret = "change-me-in-production-please";
        private String jwtExpiresIn = "24h";
    }

    @Data
    public static class LoggingConfig {
        private String level = "INFO";
        private String file = "./logs/starrocks-admin.log";
    }

    @Data
    public static class StaticConfig {
        private boolean enabled = true;
    }

    @Data
    public static class MetricsConfig {
        private boolean enabled = true;
        private int intervalSecs = 30;
        private int retentionDays = 7;
    }

    @Data
    public static class AuditLogConfig {
        private String database = "starrocks_audit_db__";
        private String table = "starrocks_audit_tbl__";

        public String getFullTableName() {
            return database + "." + table;
        }
    }
}
