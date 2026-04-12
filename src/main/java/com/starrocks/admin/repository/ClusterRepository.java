package com.starrocks.admin.repository;

import com.starrocks.admin.model.entity.Cluster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClusterRepository extends JpaRepository<Cluster, Long> {

    Optional<Cluster> findByIsActiveTrue();

    Optional<Cluster> findByIsActiveTrueAndOrganizationId(Long organizationId);

    @Query("SELECT c FROM Cluster c WHERE c.organizationId = :orgId ORDER BY c.createdAt DESC")
    List<Cluster> findByOrganizationId(@Param("orgId") Long orgId);

    @Query("SELECT c FROM Cluster c WHERE c.organizationId IS NULL ORDER BY c.createdAt DESC")
    List<Cluster> findSystemClusters();
}
