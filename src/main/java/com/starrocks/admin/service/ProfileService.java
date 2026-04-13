package com.starrocks.admin.service;

import com.starrocks.admin.client.MySQLClient;
import com.starrocks.admin.client.MySQLPoolManager;
import com.starrocks.admin.client.StarRocksHttpClient;
import com.starrocks.admin.exception.ApiException;
import com.starrocks.admin.model.dto.response.*;
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
public class ProfileService {

    private final MySQLPoolManager mysqlPoolManager;
    private final StarRocksHttpClient starRocksHttpClient;

    public List<ProfileListItemResponse> listProfiles(Cluster cluster) {
        try (MySQLClient client = mysqlPoolManager.createClient(cluster)) {
            MySQLClient.QueryResult result = client.queryRaw("SHOW PROFILELIST");
            List<ProfileListItemResponse> profiles = new ArrayList<>();

            for (List<String> row : result.rows()) {
                if (row.size() < 5) continue;
                profiles.add(ProfileListItemResponse.builder()
                        .queryId(row.get(0))
                        .startTime(row.get(1))
                        .time(row.get(2))
                        .state(row.get(3))
                        .statement(row.get(4))
                        .build());
            }

            return profiles;
        }
    }

    public ProfileDetailResponse getProfile(Cluster cluster, String queryId) {
        String sanitized = sanitizeQueryId(queryId);

        try (MySQLClient client = mysqlPoolManager.createClient(cluster)) {
            String sql = "SHOW QUERY PROFILE '" + sanitized + "'";
            MySQLClient.QueryResult result = client.queryRaw(sql);

            if (result.rows().isEmpty()) {
                throw ApiException.queryNotFound(sanitized);
            }

            StringBuilder content = new StringBuilder();
            for (List<String> row : result.rows()) {
                if (!row.isEmpty()) {
                    content.append(row.get(0)).append("\n");
                }
            }

            return ProfileDetailResponse.builder()
                    .queryId(sanitized)
                    .profileContent(content.toString())
                    .build();
        }
    }

    public Map<String, String> getClusterVariables(Cluster cluster) {
        String[] varNames = {
                "query_timeout", "max_query_instances", "pipeline_dop",
                "parallel_fragment_exec_instance_num", "enable_pipeline_engine",
                "cbo_enable_rewrite_group_by_to_distinct", "enable_column_prune",
                "enable_predicate_pushdown", "enable_partition_prune",
                "max_scan_key_num", "batch_size"
        };

        StringBuilder sql = new StringBuilder("SHOW VARIABLES WHERE Variable_name IN (");
        for (int i = 0; i < varNames.length; i++) {
            if (i > 0) sql.append(",");
            sql.append("'").append(varNames[i]).append("'");
        }
        sql.append(")");

        try (MySQLClient client = mysqlPoolManager.createClient(cluster)) {
            MySQLClient.QueryResult result = client.queryRaw(sql.toString());
            Map<String, String> variables = new java.util.HashMap<>();
            for (List<String> row : result.rows()) {
                if (row.size() >= 2) {
                    variables.put(row.get(0), row.get(1));
                }
            }
            return variables;
        }
    }

    private String sanitizeQueryId(String queryId) {
        String id = queryId.trim();
        if (id.isEmpty() || id.length() > 64) {
            throw ApiException.invalidData("Invalid query_id format");
        }
        for (char c : id.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && c != '-' && c != '_') {
                throw ApiException.invalidData("Invalid query_id format");
            }
        }
        return id;
    }

    public Map<String, Object> analyzeProfile(Cluster cluster, String queryId) {
        ProfileDetailResponse profile = getProfile(cluster, queryId);
        Map<String, Object> analysis = new java.util.LinkedHashMap<>();
        analysis.put("query_id", queryId);
        analysis.put("sql", "");
        analysis.put("execution_time_ms", 0);
        analysis.put("status", "completed");
        analysis.put("summary", Map.of(
                "totalTime", "N/A",
                "scanRows", "N/A",
                "scanBytes", "N/A",
                "cpuTimeNs", "N/A",
                "memoryUsageBytes", "N/A"
        ));
        analysis.put("operators", List.of());
        analysis.put("optimization_suggestions", List.of());
        analysis.put("raw_profile", profile.getProfileContent());
        return analysis;
    }
}
