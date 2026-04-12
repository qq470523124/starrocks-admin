package com.starrocks.admin.controller;

import com.starrocks.admin.model.dto.response.RuntimeInfoResponse;
import com.starrocks.admin.security.OrgContext;
import com.starrocks.admin.service.SystemInfoService;
import com.starrocks.admin.service.ClusterService;
import com.starrocks.admin.model.entity.Cluster;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "System", description = "System information")
@RestController
@RequiredArgsConstructor
public class SystemController {

    private final SystemInfoService systemInfoService;
    private final ClusterService clusterService;

    @Operation(summary = "Get runtime info", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/clusters/system/runtime_info")
    public RuntimeInfoResponse getRuntimeInfo(HttpServletRequest request) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        return systemInfoService.getRuntimeInfo(cluster);
    }
}
