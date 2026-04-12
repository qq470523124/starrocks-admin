package com.starrocks.admin.service;

import com.starrocks.admin.exception.ApiException;
import com.starrocks.admin.model.dto.request.*;
import com.starrocks.admin.model.dto.response.*;
import com.starrocks.admin.model.entity.Cluster;
import com.starrocks.admin.model.entity.Organization;
import com.starrocks.admin.model.entity.User;
import com.starrocks.admin.model.entity.UserOrganization;
import com.starrocks.admin.model.enums.HealthStatus;
import com.starrocks.admin.repository.*;
import com.starrocks.admin.client.MySQLPoolManager;
import com.starrocks.admin.client.StarRocksHttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClusterService {

    private final ClusterRepository clusterRepository;
    private final MySQLPoolManager mysqlPoolManager;
    private final StarRocksHttpClient starRocksHttpClient;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public List<ClusterResponse> listClusters(Long organizationId, boolean isSuperAdmin) {
        List<Cluster> clusters;
        if (isSuperAdmin) {
            clusters = clusterRepository.findAll();
        } else if (organizationId != null) {
            clusters = clusterRepository.findByOrganizationId(organizationId);
            clusters.addAll(clusterRepository.findSystemClusters());
        } else {
            clusters = clusterRepository.findSystemClusters();
        }
        return clusters.stream().map(ClusterResponse::from).toList();
    }

    public ClusterResponse getCluster(Long clusterId) {
        Cluster cluster = clusterRepository.findById(clusterId)
                .orElseThrow(() -> ApiException.clusterNotFound(clusterId));
        return ClusterResponse.from(cluster);
    }

    public ClusterResponse getActiveCluster() {
        Cluster cluster = clusterRepository.findByIsActiveTrue()
                .orElseThrow(() -> ApiException.resourceNotFound("No active cluster found"));
        return ClusterResponse.from(cluster);
    }

    public ClusterResponse getActiveClusterByOrg(Long organizationId) {
        Cluster cluster = clusterRepository.findByIsActiveTrueAndOrganizationId(organizationId)
                .orElseThrow(() -> ApiException.resourceNotFound("No active cluster found for organization"));
        return ClusterResponse.from(cluster);
    }

    @Transactional
    public ClusterResponse createCluster(CreateClusterRequest req, Long userId,
                                          Long requestorOrg, boolean isSuperAdmin) {
        req.setName(req.getName().trim());
        req.setFeHost(req.getFeHost().trim());
        req.setUsername(req.getUsername().trim());
        req.setCatalog(req.getCatalog().trim());

        // Deactivate other clusters in the same org
        if (isSuperAdmin || requestorOrg == null) {
            clusterRepository.findAll().forEach(c -> {
                if (c.getIsActive()) {
                    c.setIsActive(false);
                    clusterRepository.save(c);
                }
            });
        } else {
            clusterRepository.findByOrganizationId(requestorOrg).forEach(c -> {
                if (c.getIsActive()) {
                    c.setIsActive(false);
                    clusterRepository.save(c);
                }
            });
        }

        Cluster cluster = Cluster.builder()
                .name(req.getName())
                .description(req.getDescription())
                .feHost(req.getFeHost())
                .feHttpPort(req.getFeHttpPort())
                .feQueryPort(req.getFeQueryPort())
                .username(req.getUsername())
                .passwordEncrypted(req.getPassword())
                .enableSsl(req.getEnableSsl())
                .connectionTimeout(req.getConnectionTimeout())
                .catalog(req.getCatalog())
                .isActive(true)
                .tags(req.getTags())
                .createdBy(userId)
                .organizationId(isSuperAdmin ? null : requestorOrg)
                .deploymentMode(req.getDeploymentMode())
                .build();

        cluster = clusterRepository.save(cluster);
        log.info("Cluster created: {} (ID: {}) by user {}", cluster.getName(), cluster.getId(), userId);
        return ClusterResponse.from(cluster);
    }

    @Transactional
    public ClusterResponse updateCluster(Long clusterId, UpdateClusterRequest req,
                                          Long organizationId, boolean isSuperAdmin) {
        Cluster cluster = clusterRepository.findById(clusterId)
                .orElseThrow(() -> ApiException.clusterNotFound(clusterId));

        if (req.getName() != null) cluster.setName(req.getName().trim());
        if (req.getDescription() != null) cluster.setDescription(req.getDescription());
        if (req.getFeHost() != null) cluster.setFeHost(req.getFeHost().trim());
        if (req.getFeHttpPort() != null) cluster.setFeHttpPort(req.getFeHttpPort());
        if (req.getFeQueryPort() != null) cluster.setFeQueryPort(req.getFeQueryPort());
        if (req.getUsername() != null) cluster.setUsername(req.getUsername().trim());
        if (req.getPassword() != null) cluster.setPasswordEncrypted(req.getPassword());
        if (req.getEnableSsl() != null) cluster.setEnableSsl(req.getEnableSsl());
        if (req.getConnectionTimeout() != null) cluster.setConnectionTimeout(req.getConnectionTimeout());
        if (req.getCatalog() != null) cluster.setCatalog(req.getCatalog().trim());
        if (req.getTags() != null) cluster.setTags(req.getTags());
        if (req.getDeploymentMode() != null) cluster.setDeploymentMode(req.getDeploymentMode());

        cluster = clusterRepository.save(cluster);
        log.info("Cluster updated: {} (ID: {})", cluster.getName(), cluster.getId());
        return ClusterResponse.from(cluster);
    }

    @Transactional
    public void deleteCluster(Long clusterId, Long organizationId, boolean isSuperAdmin) {
        Cluster cluster = clusterRepository.findById(clusterId)
                .orElseThrow(() -> ApiException.clusterNotFound(clusterId));
        clusterRepository.delete(cluster);
        log.info("Cluster deleted: {} (ID: {})", cluster.getName(), clusterId);
    }

    @Transactional
    public ClusterResponse activateCluster(Long clusterId) {
        // Deactivate all
        clusterRepository.findAll().forEach(c -> {
            c.setIsActive(false);
            clusterRepository.save(c);
        });

        Cluster cluster = clusterRepository.findById(clusterId)
                .orElseThrow(() -> ApiException.clusterNotFound(clusterId));
        cluster.setIsActive(true);
        cluster = clusterRepository.save(cluster);
        log.info("Cluster activated: {} (ID: {})", cluster.getName(), clusterId);
        return ClusterResponse.from(cluster);
    }

    public ClusterHealthResponse getClusterHealth(Long clusterId) {
        Cluster cluster = clusterRepository.findById(clusterId)
                .orElseThrow(() -> ApiException.clusterNotFound(clusterId));
        return getClusterHealthForCluster(cluster);
    }

    public ClusterHealthResponse getClusterHealthForCluster(Cluster cluster) {
        List<ClusterHealthResponse.HealthCheck> checks = new ArrayList<>();
        HealthStatus overallStatus = HealthStatus.HEALTHY;

        // HTTP health check
        try {
            starRocksHttpClient.checkHealth(cluster);
            checks.add(new ClusterHealthResponse.HealthCheck("HTTP API", "healthy", "StarRocks FE is reachable"));
        } catch (Exception e) {
            checks.add(new ClusterHealthResponse.HealthCheck("HTTP API", "critical", "HTTP check failed: " + e.getMessage()));
            overallStatus = HealthStatus.CRITICAL;
        }

        // MySQL connection check
        try {
            try (var client = mysqlPoolManager.createClient(cluster)) {
                client.queryRaw("SELECT 1");
                checks.add(new ClusterHealthResponse.HealthCheck("Database Connection", "healthy", "MySQL protocol connection OK"));
            }
        } catch (Exception e) {
            checks.add(new ClusterHealthResponse.HealthCheck("Database Connection", "critical", "Connection failed: " + e.getMessage()));
            overallStatus = HealthStatus.CRITICAL;
        }

        return ClusterHealthResponse.builder()
                .status(overallStatus)
                .checks(checks)
                .lastCheckTime(OffsetDateTime.now())
                .build();
    }

    public ClusterHealthResponse testConnection(HealthCheckRequest req) {
        Cluster tempCluster = Cluster.builder()
                .feHost(req.getFeHost() != null ? req.getFeHost() : "localhost")
                .feHttpPort(req.getFeHttpPort() != null ? req.getFeHttpPort() : 8030)
                .feQueryPort(req.getFeQueryPort() != null ? req.getFeQueryPort() : 9030)
                .username(req.getUsername() != null ? req.getUsername() : "root")
                .passwordEncrypted(req.getPassword() != null ? req.getPassword() : "")
                .enableSsl(req.getEnableSsl())
                .connectionTimeout(10)
                .build();

        return getClusterHealthForCluster(tempCluster);
    }

    public List<Cluster> listAllClusters() {
        return clusterRepository.findAll();
    }

    public Cluster getActiveClusterEntity(Long organizationId, boolean isSuperAdmin) {
        if (isSuperAdmin) {
            return clusterRepository.findByIsActiveTrue()
                    .orElseThrow(() -> ApiException.resourceNotFound("No active cluster found"));
        }
        return clusterRepository.findByIsActiveTrueAndOrganizationId(organizationId)
                .orElseThrow(() -> ApiException.resourceNotFound("No active cluster found for organization"));
    }

    public ClusterResponse getActiveCluster(Long organizationId, boolean isSuperAdmin) {
        return ClusterResponse.from(getActiveClusterEntity(organizationId, isSuperAdmin));
    }

    @Transactional
    public ClusterResponse activateCluster(Long clusterId, Long organizationId, boolean isSuperAdmin) {
        return activateCluster(clusterId);
    }
}
