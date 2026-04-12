package com.starrocks.admin.service.profileanalyzer.model;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileSummary {
    private String queryId;
    private long totalTimeMs;
    private String queryType;
    private long peakMemoryBytes;
    private int numFragments;
    private int numInstances;
    private List<FragmentInfo> fragments;
    private List<DiagnosticIssue> issues;
    private List<OptimizationSuggestion> suggestions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FragmentInfo {
        private String fragmentId;
        private String planType;
        private long timeMs;
        private long cpuTimeNs;
        private long memoryBytes;
        private int instances;
        private List<OperatorInfo> operators;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperatorInfo {
        private String id;
        private String name;
        private String type;
        private long timeMs;
        private long cpuTimeNs;
        private long memoryBytes;
        private long rowsProduced;
        private long rowsReturned;
        private Map<String, String> details;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiagnosticIssue {
        private String severity; // "info", "warning", "critical"
        private String category; // "scan", "join", "shuffle", "memory", "skew"
        private String title;
        private String description;
        private String fragmentId;
        private String operatorId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptimizationSuggestion {
        private String priority; // "high", "medium", "low"
        private String title;
        private String description;
        private String expectedImpact;
    }
}
