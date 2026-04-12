package com.starrocks.admin.service;

import com.starrocks.admin.client.MySQLClient;
import com.starrocks.admin.client.MySQLPoolManager;
import com.starrocks.admin.exception.ApiException;
import com.starrocks.admin.model.dto.response.SessionResponse;
import com.starrocks.admin.model.entity.Cluster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final MySQLPoolManager mysqlPoolManager;

    public List<SessionResponse> getSessions(Cluster cluster) {
        try (MySQLClient client = mysqlPoolManager.createClient(cluster)) {
            MySQLClient.QueryResult result = client.queryRaw("SHOW PROCESSLIST");
            List<SessionResponse> sessions = new ArrayList<>();

            for (List<String> row : result.rows()) {
                if (row.size() < 9) continue;
                SessionResponse session = SessionResponse.builder()
                        .threadId(row.get(0))
                        .user(row.get(1))
                        .defaultDb(row.get(2))
                        .command(row.get(3))
                        .startTime(row.get(5))
                        .queryTime(row.get(4))
                        .state(row.get(6))
                        .info(row.get(7))
                        .build();
                sessions.add(session);
            }

            log.info("Fetched {} active sessions", sessions.size());
            return sessions;
        }
    }

    public void killSession(Cluster cluster, String sessionId) {
        try (MySQLClient client = mysqlPoolManager.createClient(cluster)) {
            log.info("Killing session: {}", sessionId);
            try {
                client.execute("KILL CONNECTION " + sessionId);
                log.info("Successfully killed session: {}", sessionId);
            } catch (Exception e) {
                log.warn("KILL CONNECTION failed, trying KILL: {}", e.getMessage());
                client.execute("KILL " + sessionId);
                log.info("Successfully killed session with KILL: {}", sessionId);
            }
        }
    }
}
