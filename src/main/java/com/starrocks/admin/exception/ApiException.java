package com.starrocks.admin.exception;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

    private final int code;
    private final Object details;

    public ApiException(int code, String message) {
        super(message);
        this.code = code;
        this.details = null;
    }

    public ApiException(int code, String message, Object details) {
        super(message);
        this.code = code;
        this.details = details;
    }

    // Authentication errors 1xxx
    public static ApiException unauthorized(String message) {
        return new ApiException(1001, message);
    }

    public static ApiException tokenExpired() {
        return new ApiException(1002, "Token expired");
    }

    public static ApiException invalidCredentials() {
        return new ApiException(1003, "Invalid credentials");
    }

    // Cluster errors 2xxx
    public static ApiException clusterNotFound(long clusterId) {
        return new ApiException(2001, "Cluster " + clusterId + " not found");
    }

    public static ApiException clusterConnectionFailed(String message) {
        return new ApiException(2002, "Failed to connect to cluster: " + message);
    }

    public static ApiException clusterTimeout() {
        return new ApiException(2003, "Cluster operation timeout");
    }

    public static ApiException clusterAuthFailed() {
        return new ApiException(2004, "Cluster authentication failed");
    }

    // Resource errors 3xxx
    public static ApiException resourceNotFound(String resource) {
        return new ApiException(3001, "Resource not found: " + resource);
    }

    public static ApiException queryNotFound(String queryId) {
        return new ApiException(3002, "Query " + queryId + " not found");
    }

    public static ApiException userNotFound(long userId) {
        return new ApiException(3003, "User " + userId + " not found");
    }

    public static ApiException roleNotFound(long roleId) {
        return new ApiException(3004, "Role " + roleId + " not found");
    }

    public static ApiException organizationNotFound(long orgId) {
        return new ApiException(3005, "Organization " + orgId + " not found");
    }

    // Validation errors 4xxx
    public static ApiException validationError(String message) {
        return new ApiException(4001, message);
    }

    public static ApiException invalidData(String message) {
        return new ApiException(4002, message);
    }

    public static ApiException forbidden(String message) {
        return new ApiException(4003, message);
    }

    // Internal errors 5xxx
    public static ApiException internalError(String message) {
        return new ApiException(5001, message);
    }

    public static ApiException databaseError(String message) {
        return new ApiException(5002, "Database error: " + message);
    }
}
