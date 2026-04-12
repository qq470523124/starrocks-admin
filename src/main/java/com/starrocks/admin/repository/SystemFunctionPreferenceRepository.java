package com.starrocks.admin.repository;

import com.starrocks.admin.model.entity.SystemFunctionPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemFunctionPreferenceRepository extends JpaRepository<SystemFunctionPreference, Long> {

    List<SystemFunctionPreference> findByClusterId(Long clusterId);

    Optional<SystemFunctionPreference> findByClusterIdAndFunctionId(Long clusterId, Long functionId);

    void deleteByClusterId(Long clusterId);
}
