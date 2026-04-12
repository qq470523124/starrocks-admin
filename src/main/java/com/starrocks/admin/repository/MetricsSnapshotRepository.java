package com.starrocks.admin.repository;

import com.starrocks.admin.model.entity.MetricsSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface MetricsSnapshotRepository extends JpaRepository<MetricsSnapshot, Long> {

    List<MetricsSnapshot> findByClusterIdOrderByCapturedAtAsc(Long clusterId);

    List<MetricsSnapshot> findByClusterIdAndCapturedAtAfter(Long clusterId, OffsetDateTime since);

    @Query("SELECT m FROM MetricsSnapshot m WHERE m.clusterId = :clusterId AND m.capturedAt >= :since ORDER BY m.capturedAt ASC")
    List<MetricsSnapshot> findRecentByCluster(@Param("clusterId") Long clusterId, @Param("since") OffsetDateTime since);

    @Modifying
    @Query("DELETE FROM MetricsSnapshot m WHERE m.clusterId = :clusterId AND m.capturedAt < :before")
    void deleteOldSnapshots(@Param("clusterId") Long clusterId, @Param("before") OffsetDateTime before);
}
