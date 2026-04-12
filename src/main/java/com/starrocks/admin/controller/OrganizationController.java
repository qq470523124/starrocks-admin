package com.starrocks.admin.controller;

import com.starrocks.admin.model.dto.request.*;
import com.starrocks.admin.model.dto.response.*;
import com.starrocks.admin.security.OrgContext;
import com.starrocks.admin.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Organizations", description = "Organization management endpoints")
@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @Operation(summary = "List organizations", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public List<OrganizationResponse> listOrganizations(HttpServletRequest request) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        return organizationService.listOrganizations(ctx.getOrganizationId(), ctx.isSuperAdmin());
    }

    @Operation(summary = "Create organization", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public OrganizationResponse createOrganization(HttpServletRequest request,
                                                   @Valid @RequestBody CreateOrganizationRequest req) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        return organizationService.createOrganization(req, ctx.getOrganizationId(), ctx.isSuperAdmin());
    }

    @Operation(summary = "Get organization by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}")
    public OrganizationResponse getOrganization(@PathVariable Long id) {
        return organizationService.getOrganization(id);
    }

    @Operation(summary = "Update organization", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}")
    public OrganizationResponse updateOrganization(HttpServletRequest request, @PathVariable Long id,
                                                    @Valid @RequestBody UpdateOrganizationRequest req) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        return organizationService.updateOrganization(id, req, ctx.getOrganizationId(), ctx.isSuperAdmin());
    }

    @Operation(summary = "Delete organization", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    public void deleteOrganization(HttpServletRequest request, @PathVariable Long id) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        organizationService.deleteOrganization(id, ctx.getOrganizationId(), ctx.isSuperAdmin());
    }
}
