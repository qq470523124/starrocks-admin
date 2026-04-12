package com.starrocks.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Health", description = "Health check endpoints")
@RestController
public class HealthController {

    @Operation(summary = "Health check")
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "healthy");
    }

    @Operation(summary = "Readiness check")
    @GetMapping("/ready")
    public Map<String, String> ready() {
        return Map.of("status", "ready");
    }
}
