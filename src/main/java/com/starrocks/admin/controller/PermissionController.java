package com.starrocks.admin.controller;

import com.starrocks.admin.model.dto.response.*;
import com.starrocks.admin.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Permissions", description = "Permission management endpoints")
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "List all permissions", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public List<PermissionResponse> listPermissions() {
        return permissionService.listPermissions();
    }

    @Operation(summary = "List menu permissions", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/menu")
    public List<PermissionResponse> listMenuPermissions() {
        return permissionService.listMenuPermissions();
    }

    @Operation(summary = "List API permissions", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api")
    public List<PermissionResponse> listApiPermissions() {
        return permissionService.listApiPermissions();
    }

    @Operation(summary = "Get permission tree", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/tree")
    public List<PermissionTreeResponse> getPermissionTree() {
        return permissionService.getPermissionTree();
    }
}
