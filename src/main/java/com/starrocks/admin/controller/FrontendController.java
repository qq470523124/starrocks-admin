package com.starrocks.admin.controller;

import com.starrocks.admin.model.dto.response.FrontendResponse;
import com.starrocks.admin.security.OrgContext;
import com.starrocks.admin.service.FrontendService;
import com.starrocks.admin.service.ClusterService;
import com.starrocks.admin.model.entity.Cluster;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Frontends", description = "Frontend node management")
@RestController
@RequiredArgsConstructor
public class FrontendController {

    private final FrontendService frontendService;
    private final ClusterService clusterService;

    @Operation(summary = "List frontends", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/clusters/frontends")
    public List<FrontendResponse> listFrontends(HttpServletRequest request) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        return frontendService.listFrontends(cluster);
    }
}
