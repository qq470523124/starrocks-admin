package com.starrocks.admin.client;

import com.starrocks.admin.exception.ApiException;
import com.starrocks.admin.model.entity.Cluster;
import com.starrocks.admin.model.dto.response.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Slf4j
@Component
public class StarRocksHttpClient {

    private final ObjectMapper objectMapper;

    public StarRocksHttpClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private HttpClient buildClient(int timeoutSeconds) {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
    }

    private String buildBaseUrl(Cluster cluster) {
        String protocol = Boolean.TRUE.equals(cluster.getEnableSsl()) ? "https" : "http";
        return protocol + "://" + cluster.getFeHost() + ":" + cluster.getFeHttpPort();
    }

    private HttpRequest buildRequest(Cluster cluster, String path) {
        String baseUrl = buildBaseUrl(cluster);
        String url = baseUrl + "/api/" + path;

        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", basicAuth(cluster.getUsername(), cluster.getPasswordEncrypted()))
                .timeout(Duration.ofSeconds(cluster.getConnectionTimeout()))
                .GET()
                .build();
    }

    private String basicAuth(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

    public List<BackendResponse> getBackends(Cluster cluster) {
        try {
            HttpRequest request = buildRequest(cluster, "v2/backends");
            HttpResponse<String> response = buildClient(cluster.getConnectionTimeout())
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw ApiException.clusterConnectionFailed("HTTP " + response.statusCode());
            }

            Map<String, Object> body = objectMapper.readValue(response.body(), new TypeReference<>() {});
            List<Map<String, Object>> rows = (List<Map<String, Object>>) body.get("rows");
            if (rows == null) return List.of();

            List<BackendResponse> backends = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                backends.add(objectMapper.convertValue(row, BackendResponse.class));
            }
            return backends;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get backends: {}", e.getMessage());
            throw ApiException.clusterConnectionFailed(e.getMessage());
        }
    }

    public List<FrontendResponse> getFrontends(Cluster cluster) {
        try {
            HttpRequest request = buildRequest(cluster, "v2/frontends");
            HttpResponse<String> response = buildClient(cluster.getConnectionTimeout())
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw ApiException.clusterConnectionFailed("HTTP " + response.statusCode());
            }

            Map<String, Object> body = objectMapper.readValue(response.body(), new TypeReference<>() {});
            List<Map<String, Object>> rows = (List<Map<String, Object>>) body.get("rows");
            if (rows == null) return List.of();

            List<FrontendResponse> frontends = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                frontends.add(objectMapper.convertValue(row, FrontendResponse.class));
            }
            return frontends;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get frontends: {}", e.getMessage());
            throw ApiException.clusterConnectionFailed(e.getMessage());
        }
    }

    public void dropBackend(Cluster cluster, String host, String port) {
        try {
            String baseUrl = buildBaseUrl(cluster);
            String url = baseUrl + "/api/v2/backends/drop?host=" + host + "&port=" + port;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", basicAuth(cluster.getUsername(), cluster.getPasswordEncrypted()))
                    .timeout(Duration.ofSeconds(cluster.getConnectionTimeout()))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = buildClient(cluster.getConnectionTimeout())
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw ApiException.clusterConnectionFailed("Failed to drop backend: HTTP " + response.statusCode());
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to drop backend: {}", e.getMessage());
            throw ApiException.clusterConnectionFailed(e.getMessage());
        }
    }

    public String getMetrics(Cluster cluster) {
        try {
            String baseUrl = buildBaseUrl(cluster);
            String url = baseUrl + "/metrics";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", basicAuth(cluster.getUsername(), cluster.getPasswordEncrypted()))
                    .timeout(Duration.ofSeconds(cluster.getConnectionTimeout()))
                    .GET()
                    .build();

            HttpResponse<String> response = buildClient(cluster.getConnectionTimeout())
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw ApiException.clusterConnectionFailed("Failed to get metrics: HTTP " + response.statusCode());
            }

            return response.body();
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get metrics: {}", e.getMessage());
            throw ApiException.clusterConnectionFailed(e.getMessage());
        }
    }

    public Map<String, Double> parsePrometheusMetrics(String metricsText) {
        Map<String, Double> metrics = new HashMap<>();
        for (String line : metricsText.split("\n")) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            int lastSpace = line.lastIndexOf(' ');
            if (lastSpace > 0) {
                String namePart = line.substring(0, lastSpace);
                String valueStr = line.substring(lastSpace + 1).trim();
                try {
                    double value = Double.parseDouble(valueStr);
                    String metricName = namePart.contains("{") ? namePart.substring(0, namePart.indexOf('{')) : namePart;
                    metrics.put(metricName, value);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return metrics;
    }

    public Map<String, Double> getPrometheusMetrics(Cluster cluster) {
        String metricsText = getMetrics(cluster);
        return parsePrometheusMetrics(metricsText);
    }

    public void checkHealth(Cluster cluster) {
        String baseUrl = buildBaseUrl(cluster);
        String url = baseUrl + "/api/health";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", basicAuth(cluster.getUsername(), cluster.getPasswordEncrypted()))
                    .timeout(Duration.ofSeconds(cluster.getConnectionTimeout()))
                    .GET()
                    .build();

            HttpResponse<String> response = buildClient(cluster.getConnectionTimeout())
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw ApiException.clusterConnectionFailed("Health check failed: HTTP " + response.statusCode());
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw ApiException.clusterConnectionFailed("Health check failed: " + e.getMessage());
        }
    }

    public RuntimeInfoResponse getRuntimeInfo(Cluster cluster) {
        try {
            String baseUrl = buildBaseUrl(cluster);
            String url = baseUrl + "/api/runtime_info";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", basicAuth(cluster.getUsername(), cluster.getPasswordEncrypted()))
                    .timeout(Duration.ofSeconds(cluster.getConnectionTimeout()))
                    .GET()
                    .build();

            HttpResponse<String> response = buildClient(cluster.getConnectionTimeout())
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw ApiException.clusterConnectionFailed("Failed to get runtime info: HTTP " + response.statusCode());
            }

            return objectMapper.readValue(response.body(), RuntimeInfoResponse.class);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get runtime info: {}", e.getMessage());
            throw ApiException.clusterConnectionFailed(e.getMessage());
        }
    }
}
