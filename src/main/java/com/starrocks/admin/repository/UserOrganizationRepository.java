package com.starrocks.admin.repository;

import com.starrocks.admin.model.entity.UserOrganization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserOrganizationRepository extends JpaRepository<UserOrganization, Long> {

    Optional<UserOrganization> findByUserId(Long userId);

    Optional<UserOrganization> findByOrganizationId(Long organizationId);

    @Query("SELECT uo.userId FROM UserOrganization uo WHERE uo.organizationId = :orgId")
    List<Long> findUserIdsByOrganizationId(@Param("orgId") Long orgId);

    @Query("SELECT uo.organizationId FROM UserOrganization uo WHERE uo.userId = :userId")
    Optional<Long> findOrganizationIdByUserId(@Param("userId") Long userId);

    void deleteByUserId(Long userId);

    void deleteByOrganizationId(Long organizationId);
}
