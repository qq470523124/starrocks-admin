package com.starrocks.admin.controller;

import com.starrocks.admin.model.dto.request.*;
import com.starrocks.admin.model.dto.response.*;
import com.starrocks.admin.security.OrgContext;
import com.starrocks.admin.service.ClusterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Clusters", description = "Cluster management endpoints")
@RestController
@RequestMapping("/api/clusters")
@RequiredArgsConstructor
public class ClusterController {

    private final ClusterService clusterService;

    @Operation(summary = "Create a new cluster", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ClusterResponse createCluster(HttpServletRequest request, @Valid @RequestBody CreateClusterRequest req) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        return clusterService.createCluster(req, ctx.getUserId(), ctx.getOrganizationId(), ctx.isSuperAdmin());
    }

    @Operation(summary = "List all clusters", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public List<ClusterResponse> listClusters(HttpServletRequest request) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        return clusterService.listClusters(ctx.getOrganizationId(), ctx.isSuperAdmin());
    }

    @Operation(summary = "Get cluster by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}")
    public ClusterResponse getCluster(HttpServletRequest request, @PathVariable Long id) {
        return clusterService.getCluster(id);
    }

    @Operation(summary = "Update cluster", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}")
    public ClusterResponse updateCluster(HttpServletRequest request, @PathVariable Long id,
                                          @Valid @RequestBody UpdateClusterRequest req) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        return clusterService.updateCluster(id, req, ctx.getOrganizationId(), ctx.isSuperAdmin());
    }

    @Operation(summary = "Delete cluster", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    public void deleteCluster(HttpServletRequest request, @PathVariable Long id) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        clusterService.deleteCluster(id, ctx.getOrganizationId(), ctx.isSuperAdmin());
    }

    @Operation(summary = "Set active cluster", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}/activate")
    public ClusterResponse activateCluster(HttpServletRequest request, @PathVariable Long id) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        return clusterService.activateCluster(id, ctx.getOrganizationId(), ctx.isSuperAdmin());
    }

    @Operation(summary = "Get active cluster", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/active")
    public ClusterResponse getActiveCluster(HttpServletRequest request) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        return clusterService.getActiveCluster(ctx.getOrganizationId(), ctx.isSuperAdmin());
    }

    @Operation(summary = "Get cluster health", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}/health")
    public ClusterHealthResponse getClusterHealth(HttpServletRequest request, @PathVariable Long id) {
        return clusterService.getClusterHealth(id);
    }

    @Operation(summary = "Test cluster connection", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/health/test")
    public ClusterHealthResponse testConnection(@RequestBody HealthCheckRequest req) {
        return clusterService.testConnection(req);
    }
}
