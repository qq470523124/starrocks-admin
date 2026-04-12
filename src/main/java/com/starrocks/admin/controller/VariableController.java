package com.starrocks.admin.controller;

import com.starrocks.admin.model.dto.request.UpdateVariableRequest;
import com.starrocks.admin.model.dto.response.VariableResponse;
import com.starrocks.admin.security.OrgContext;
import com.starrocks.admin.service.VariableService;
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

@Tag(name = "Variables", description = "System variables management")
@RestController
@RequiredArgsConstructor
public class VariableController {

    private final VariableService variableService;
    private final ClusterService clusterService;

    @Operation(summary = "Get system variables", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/clusters/variables")
    public List<VariableResponse> getVariables(HttpServletRequest request,
                                                 @RequestParam(defaultValue = "global") String type,
                                                 @RequestParam(required = false) String filter) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        return variableService.getVariables(cluster, type, filter);
    }

    @Operation(summary = "Update system variable", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/api/clusters/variables/{variableName}")
    public Map<String, String> updateVariable(HttpServletRequest request,
                                                @PathVariable String variableName,
                                                @Valid @RequestBody UpdateVariableRequest req) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        variableService.updateVariable(cluster, variableName, req);
        return Map.of("message", "Variable updated successfully");
    }
}
