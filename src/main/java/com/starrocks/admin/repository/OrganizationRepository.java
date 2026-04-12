package com.starrocks.admin.repository;

import com.starrocks.admin.model.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    Optional<Organization> findByCode(String code);

    boolean existsByCode(String code);

    @Query("SELECT o FROM Organization o ORDER BY o.createdAt DESC")
    List<Organization> findAllOrdered();
}
