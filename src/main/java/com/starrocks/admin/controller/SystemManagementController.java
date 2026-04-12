package com.starrocks.admin.controller;

import com.starrocks.admin.model.dto.response.SystemFunctionDetailResponse;
import com.starrocks.admin.security.OrgContext;
import com.starrocks.admin.service.SystemManagementService;
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

@Tag(name = "System Management", description = "StarRocks system function management")
@RestController
@RequiredArgsConstructor
public class SystemManagementController {

    private final SystemManagementService systemManagementService;
    private final ClusterService clusterService;

    @Operation(summary = "List system functions", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/clusters/system")
    public List<Map<String, String>> listSystemFunctions(HttpServletRequest request,
                                                          @RequestParam(required = false) Integer limit,
                                                          @RequestParam(required = false) Integer offset,
                                                          @RequestParam(required = false) String filter) {
        return systemManagementService.listSystemFunctions();
    }

    @Operation(summary = "Get system function detail", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/clusters/system/{functionName}")
    public SystemFunctionDetailResponse getSystemFunctionDetail(HttpServletRequest request,
                                                                 @PathVariable String functionName,
                                                                 @RequestParam(required = false) Integer limit,
                                                                 @RequestParam(required = false) Integer offset,
                                                                 @RequestParam(required = false) String filter) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        return systemManagementService.getSystemFunctionDetail(cluster, functionName,
                limit != null ? limit : 100, offset != null ? offset : 0, filter);
    }
}
