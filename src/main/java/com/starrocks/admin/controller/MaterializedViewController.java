package com.starrocks.admin.controller;

import com.starrocks.admin.model.dto.request.*;
import com.starrocks.admin.model.dto.response.MaterializedViewResponse;
import com.starrocks.admin.security.OrgContext;
import com.starrocks.admin.service.MaterializedViewService;
import com.starrocks.admin.service.ClusterService;
import com.starrocks.admin.model.entity.Cluster;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Materialized Views", description = "Materialized view management")
@RestController
@RequiredArgsConstructor
public class MaterializedViewController {

    private final MaterializedViewService mvService;
    private final ClusterService clusterService;

    @Operation(summary = "List materialized views", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/clusters/materialized_views")
    public List<MaterializedViewResponse> listMaterializedViews(HttpServletRequest request,
                                                                  @RequestParam(required = false) String database) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        return mvService.listMaterializedViews(cluster, database);
    }

    @Operation(summary = "Create materialized view", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/api/clusters/materialized_views")
    public Map<String, String> createMaterializedView(HttpServletRequest request,
                                                       @RequestBody Map<String, String> body) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        mvService.createMaterializedView(cluster, body.get("sql"));
        return Map.of("message", "Materialized view created successfully");
    }

    @Operation(summary = "Refresh materialized view", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/api/clusters/materialized_views/{mvName}/refresh")
    public Map<String, String> refreshMaterializedView(HttpServletRequest request,
                                                        @PathVariable String mvName,
                                                        @Valid @RequestBody RefreshMaterializedViewRequest req) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        mvService.refreshMaterializedView(cluster, mvName, req);
        return Map.of("message", "Materialized view refresh triggered");
    }

    @Operation(summary = "Cancel refresh", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/api/clusters/materialized_views/{mvName}/cancel-refresh")
    public Map<String, String> cancelRefresh(HttpServletRequest request,
                                              @PathVariable String mvName,
                                              @RequestParam(defaultValue = "false") boolean force) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        mvService.cancelRefresh(cluster, mvName, force);
        return Map.of("message", "Refresh cancelled");
    }

    @Operation(summary = "Get MV DDL", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/clusters/materialized_views/{mvName}/ddl")
    public Map<String, String> getDDL(HttpServletRequest request, @PathVariable String mvName) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        return mvService.getMaterializedViewDDL(cluster, mvName);
    }

    @Operation(summary = "Alter materialized view", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/api/clusters/materialized_views/{mvName}")
    public Map<String, String> alterMaterializedView(HttpServletRequest request,
                                                      @PathVariable String mvName,
                                                      @Valid @RequestBody AlterMaterializedViewRequest req) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        mvService.alterMaterializedView(cluster, mvName, req.getAlterClause());
        return Map.of("message", "Materialized view altered successfully");
    }

    @Operation(summary = "Drop materialized view", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/api/clusters/materialized_views/{mvName}")
    public Map<String, String> dropMaterializedView(HttpServletRequest request,
                                                      @PathVariable String mvName,
                                                      @RequestParam(defaultValue = "false") boolean ifExists) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        mvService.dropMaterializedView(cluster, mvName, ifExists);
        return Map.of("message", "Materialized view dropped successfully");
    }
}
