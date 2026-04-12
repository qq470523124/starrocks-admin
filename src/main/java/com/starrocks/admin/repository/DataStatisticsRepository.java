package com.starrocks.admin.repository;

import com.starrocks.admin.model.entity.DataStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataStatisticsRepository extends JpaRepository<DataStatistics, Long> {

    List<DataStatistics> findByClusterId(Long clusterId);

    @Query("SELECT ds FROM DataStatistics ds WHERE ds.clusterId = :clusterId ORDER BY ds.tableSizeBytes DESC")
    List<DataStatistics> findTopTablesBySize(@Param("clusterId") Long clusterId);

    @Query("SELECT ds FROM DataStatistics ds WHERE ds.clusterId = :clusterId ORDER BY ds.accessCount DESC")
    List<DataStatistics> findTopTablesByAccess(@Param("clusterId") Long clusterId);

    void deleteByClusterId(Long clusterId);
}
