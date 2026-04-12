package com.starrocks.admin.service;

import com.starrocks.admin.client.MySQLClient;
import com.starrocks.admin.client.MySQLPoolManager;
import com.starrocks.admin.exception.ApiException;
import com.starrocks.admin.model.dto.request.UpdateVariableRequest;
import com.starrocks.admin.model.dto.response.VariableResponse;
import com.starrocks.admin.model.entity.Cluster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VariableService {

    private final MySQLPoolManager mysqlPoolManager;

    public List<VariableResponse> getVariables(Cluster cluster, String type, String filter) {
        String scope = "GLOBAL".equalsIgnoreCase(type) ? "GLOBAL" : "SESSION";
        String sql = "SHOW " + scope + " VARIABLES";
        if (filter != null && !filter.isBlank()) {
            sql += " LIKE '%" + filter.replace("'", "") + "%'";
        }

        try (MySQLClient client = mysqlPoolManager.createClient(cluster)) {
            MySQLClient.QueryResult result = client.queryRaw(sql);
            List<VariableResponse> variables = new ArrayList<>();

            for (List<String> row : result.rows()) {
                if (row.size() < 2) continue;
                variables.add(VariableResponse.builder()
                        .variableName(row.get(0))
                        .value(row.get(1))
                        .build());
            }

            return variables;
        }
    }

    public void updateVariable(Cluster cluster, String variableName, UpdateVariableRequest req) {
        String scope = req.getScope().toUpperCase();
        if (!"GLOBAL".equals(scope) && !"SESSION".equals(scope)) {
            throw ApiException.invalidData("Invalid scope. Must be GLOBAL or SESSION");
        }

        String sql = "SET " + scope + " " + variableName + " = " + req.getValue();

        try (MySQLClient client = mysqlPoolManager.createClient(cluster)) {
            client.execute(sql);
        }
    }
}
