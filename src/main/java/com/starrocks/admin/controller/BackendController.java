package com.starrocks.admin.controller;

import com.starrocks.admin.model.dto.response.BackendResponse;
import com.starrocks.admin.security.OrgContext;
import com.starrocks.admin.service.BackendService;
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

@Tag(name = "Backends", description = "Backend/Compute node management")
@RestController
@RequiredArgsConstructor
public class BackendController {

    private final BackendService backendService;
    private final ClusterService clusterService;

    @Operation(summary = "List backends", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/clusters/backends")
    public List<BackendResponse> listBackends(HttpServletRequest request) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        return backendService.listBackends(cluster);
    }

    @Operation(summary = "Delete backend", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/api/clusters/backends/{host}/{port}")
    public Map<String, String> deleteBackend(HttpServletRequest request,
                                              @PathVariable String host, @PathVariable String port) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        backendService.dropBackend(cluster, host, port);
        return Map.of("message", "Backend " + host + ":" + port + " deleted successfully");
    }
}
