package com.starrocks.admin.service;

import com.starrocks.admin.exception.ApiException;
import com.starrocks.admin.model.dto.request.*;
import com.starrocks.admin.model.dto.response.*;
import com.starrocks.admin.model.entity.Cluster;
import com.starrocks.admin.model.entity.Role;
import com.starrocks.admin.model.entity.RolePermission;
import com.starrocks.admin.model.enums.HealthStatus;
import com.starrocks.admin.repository.RoleRepository;
import com.starrocks.admin.repository.RolePermissionRepository;
import com.starrocks.admin.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;

    public List<RoleResponse> listRoles(Long organizationId, boolean isSuperAdmin) {
        List<Role> roles;
        if (isSuperAdmin) {
            roles = roleRepository.findAll();
        } else if (organizationId != null) {
            roles = roleRepository.findByOrganizationId(organizationId);
            roles.addAll(roleRepository.findSystemRoles());
        } else {
            roles = roleRepository.findSystemRoles();
        }
        return roles.stream().map(RoleResponse::from).toList();
    }

    public RoleResponse getRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> ApiException.roleNotFound(roleId));
        return RoleResponse.from(role);
    }

    @Transactional
    public RoleResponse createRole(CreateRoleRequest req, Long organizationId, boolean isSuperAdmin) {
        if (roleRepository.existsByCode(req.getCode())) {
            throw ApiException.validationError("Role code already exists");
        }

        Role role = Role.builder()
                .code(req.getCode())
                .name(req.getName())
                .description(req.getDescription())
                .organizationId(isSuperAdmin ? req.getOrganizationId() : organizationId)
                .isSystem(false)
                .build();
        role = roleRepository.save(role);
        log.info("Role created: {} (ID: {})", role.getCode(), role.getId());
        return RoleResponse.from(role);
    }

    @Transactional
    public RoleResponse updateRole(Long roleId, UpdateRoleRequest req, Long organizationId, boolean isSuperAdmin) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> ApiException.roleNotFound(roleId));

        if (role.getIsSystem()) {
            throw ApiException.forbidden("Cannot modify system role");
        }

        if (req.getName() != null) role.setName(req.getName());
        if (req.getDescription() != null) role.setDescription(req.getDescription());
        if (req.getOrganizationId() != null && isSuperAdmin) role.setOrganizationId(req.getOrganizationId());

        role = roleRepository.save(role);
        log.info("Role updated: {} (ID: {})", role.getCode(), role.getId());
        return RoleResponse.from(role);
    }

    @Transactional
    public void deleteRole(Long roleId, Long organizationId, boolean isSuperAdmin) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> ApiException.roleNotFound(roleId));

        if (role.getIsSystem()) {
            throw ApiException.forbidden("Cannot delete system role");
        }

        rolePermissionRepository.deleteByRoleId(roleId);
        roleRepository.delete(role);
        log.info("Role deleted: {} (ID: {})", role.getCode(), roleId);
    }

    public RoleWithPermissionsResponse getRoleWithPermissions(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> ApiException.roleNotFound(roleId));

        List<Long> permIds = rolePermissionRepository.findPermissionIdsByRoleId(roleId);
        List<PermissionResponse> permissions = new ArrayList<>();
        for (Long permId : permIds) {
            permissionRepository.findById(permId)
                    .map(PermissionResponse::from)
                    .ifPresent(permissions::add);
        }

        return RoleWithPermissionsResponse.builder()
                .role(RoleResponse.from(role))
                .permissions(permissions)
                .build();
    }

    @Transactional
    public void assignPermissionsToRole(Long roleId, UpdateRolePermissionsRequest req,
                                         Long organizationId, boolean isSuperAdmin) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> ApiException.roleNotFound(roleId));

        if (role.getIsSystem()) {
            throw ApiException.forbidden("Cannot modify system role permissions");
        }

        rolePermissionRepository.deleteByRoleId(roleId);
        for (Long permId : req.getPermissionIds()) {
            RolePermission rp = RolePermission.builder()
                    .roleId(roleId)
                    .permissionId(permId)
                    .build();
            rolePermissionRepository.save(rp);
        }
        log.info("Updated permissions for role {}: {} permissions", roleId, req.getPermissionIds().size());
    }
}
