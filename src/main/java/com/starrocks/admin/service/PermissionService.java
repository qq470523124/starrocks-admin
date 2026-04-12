package com.starrocks.admin.service;

import com.starrocks.admin.model.dto.response.PermissionResponse;
import com.starrocks.admin.model.dto.response.PermissionTreeResponse;
import com.starrocks.admin.model.entity.Permission;
import com.starrocks.admin.model.entity.RolePermission;
import com.starrocks.admin.model.entity.UserRole;
import com.starrocks.admin.repository.PermissionRepository;
import com.starrocks.admin.repository.RolePermissionRepository;
import com.starrocks.admin.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public List<PermissionResponse> listPermissions() {
        return permissionRepository.findAll().stream()
                .map(PermissionResponse::from)
                .toList();
    }

    public List<PermissionResponse> listMenuPermissions() {
        return permissionRepository.findMenuPermissions().stream()
                .map(PermissionResponse::from)
                .toList();
    }

    public List<PermissionResponse> listApiPermissions() {
        return permissionRepository.findApiPermissions().stream()
                .map(PermissionResponse::from)
                .toList();
    }

    public List<PermissionTreeResponse> getPermissionTree() {
        List<Permission> allPermissions = permissionRepository.findAll();
        Map<Long, List<Permission>> childrenMap = allPermissions.stream()
                .filter(p -> p.getParentId() != null)
                .collect(Collectors.groupingBy(Permission::getParentId));

        return allPermissions.stream()
                .filter(p -> p.getParentId() == null)
                .map(p -> buildTree(p, childrenMap))
                .toList();
    }

    public List<PermissionResponse> getUserPermissions(Long userId) {
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        Set<Long> permissionIds = new HashSet<>();

        for (UserRole ur : userRoles) {
            List<Long> permIds = rolePermissionRepository.findPermissionIdsByRoleId(ur.getRoleId());
            permissionIds.addAll(permIds);
        }

        return permissionIds.stream()
                .map(permissionRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(PermissionResponse::from)
                .toList();
    }

    private PermissionTreeResponse buildTree(Permission p, Map<Long, List<Permission>> childrenMap) {
        List<PermissionTreeResponse> children = childrenMap.getOrDefault(p.getId(), List.of())
                .stream()
                .map(child -> buildTree(child, childrenMap))
                .toList();

        return PermissionTreeResponse.builder()
                .id(p.getId())
                .code(p.getCode())
                .name(p.getName())
                .type(p.getType())
                .resource(p.getResource())
                .action(p.getAction())
                .description(p.getDescription())
                .children(children)
                .build();
    }
}
