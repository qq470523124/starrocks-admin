package com.starrocks.admin.repository;

import com.starrocks.admin.model.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    List<Permission> findByType(String type);

    List<Permission> findByParentIdIsNull();

    @Query("SELECT p FROM Permission p WHERE p.type = 'menu' ORDER BY p.id")
    List<Permission> findMenuPermissions();

    @Query("SELECT p FROM Permission p WHERE p.type = 'api' ORDER BY p.id")
    List<Permission> findApiPermissions();
}
