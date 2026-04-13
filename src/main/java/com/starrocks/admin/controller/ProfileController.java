package com.starrocks.admin.controller;

import com.starrocks.admin.model.dto.response.*;
import com.starrocks.admin.security.OrgContext;
import com.starrocks.admin.service.ProfileService;
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

@Tag(name = "Profiles", description = "Query profile analysis")
@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final ClusterService clusterService;

    @Operation(summary = "List query profiles", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/clusters/profiles")
    public List<ProfileListItemResponse> listProfiles(HttpServletRequest request) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        return profileService.listProfiles(cluster);
    }

    @Operation(summary = "Get query profile detail", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/clusters/profiles/{queryId}")
    public ProfileDetailResponse getProfile(HttpServletRequest request, @PathVariable String queryId) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        return profileService.getProfile(cluster, queryId);
    }

    @Operation(summary = "Analyze query profile", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/clusters/profiles/{queryId}/analyze")
    public Map<String, Object> analyzeProfile(HttpServletRequest request, @PathVariable String queryId) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        return profileService.analyzeProfile(cluster, queryId);
    }
}
