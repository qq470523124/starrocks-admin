package com.starrocks.admin.repository;

import com.starrocks.admin.model.entity.SystemFunction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemFunctionRepository extends JpaRepository<SystemFunction, Long> {

    List<SystemFunction> findByClusterIdOrderByCategoryOrderAscDisplayOrderAsc(Long clusterId);

    List<SystemFunction> findByClusterIdAndCategoryNameOrderByDisplayOrderAsc(Long clusterId, String categoryName);

    @Query("SELECT sf FROM SystemFunction sf WHERE sf.clusterId = :clusterId AND sf.isSystem = true ORDER BY sf.categoryOrder ASC, sf.displayOrder ASC")
    List<SystemFunction> findSystemFunctionsByClusterId(@Param("clusterId") Long clusterId);

    @Query("SELECT sf FROM SystemFunction sf WHERE sf.clusterId = :clusterId AND sf.isSystem = false ORDER BY sf.categoryOrder ASC, sf.displayOrder ASC")
    List<SystemFunction> findCustomFunctionsByClusterId(@Param("clusterId") Long clusterId);

    void deleteByClusterIdAndFunctionName(Long clusterId, String functionName);

    void deleteByClusterIdAndCategoryName(Long clusterId, String categoryName);
}
