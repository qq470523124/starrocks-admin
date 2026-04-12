package com.starrocks.admin.controller;

import com.starrocks.admin.model.dto.request.*;
import com.starrocks.admin.model.dto.response.*;
import com.starrocks.admin.security.OrgContext;
import com.starrocks.admin.service.QueryService;
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

@Tag(name = "Queries", description = "SQL query execution endpoints")
@RestController
@RequiredArgsConstructor
public class QueryController {

    private final QueryService queryService;
    private final ClusterService clusterService;

    @Operation(summary = "List catalogs", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/clusters/catalogs")
    public List<String> listCatalogs(HttpServletRequest request) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        return queryService.listCatalogs(cluster);
    }

    @Operation(summary = "Get catalogs with databases", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/clusters/catalogs/databases")
    public CatalogsWithDatabasesResponse getCatalogsWithDatabases(HttpServletRequest request) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        return queryService.getCatalogsWithDatabases(cluster);
    }

    @Operation(summary = "List databases", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/clusters/databases")
    public List<String> listDatabases(HttpServletRequest request) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        return queryService.listDatabases(cluster);
    }

    @Operation(summary = "List tables", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/clusters/tables")
    public List<String> listTables(HttpServletRequest request, @RequestParam String database) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        return queryService.listTables(cluster, database);
    }

    @Operation(summary = "Execute SQL query", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/api/clusters/query")
    public QueryExecuteResponse executeQuery(HttpServletRequest request,
                                              @Valid @RequestBody QueryExecuteRequest req) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        return queryService.executeQuery(cluster, req);
    }

    @Operation(summary = "Get table DDL", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/clusters/tables/{database}/{table}/ddl")
    public Map<String, String> getTableDDL(HttpServletRequest request,
                                             @PathVariable String database,
                                             @PathVariable String table) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        String ddl = queryService.getTableDDL(cluster, database, table);
        return Map.of("ddl", ddl);
    }
}
