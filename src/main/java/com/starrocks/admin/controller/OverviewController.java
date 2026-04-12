package com.starrocks.admin.controller;

import com.starrocks.admin.model.enums.TimeRange;
import com.starrocks.admin.security.OrgContext;
import com.starrocks.admin.service.OverviewService;
import com.starrocks.admin.service.ClusterService;
import com.starrocks.admin.model.entity.Cluster;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Cluster Overview", description = "Cluster overview and monitoring")
@RestController
@RequiredArgsConstructor
public class OverviewController {

    private final OverviewService overviewService;
    private final ClusterService clusterService;

    @Operation(summary = "Get cluster overview", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/clusters/overview")
    public Map<String, Object> getClusterOverview(HttpServletRequest request,
                                                    @RequestParam(defaultValue = "24h") String timeRange) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        return overviewService.getClusterOverview(cluster, TimeRange.fromValue(timeRange));
    }

    @Operation(summary = "Get performance trends", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/clusters/overview/performance")
    public Map<String, Object> getPerformanceTrends(HttpServletRequest request,
                                                      @RequestParam(defaultValue = "24h") String timeRange) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        return overviewService.getPerformanceTrends(cluster, TimeRange.fromValue(timeRange));
    }

    @Operation(summary = "Get resource trends", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/clusters/overview/resources")
    public Map<String, Object> getResourceTrends(HttpServletRequest request,
                                                   @RequestParam(defaultValue = "24h") String timeRange) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        return overviewService.getResourceTrends(cluster, TimeRange.fromValue(timeRange));
    }

    @Operation(summary = "Get compaction detail stats", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/clusters/overview/compaction")
    public Map<String, Object> getCompactionDetailStats(HttpServletRequest request,
                                                          @RequestParam(defaultValue = "24h") String timeRange) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        return overviewService.getCompactionDetailStats(cluster, timeRange);
    }
}
