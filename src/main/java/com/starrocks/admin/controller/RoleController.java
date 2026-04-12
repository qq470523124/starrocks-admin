package com.starrocks.admin.controller;

import com.starrocks.admin.model.dto.request.*;
import com.starrocks.admin.model.dto.response.*;
import com.starrocks.admin.security.OrgContext;
import com.starrocks.admin.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Roles", description = "Role management endpoints")
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "List all roles", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public List<RoleResponse> listRoles(HttpServletRequest request) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        return roleService.listRoles(ctx.getOrganizationId(), ctx.isSuperAdmin());
    }

    @Operation(summary = "Create a new role", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public RoleResponse createRole(HttpServletRequest request, @Valid @RequestBody CreateRoleRequest req) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        return roleService.createRole(req, ctx.getOrganizationId(), ctx.isSuperAdmin());
    }

    @Operation(summary = "Get role by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}")
    public RoleResponse getRole(@PathVariable Long id) {
        return roleService.getRole(id);
    }

    @Operation(summary = "Update role", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}")
    public RoleResponse updateRole(HttpServletRequest request, @PathVariable Long id,
                                    @Valid @RequestBody UpdateRoleRequest req) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        return roleService.updateRole(id, req, ctx.getOrganizationId(), ctx.isSuperAdmin());
    }

    @Operation(summary = "Delete role", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    public void deleteRole(HttpServletRequest request, @PathVariable Long id) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        roleService.deleteRole(id, ctx.getOrganizationId(), ctx.isSuperAdmin());
    }

    @Operation(summary = "Get role with permissions", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}/permissions")
    public RoleWithPermissionsResponse getRoleWithPermissions(@PathVariable Long id) {
        return roleService.getRoleWithPermissions(id);
    }

    @Operation(summary = "Update role permissions", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}/permissions")
    public void updateRolePermissions(HttpServletRequest request, @PathVariable Long id,
                                       @Valid @RequestBody UpdateRolePermissionsRequest req) {
        OrgContext ctx = (OrgContext) request.getAttribute("orgContext");
        roleService.assignPermissionsToRole(id, req, ctx.getOrganizationId(), ctx.isSuperAdmin());
    }
}
