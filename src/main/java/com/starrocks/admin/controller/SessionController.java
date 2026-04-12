package com.starrocks.admin.controller;

import com.starrocks.admin.model.dto.response.SessionResponse;
import com.starrocks.admin.security.OrgContext;
import com.starrocks.admin.service.SessionService;
import com.starrocks.admin.service.ClusterService;
import com.starrocks.admin.model.entity.Cluster;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Sessions", description = "Session management endpoints")
@RestController
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;
    private final ClusterService clusterService;

    @Operation(summary = "Get all sessions", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/clusters/sessions")
    public List<SessionResponse> getSessions(HttpServletRequest request) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        return sessionService.getSessions(cluster);
    }

    @Operation(summary = "Kill session", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/api/clusters/sessions/{sessionId}")
    public Map<String, String> killSession(HttpServletRequest request, @PathVariable String sessionId) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        sessionService.killSession(cluster, sessionId);
        return Map.of("message", "Session " + sessionId + " killed successfully");
    }
}
