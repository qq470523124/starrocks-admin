package com.starrocks.admin.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starrocks.admin.exception.ApiException;
import com.starrocks.admin.exception.GlobalExceptionHandler;
import com.starrocks.admin.repository.UserOrganizationRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthFilter implements Filter {

    private final JwtUtil jwtUtil;
    private final CasbinService casbinService;
    private final UserOrganizationRepository userOrganizationRepository;
    private final PermissionExtractor permissionExtractor;
    private final ObjectMapper objectMapper;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();

        // Skip non-API paths
        if (!path.startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }

        // Skip public endpoints
        if (isPublicEndpoint(path, method)) {
            chain.doFilter(request, response);
            return;
        }

        // Extract JWT token
        String token = extractToken(httpRequest);
        if (token == null) {
            sendError(httpResponse, ApiException.unauthorized("Missing authentication token"));
            return;
        }

        // Verify token
        Claims claims;
        try {
            claims = jwtUtil.verifyToken(token);
        } catch (ApiException e) {
            sendError(httpResponse, e);
            return;
        }

        Long userId = Long.parseLong(claims.getSubject());
        String username = claims.get("username", String.class);

        // Build OrgContext
        boolean isSuperAdmin = casbinService.isSuperAdmin(userId);
        Long orgId = casbinService.getOrganizationId(userId);

        OrgContext orgContext = OrgContext.builder()
                .userId(userId)
                .username(username)
                .organizationId(orgId)
                .isSuperAdmin(isSuperAdmin)
                .build();

        // Permission check via Casbin
        PermissionExtractor.PermissionCheck permCheck = permissionExtractor.extract(method, path);
        if (permCheck != null && !isSuperAdmin) {
            String resourceScope = CasbinService.formatResourceKey(orgId, permCheck.resource());
            boolean allowed = casbinService.enforce(userId, resourceScope, permCheck.action());
            if (!allowed) {
                log.warn("Permission denied for user {} on {} {} (resource={}, action={})",
                        userId, method, path, permCheck.resource(), permCheck.action());
                sendError(httpResponse, ApiException.unauthorized(
                        "Permission denied: no access to " + permCheck.resource() + " " + permCheck.action()));
                return;
            }
        }

        // Store OrgContext in request attribute
        httpRequest.setAttribute("orgContext", orgContext);
        httpRequest.setAttribute("userId", userId);

        chain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String path, String method) {
        return path.equals("/api/auth/register") && method.equalsIgnoreCase("POST")
                || path.equals("/api/auth/login") && method.equalsIgnoreCase("POST");
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private void sendError(HttpServletResponse response, ApiException ex) throws IOException {
        response.setStatus(ex.getCode() >= 1001 && ex.getCode() <= 1999 ? 401 : 500);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        GlobalExceptionHandler.ApiErrorResponse errorResponse =
                new GlobalExceptionHandler.ApiErrorResponse(ex.getCode(), ex.getMessage(), null);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
