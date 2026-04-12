package com.starrocks.admin.security;

import org.springframework.stereotype.Component;

@Component
public class PermissionExtractor {

    public record PermissionCheck(String resource, String action) {}

    public PermissionCheck extract(String method, String uri) {
        // Routes that don't require permission check (only require authentication)
        if (uri.equals("/api/auth/permissions")) {
            return null;
        }
        if (uri.equals("/api/clusters/active") && "GET".equalsIgnoreCase(method)) {
            return null;
        }

        String path = uri.startsWith("/api/") ? uri.substring(5) : uri;
        String[] segments = path.split("/");

        String resource = switch (segments[0]) {
            case "roles" -> "roles";
            case "permissions" -> "permissions";
            case "users" -> "users";
            case "clusters" -> "clusters";
            case "organizations" -> "organizations";
            default -> null;
        };

        if (resource == null) {
            return null;
        }

        // Try special extractors for clusters
        if ("clusters".equals(resource)) {
            PermissionCheck special = extractClusterAction(segments, method);
            if (special != null) return special;
        }

        // Default extraction
        return extractDefaultAction(segments, method);
    }

    private PermissionCheck extractClusterAction(String[] segments, String method) {
        if (segments.length < 2) {
            return switch (method.toUpperCase()) {
                case "GET" -> new PermissionCheck("clusters", "list");
                case "POST" -> new PermissionCheck("clusters", "create");
                default -> null;
            };
        }

        String second = segments[1];

        // Numeric ID
        if (isNumeric(second)) {
            return switch (method.toUpperCase()) {
                case "GET" -> new PermissionCheck("clusters", "get");
                case "PUT" -> new PermissionCheck("clusters", "update");
                case "DELETE" -> new PermissionCheck("clusters", "delete");
                default -> null;
            };
        }

        // Named sub-routes
        return switch (second) {
            case "active" -> new PermissionCheck("clusters", "active");
            case "health" -> new PermissionCheck("clusters", "health");
            case "test-connection" -> new PermissionCheck("clusters", "test_connection");
            case "catalogs" -> new PermissionCheck("clusters", "catalogs");
            case "databases" -> new PermissionCheck("clusters", "databases");
            case "tables" -> new PermissionCheck("clusters", "tables");
            case "execute" -> new PermissionCheck("clusters", "execute");
            case "materialized_views" -> new PermissionCheck("clusters", "materialized_views");
            case "sessions" -> new PermissionCheck("clusters", "sessions");
            case "variables" -> new PermissionCheck("clusters", "variables");
            case "queries" -> new PermissionCheck("clusters", "queries");
            case "system" -> new PermissionCheck("clusters", "system");
            case "backends" -> new PermissionCheck("clusters", "backends");
            case "frontends" -> new PermissionCheck("clusters", "frontends");
            case "system-functions" -> new PermissionCheck("clusters", "system_functions");
            case "overview" -> new PermissionCheck("clusters", "overview");
            case "metrics" -> new PermissionCheck("clusters", "metrics");
            case "statistics" -> new PermissionCheck("clusters", "statistics");
            case "profiles" -> new PermissionCheck("clusters", "profiles");
            default -> null;
        };
    }

    private PermissionCheck extractDefaultAction(String[] segments, String method) {
        if (segments.length < 2) {
            return switch (method.toUpperCase()) {
                case "GET" -> new PermissionCheck(segments[0], "list");
                case "POST" -> new PermissionCheck(segments[0], "create");
                default -> null;
            };
        }

        if (isNumeric(segments[1])) {
            return switch (method.toUpperCase()) {
                case "GET" -> new PermissionCheck(segments[0], "get");
                case "PUT" -> new PermissionCheck(segments[0], "update");
                case "DELETE" -> new PermissionCheck(segments[0], "delete");
                default -> null;
            };
        }

        return switch (method.toUpperCase()) {
            case "GET" -> new PermissionCheck(segments[0], segments[1]);
            default -> null;
        };
    }

    private boolean isNumeric(String s) {
        if (s == null || s.isEmpty()) return false;
        for (char c : s.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }
}
