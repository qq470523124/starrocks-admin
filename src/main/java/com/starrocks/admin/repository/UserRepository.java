package com.starrocks.admin.repository;

import com.starrocks.admin.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.organizationId = :orgId")
    List<User> findByOrganizationId(@Param("orgId") Long orgId);

    @Query("SELECT u FROM User u WHERE u.organizationId IS NULL AND u.id IN " +
           "(SELECT uo.userId FROM UserOrganization uo WHERE uo.organizationId = :orgId)")
    List<User> findByOrgViaUserOrganization(@Param("orgId") Long orgId);
}
