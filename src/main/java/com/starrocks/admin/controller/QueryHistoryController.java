package com.starrocks.admin.controller;

import com.starrocks.admin.model.dto.response.QueryHistoryResponse;
import com.starrocks.admin.security.OrgContext;
import com.starrocks.admin.service.QueryHistoryService;
import com.starrocks.admin.service.ClusterService;
import com.starrocks.admin.model.entity.Cluster;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Queries", description = "Query history")
@RestController
@RequiredArgsConstructor
public class QueryHistoryController {

    private final QueryHistoryService queryHistoryService;
    private final ClusterService clusterService;

    @Operation(summary = "List query history", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/clusters/queries/history")
    public QueryHistoryResponse listQueryHistory(HttpServletRequest request,
                                                  @RequestParam(defaultValue = "10") int limit,
                                                  @RequestParam(defaultValue = "0") int offset,
                                                  @RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) String startTime,
                                                  @RequestParam(required = false) String endTime) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        return queryHistoryService.listQueryHistory(cluster, limit, offset, keyword, startTime, endTime);
    }
}
