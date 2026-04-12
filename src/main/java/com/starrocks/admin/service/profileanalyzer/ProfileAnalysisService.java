package com.starrocks.admin.service.profileanalyzer;

import com.starrocks.admin.service.profileanalyzer.analyzer.ProfileAnalyzer;
import com.starrocks.admin.service.profileanalyzer.model.ProfileSummary;
import com.starrocks.admin.service.profileanalyzer.parser.ProfileParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileAnalysisService {

    private final ProfileParser profileParser;
    private final ProfileAnalyzer profileAnalyzer;

    public ProfileAnalysisResult analyzeProfile(String profileContent, Map<String, String> clusterVariables) {
        log.info("Starting profile analysis...");

        // Step 1: Parse profile
        ProfileSummary summary = profileParser.parse(profileContent);
        log.info("Profile parsed: queryId={}, totalTime={}ms, fragments={}",
                summary.getQueryId(), summary.getTotalTimeMs(), summary.getNumFragments());

        // Step 2: Analyze profile
        ProfileAnalyzer.ProfileAnalysisResult result = profileAnalyzer.analyze(summary);
        log.info("Analysis complete: {} issues, {} suggestions",
                result.issues().size(), result.suggestions().size());

        return new ProfileAnalysisResult(summary, result.issues(), result.suggestions());
    }

    public record ProfileAnalysisResult(
            ProfileSummary summary,
            java.util.List<ProfileSummary.DiagnosticIssue> issues,
            java.util.List<ProfileSummary.OptimizationSuggestion> suggestions
    ) {}
}
