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

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final MySQLPoolManager mysqlPoolManager;

    /**
     * SHOW PROCESSLIST columns: Id, User, Host, db, Command, Time, State, Info [, ConnectionId]
     * Frontend expects: id, user, host, db, command, time, state, info
     */
    public List<SessionResponse> getSessions(Cluster cluster) {
        try (MySQLClient client = mysqlPoolManager.createClient(cluster)) {
            MySQLClient.QueryResult result = client.queryRaw("SHOW PROCESSLIST");
            List<SessionResponse> sessions = new ArrayList<>();

            for (List<String> row : result.rows()) {
                if (row.size() < 8) continue;
                SessionResponse session = SessionResponse.builder()
                        .id(getCol(row, 0))
                        .user(getCol(row, 1))
                        .host(getCol(row, 2))
                        .db(getCol(row, 3))
                        .command(getCol(row, 4))
                        .time(getCol(row, 5))
                        .state(getCol(row, 6))
                        .info(getCol(row, 7))
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

    private String getCol(List<String> row, int index) {
        return index < row.size() && row.get(index) != null ? row.get(index) : "";
    }
}
