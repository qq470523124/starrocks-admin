package com.starrocks.admin.controller;

import com.starrocks.admin.model.dto.request.*;
import com.starrocks.admin.model.dto.response.*;
import com.starrocks.admin.security.OrgContext;
import com.starrocks.admin.service.AuthService;
import com.starrocks.admin.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "User authentication endpoints")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PermissionService permissionService;

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody RegisterRequest req) {
        var user = authService.register(req);
        return authService.toUserResponse(user);
    }

    @Operation(summary = "Login")
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @Operation(summary = "Get current user info", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me")
    public UserResponse getMe(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return authService.getCurrentUser(userId);
    }

    @Operation(summary = "Update current user", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/me")
    public UserResponse updateMe(HttpServletRequest request, @Valid @RequestBody UpdateUserRequest req) {
        Long userId = (Long) request.getAttribute("userId");
        var user = authService.updateUser(userId, req);
        return authService.toUserResponse(user);
    }

    @Operation(summary = "Get current user permissions", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/permissions")
    public java.util.List<PermissionResponse> getCurrentUserPermissions(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return permissionService.getUserPermissions(userId);
    }
}
