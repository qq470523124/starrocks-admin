package com.starrocks.admin.repository;

import com.starrocks.admin.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByCode(String code);

    boolean existsByCode(String code);

    @Query("SELECT r FROM Role r WHERE r.organizationId = :orgId ORDER BY r.createdAt DESC")
    List<Role> findByOrganizationId(@Param("orgId") Long orgId);

    @Query("SELECT r FROM Role r WHERE r.organizationId IS NULL ORDER BY r.createdAt DESC")
    List<Role> findSystemRoles();
}
