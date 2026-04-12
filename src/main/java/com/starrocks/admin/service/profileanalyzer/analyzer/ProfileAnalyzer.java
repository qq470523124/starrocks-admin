package com.starrocks.admin.service.profileanalyzer.analyzer;

import com.starrocks.admin.service.profileanalyzer.model.ProfileSummary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ProfileAnalyzer {

    public ProfileAnalysisResult analyze(ProfileSummary summary) {
        List<ProfileSummary.DiagnosticIssue> issues = new ArrayList<>();
        List<ProfileSummary.OptimizationSuggestion> suggestions = new ArrayList<>();

        // Rule 1: Scan bottleneck detection
        detectScanBottlenecks(summary, issues, suggestions);

        // Rule 2: Join performance analysis
        analyzeJoinPerformance(summary, issues, suggestions);

        // Rule 3: Data skew detection
        detectDataSkew(summary, issues, suggestions);

        // Rule 4: Memory pressure analysis
        analyzeMemoryPressure(summary, issues, suggestions);

        // Rule 5: Shuffle bottleneck detection
        detectShuffleBottlenecks(summary, issues, suggestions);

        // Rule 6: Overall query performance
        analyzeOverallPerformance(summary, issues, suggestions);

        summary.setIssues(issues);
        summary.setSuggestions(suggestions);

        return new ProfileAnalysisResult(summary, issues, suggestions);
    }

    private void detectScanBottlenecks(ProfileSummary summary,
                                         List<ProfileSummary.DiagnosticIssue> issues,
                                         List<ProfileSummary.OptimizationSuggestion> suggestions) {
        if (summary.getFragments() == null) return;

        for (ProfileSummary.FragmentInfo fragment : summary.getFragments()) {
            if (fragment.getOperators() == null) continue;
            for (ProfileSummary.OperatorInfo op : fragment.getOperators()) {
                if (op.getName() != null && op.getName().contains("OlapScanNode")) {
                    // Check if scan is the bottleneck
                    double scanRatio = summary.getTotalTimeMs() > 0
                            ? (double) op.getTimeMs() / summary.getTotalTimeMs()
                            : 0;

                    if (scanRatio > 0.5) {
                        issues.add(new ProfileSummary.DiagnosticIssue(
                                "warning", "scan",
                                "Scan is the main bottleneck",
                                String.format("OlapScanNode in Fragment %s takes %.1f%% of total time (%d ms)",
                                        fragment.getFragmentId(), scanRatio * 100, op.getTimeMs()),
                                fragment.getFragmentId(), op.getId()
                        ));

                        suggestions.add(new ProfileSummary.OptimizationSuggestion(
                                "high", "Optimize scan performance",
                                "Consider adding appropriate indexes, partition pruning, or column pruning to reduce scan time.",
                                "Potentially reduce query time by " + (int)((scanRatio - 0.3) * 100) + "%"
                        ));
                    }
                }
            }
        }
    }

    private void analyzeJoinPerformance(ProfileSummary summary,
                                         List<ProfileSummary.DiagnosticIssue> issues,
                                         List<ProfileSummary.OptimizationSuggestion> suggestions) {
        if (summary.getFragments() == null) return;

        for (ProfileSummary.FragmentInfo fragment : summary.getFragments()) {
            if (fragment.getOperators() == null) continue;
            for (ProfileSummary.OperatorInfo op : fragment.getOperators()) {
                if (op.getName() != null && op.getName().contains("HashJoinNode")) {
                    if (op.getTimeMs() > summary.getTotalTimeMs() * 0.3) {
                        issues.add(new ProfileSummary.DiagnosticIssue(
                                "warning", "join",
                                "Join is consuming significant time",
                                String.format("HashJoinNode takes %d ms (%.1f%% of total)",
                                        op.getTimeMs(),
                                        summary.getTotalTimeMs() > 0
                                                ? (double) op.getTimeMs() / summary.getTotalTimeMs() * 100 : 0),
                                fragment.getFragmentId(), op.getId()
                        ));

                        suggestions.add(new ProfileSummary.OptimizationSuggestion(
                                "medium", "Optimize join strategy",
                                "Consider broadcast join for small tables, or check join key data distribution.",
                                "May reduce join time significantly"
                        ));
                    }
                }
            }
        }
    }

    private void detectDataSkew(ProfileSummary summary,
                                  List<ProfileSummary.DiagnosticIssue> issues,
                                  List<ProfileSummary.OptimizationSuggestion> suggestions) {
        if (summary.getFragments() == null) return;

        for (ProfileSummary.FragmentInfo fragment : summary.getFragments()) {
            if (fragment.getOperators() == null) continue;
            for (ProfileSummary.OperatorInfo op : fragment.getOperators()) {
                if (op.getName() != null && op.getName().contains("ExchangeNode")) {
                    if (op.getTimeMs() > summary.getTotalTimeMs() * 0.4) {
                        issues.add(new ProfileSummary.DiagnosticIssue(
                                "warning", "skew",
                                "Potential data skew detected",
                                String.format("ExchangeNode in Fragment %s takes %d ms, possible data skew",
                                        fragment.getFragmentId(), op.getTimeMs()),
                                fragment.getFragmentId(), op.getId()
                        ));

                        suggestions.add(new ProfileSummary.OptimizationSuggestion(
                                "high", "Address data skew",
                                "Check data distribution and consider using random distribution or salting keys.",
                                "Can significantly reduce query time if skew is the root cause"
                        ));
                    }
                }
            }
        }
    }

    private void analyzeMemoryPressure(ProfileSummary summary,
                                       List<ProfileSummary.DiagnosticIssue> issues,
                                       List<ProfileSummary.OptimizationSuggestion> suggestions) {
        if (summary.getPeakMemoryBytes() > 2L * 1024 * 1024 * 1024) { // > 2GB
            issues.add(new ProfileSummary.DiagnosticIssue(
                    "warning", "memory",
                    "High memory usage detected",
                    String.format("Peak memory usage: %d MB", summary.getPeakMemoryBytes() / (1024 * 1024)),
                    null, null
            ));

            suggestions.add(new ProfileSummary.OptimizationSuggestion(
                    "medium", "Reduce memory usage",
                    "Consider using spill-to-disk, reducing batch size, or optimizing query to process less data.",
                    "Prevents OOM errors and improves stability"
            ));
        }
    }

    private void detectShuffleBottlenecks(ProfileSummary summary,
                                            List<ProfileSummary.DiagnosticIssue> issues,
                                            List<ProfileSummary.OptimizationSuggestion> suggestions) {
        if (summary.getFragments() == null) return;

        long totalExchangeTime = 0;
        for (ProfileSummary.FragmentInfo fragment : summary.getFragments()) {
            if (fragment.getOperators() == null) continue;
            for (ProfileSummary.OperatorInfo op : fragment.getOperators()) {
                if (op.getName() != null && op.getName().contains("Exchange")) {
                    totalExchangeTime += op.getTimeMs();
                }
            }
        }

        if (summary.getTotalTimeMs() > 0 && totalExchangeTime > summary.getTotalTimeMs() * 0.5) {
            issues.add(new ProfileSummary.DiagnosticIssue(
                    "warning", "shuffle",
                    "Shuffle overhead is significant",
                    String.format("Exchange operators take %d ms (%.1f%% of total)",
                            totalExchangeTime,
                            (double) totalExchangeTime / summary.getTotalTimeMs() * 100),
                    null, null
            ));

            suggestions.add(new ProfileSummary.OptimizationSuggestion(
                    "medium", "Reduce shuffle data",
                    "Consider pre-aggregation, colocation, or partition pruning to reduce shuffle volume.",
                    "Can reduce network overhead and improve parallelism"
            ));
        }
    }

    private void analyzeOverallPerformance(ProfileSummary summary,
                                           List<ProfileSummary.DiagnosticIssue> issues,
                                           List<ProfileSummary.OptimizationSuggestion> suggestions) {
        if (summary.getTotalTimeMs() > 60_000) { // > 60s
            issues.add(new ProfileSummary.DiagnosticIssue(
                    "critical", "performance",
                    "Very slow query",
                    String.format("Total query time: %d ms (%.1f s)",
                            summary.getTotalTimeMs(), summary.getTotalTimeMs() / 1000.0),
                    null, null
            ));
        } else if (summary.getTotalTimeMs() > 10_000) { // > 10s
            issues.add(new ProfileSummary.DiagnosticIssue(
                    "info", "performance",
                    "Slow query detected",
                    String.format("Total query time: %d ms (%.1f s)",
                            summary.getTotalTimeMs(), summary.getTotalTimeMs() / 1000.0),
                    null, null
            ));
        }
    }

    public record ProfileAnalysisResult(
            ProfileSummary summary,
            List<ProfileSummary.DiagnosticIssue> issues,
            List<ProfileSummary.OptimizationSuggestion> suggestions
    ) {}
}
