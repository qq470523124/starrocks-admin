package com.starrocks.admin.controller;

import com.starrocks.admin.model.dto.request.*;
import com.starrocks.admin.model.dto.response.*;
import com.starrocks.admin.security.OrgContext;
import com.starrocks.admin.service.SystemFunctionService;
import com.starrocks.admin.service.ClusterService;
import com.starrocks.admin.model.entity.Cluster;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "System Functions", description = "Custom SQL function management")
@RestController
@RequiredArgsConstructor
public class SystemFunctionController {

    private final SystemFunctionService systemFunctionService;
    private final ClusterService clusterService;

    @Operation(summary = "Get system functions", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/clusters/system-functions")
    public List<com.starrocks.admin.model.entity.SystemFunction> getSystemFunctions(HttpServletRequest request) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        return systemFunctionService.getFunctions(cluster.getId());
    }

    @Operation(summary = "Create system function", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/api/clusters/system-functions")
    public com.starrocks.admin.model.entity.SystemFunction createSystemFunction(
            HttpServletRequest request, @Valid @RequestBody CreateFunctionRequest req) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        return systemFunctionService.createFunction(cluster.getId(), req, ctx.getUserId());
    }

    @Operation(summary = "Update system function", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/api/clusters/system-functions/{functionId}")
    public com.starrocks.admin.model.entity.SystemFunction updateSystemFunction(
            HttpServletRequest request, @PathVariable Long functionId,
            @Valid @RequestBody UpdateFunctionRequest req) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        return systemFunctionService.updateFunction(cluster.getId(), functionId, req);
    }

    @Operation(summary = "Delete system function", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/api/clusters/system-functions/{functionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSystemFunction(HttpServletRequest request, @PathVariable Long functionId) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        systemFunctionService.deleteFunction(cluster.getId(), functionId);
    }

    @Operation(summary = "Update function order", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/api/clusters/system-functions/order")
    public void updateOrder(HttpServletRequest request, @Valid @RequestBody UpdateOrderRequest req) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        Cluster cluster = clusterService.getActiveClusterEntity(ctx.getOrganizationId(), ctx.isSuperAdmin());
        systemFunctionService.updateOrder(cluster.getId(), req);
    }

    @Operation(summary = "Update function access time", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/api/system-functions/{functionName}/access-time")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateAccessTime(@PathVariable String functionName) {
        systemFunctionService.updateAccessTime(functionName);
    }

    @Operation(summary = "Delete function category", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/api/system-functions/category/{categoryName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable String categoryName) {
        systemFunctionService.deleteCategory(categoryName);
    }
}
