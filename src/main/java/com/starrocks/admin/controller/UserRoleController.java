package com.starrocks.admin.controller;

import com.starrocks.admin.model.dto.request.*;
import com.starrocks.admin.model.dto.response.*;
import com.starrocks.admin.security.OrgContext;
import com.starrocks.admin.service.UserRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Users", description = "User role management endpoints")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserRoleController {

    private final UserRoleService userRoleService;

    @Operation(summary = "Get user's roles", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}/roles")
    public List<RoleResponse> getUserRoles(@PathVariable Long id) {
        return userRoleService.getUserRoles(id);
    }

    @Operation(summary = "Assign role to user", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{id}/roles")
    public void assignRoleToUser(@PathVariable Long id, @Valid @RequestBody AssignUserRoleRequest req) {
        userRoleService.assignRoleToUser(id, req);
    }

    @Operation(summary = "Remove role from user", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}/roles/{roleId}")
    public void removeRoleFromUser(@PathVariable Long id, @PathVariable Long roleId) {
        userRoleService.removeRoleFromUser(id, roleId);
    }
}
