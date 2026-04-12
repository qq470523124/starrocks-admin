package com.starrocks.admin.service.profileanalyzer.parser;

import com.starrocks.admin.service.profileanalyzer.model.ProfileSummary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ProfileParser {

    private static final Pattern QUERY_ID_PATTERN = Pattern.compile("Query ID: ([\\w-]+)");
    private static final Pattern QUERY_TYPE_PATTERN = Pattern.compile("Query Type: (\\w+)");
    private static final Pattern QUERY_STATE_PATTERN = Pattern.compile("Query State: (\\w+)");
    private static final Pattern TOTAL_TIME_PATTERN = Pattern.compile("Total\\s*:\\s*([\\d.]+)\\s*(ms|us|s|ns)");
    private static final Pattern FRAGMENT_PATTERN = Pattern.compile("Fragment\\s+(\\d+)");
    private static final Pattern INSTANCE_PATTERN = Pattern.compile("Instance\\s+([\\w-]+)");
    private static final Pattern OPERATOR_PATTERN = Pattern.compile("\\s+(\\d+):\\s+(\\w+)");
    private static final Pattern TIME_PATTERN = Pattern.compile("([\\d.]+)\\s*(ms|us|s|ns)");
    private static final Pattern MEMORY_PATTERN = Pattern.compile("Memory\\s*:\\s*([\\d.]+)\\s*(KB|MB|GB|TB|B)");
    private static final Pattern ROWS_PATTERN = Pattern.compile("RowsProduced\\s*:\\s*([\\d.]+)");

    public ProfileSummary parse(String profileContent) {
        ProfileSummary.ProfileSummaryBuilder builder = ProfileSummary.builder();

        // Extract query ID
        Matcher queryIdMatcher = QUERY_ID_PATTERN.matcher(profileContent);
        if (queryIdMatcher.find()) {
            builder.queryId(queryIdMatcher.group(1));
        }

        // Extract query type
        Matcher queryTypeMatcher = QUERY_TYPE_PATTERN.matcher(profileContent);
        if (queryTypeMatcher.find()) {
            builder.queryType(queryTypeMatcher.group(1));
        }

        // Extract total time
        builder.totalTimeMs(extractTotalTimeMs(profileContent));

        // Parse fragments
        List<ProfileSummary.FragmentInfo> fragments = parseFragments(profileContent);
        builder.fragments(fragments);
        builder.numFragments(fragments.size());

        // Calculate peak memory
        builder.peakMemoryBytes(calculatePeakMemory(fragments));

        // Count instances
        int totalInstances = fragments.stream()
                .mapToInt(f -> f.getInstances())
                .sum();
        builder.numInstances(totalInstances);

        return builder.build();
    }

    private long extractTotalTimeMs(String content) {
        Matcher matcher = TOTAL_TIME_PATTERN.matcher(content);
        if (matcher.find()) {
            return parseTimeToMs(matcher.group(1), matcher.group(2));
        }
        return 0;
    }

    private List<ProfileSummary.FragmentInfo> parseFragments(String content) {
        List<ProfileSummary.FragmentInfo> fragments = new ArrayList<>();
        String[] sections = content.split("(?=Fragment\\s+\\d+)");

        for (String section : sections) {
            if (!section.trim().startsWith("Fragment")) continue;

            ProfileSummary.FragmentInfo fragment = parseFragment(section);
            if (fragment != null) {
                fragments.add(fragment);
            }
        }

        return fragments;
    }

    private ProfileSummary.FragmentInfo parseFragment(String section) {
        Matcher fragmentMatcher = FRAGMENT_PATTERN.matcher(section);
        if (!fragmentMatcher.find()) return null;

        String fragmentId = fragmentMatcher.group(1);
        long fragmentTimeMs = extractTotalTimeMs(section);
        int instances = countInstances(section);

        List<ProfileSummary.OperatorInfo> operators = parseOperators(section);

        return ProfileSummary.FragmentInfo.builder()
                .fragmentId(fragmentId)
                .timeMs(fragmentTimeMs)
                .instances(instances)
                .operators(operators)
                .build();
    }

    private List<ProfileSummary.OperatorInfo> parseOperators(String section) {
        List<ProfileSummary.OperatorInfo> operators = new ArrayList<>();
        String[] lines = section.split("\n");

        for (String line : lines) {
            Matcher matcher = OPERATOR_PATTERN.matcher(line);
            if (matcher.find()) {
                String id = matcher.group(1);
                String name = matcher.group(2);

                long timeMs = 0;
                Matcher timeMatcher = TIME_PATTERN.matcher(line);
                if (timeMatcher.find()) {
                    timeMs = parseTimeToMs(timeMatcher.group(1), timeMatcher.group(2));
                }

                long memoryBytes = 0;
                Matcher memMatcher = MEMORY_PATTERN.matcher(line);
                if (memMatcher.find()) {
                    memoryBytes = parseMemoryToBytes(memMatcher.group(1), memMatcher.group(2));
                }

                long rowsProduced = 0;
                Matcher rowsMatcher = ROWS_PATTERN.matcher(line);
                if (rowsMatcher.find()) {
                    try { rowsProduced = Long.parseLong(rowsMatcher.group(1).replaceAll("[^0-9]", "")); }
                    catch (NumberFormatException ignored) {}
                }

                operators.add(ProfileSummary.OperatorInfo.builder()
                        .id(id)
                        .name(name)
                        .timeMs(timeMs)
                        .memoryBytes(memoryBytes)
                        .rowsProduced(rowsProduced)
                        .build());
            }
        }

        return operators;
    }

    private int countInstances(String section) {
        Matcher matcher = INSTANCE_PATTERN.matcher(section);
        int count = 0;
        while (matcher.find()) count++;
        return Math.max(count, 1);
    }

    private long calculatePeakMemory(List<ProfileSummary.FragmentInfo> fragments) {
        long peak = 0;
        for (ProfileSummary.FragmentInfo fragment : fragments) {
            for (ProfileSummary.OperatorInfo op : fragment.getOperators()) {
                if (op.getMemoryBytes() > peak) {
                    peak = op.getMemoryBytes();
                }
            }
        }
        return peak;
    }

    static long parseTimeToMs(String value, String unit) {
        double v = Double.parseDouble(value);
        return switch (unit.toLowerCase()) {
            case "ns" -> (long)(v / 1_000_000);
            case "us" -> (long)(v / 1_000);
            case "ms" -> (long)v;
            case "s" -> (long)(v * 1_000);
            default -> (long)v;
        };
    }

    static long parseMemoryToBytes(String value, String unit) {
        double v = Double.parseDouble(value);
        return switch (unit.toUpperCase()) {
            case "B" -> (long)v;
            case "KB" -> (long)(v * 1024);
            case "MB" -> (long)(v * 1024 * 1024);
            case "GB" -> (long)(v * 1024 * 1024 * 1024);
            case "TB" -> (long)(v * 1024L * 1024 * 1024 * 1024);
            default -> (long)v;
        };
    }
}
