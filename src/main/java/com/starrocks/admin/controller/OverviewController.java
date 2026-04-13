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

    @Operation(summary = "Get cluster overview (active cluster)", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/clusters/overview")
    public Map<String, Object> getClusterOverview(HttpServletRequest request,
                                                    @RequestParam(defaultValue = "24h") String timeRange) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        return overviewService.getClusterOverview(cluster, TimeRange.fromValue(timeRange));
    }

    @Operation(summary = "Get extended overview", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/clusters/overview/extended")
    public Map<String, Object> getExtendedOverview(HttpServletRequest request,
                                                     @RequestParam(defaultValue = "24h") String timeRange) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        return overviewService.getExtendedOverview(cluster, TimeRange.fromValue(timeRange));
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
    @GetMapping("/api/clusters/overview/compaction-details")
    public Map<String, Object> getCompactionDetailStats(HttpServletRequest request,
                                                          @RequestParam(defaultValue = "24h") String timeRange) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        return overviewService.getCompactionDetailStats(cluster, timeRange);
    }

    // Per-cluster overview endpoints
    @Operation(summary = "Get cluster overview by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/clusters/{clusterId}/overview")
    public Map<String, Object> getClusterOverviewById(HttpServletRequest request,
                                                       @PathVariable Long clusterId,
                                                       @RequestParam(defaultValue = "24h") String timeRange) {
        Cluster cluster = clusterService.getClusterEntityById(clusterId);
        return overviewService.getClusterOverview(cluster, TimeRange.fromValue(timeRange));
    }

    @Operation(summary = "Get cluster health by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/clusters/{clusterId}/overview/health")
    public Map<String, Object> getClusterHealthById(HttpServletRequest request,
                                                     @PathVariable Long clusterId) {
        Cluster cluster = clusterService.getClusterEntityById(clusterId);
        return overviewService.getClusterOverview(cluster, TimeRange.fromValue("1h"));
    }

    @Operation(summary = "Get performance trends by cluster ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/clusters/{clusterId}/overview/performance")
    public Map<String, Object> getPerformanceTrendsById(HttpServletRequest request,
                                                         @PathVariable Long clusterId,
                                                         @RequestParam(defaultValue = "24h") String timeRange) {
        Cluster cluster = clusterService.getClusterEntityById(clusterId);
        return overviewService.getPerformanceTrends(cluster, TimeRange.fromValue(timeRange));
    }

    @Operation(summary = "Get resource trends by cluster ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/clusters/{clusterId}/overview/resources")
    public Map<String, Object> getResourceTrendsById(HttpServletRequest request,
                                                      @PathVariable Long clusterId,
                                                      @RequestParam(defaultValue = "24h") String timeRange) {
        Cluster cluster = clusterService.getClusterEntityById(clusterId);
        return overviewService.getResourceTrends(cluster, TimeRange.fromValue(timeRange));
    }

    @Operation(summary = "Get data statistics by cluster ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/clusters/{clusterId}/overview/data-stats")
    public Map<String, Object> getDataStats(HttpServletRequest request,
                                              @PathVariable Long clusterId) {
        Cluster cluster = clusterService.getClusterEntityById(clusterId);
        return overviewService.getDataStats(cluster);
    }

    @Operation(summary = "Get capacity prediction by cluster ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/clusters/{clusterId}/overview/capacity-prediction")
    public Map<String, Object> getCapacityPrediction(HttpServletRequest request,
                                                      @PathVariable Long clusterId) {
        Cluster cluster = clusterService.getClusterEntityById(clusterId);
        return overviewService.getCapacityPrediction(cluster);
    }
}
