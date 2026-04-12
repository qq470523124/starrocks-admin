package com.starrocks.admin.security;

import com.starrocks.admin.exception.ApiException;
import com.starrocks.admin.model.entity.Permission;
import com.starrocks.admin.model.entity.Role;
import com.starrocks.admin.model.entity.RolePermission;
import com.starrocks.admin.model.entity.UserRole;
import com.starrocks.admin.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.model.Model;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CasbinService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserOrganizationRepository userOrganizationRepository;

    private Enforcer enforcer;

    @PostConstruct
    public void init() {
        String modelText = """
                [request_definition]
                r = sub, obj, act
                
                [policy_definition]
                p = sub, obj, act
                
                [role_definition]
                g = _, _
                
                [policy_effect]
                e = some(where (p.eft == allow))
                
                [matchers]
                m = g(r.sub, p.sub) && r.obj == p.obj && r.act == p.act
                """;
        try {
            Model casbinModel = new Model();
            casbinModel.loadModelFromText(modelText);
            enforcer = new Enforcer(casbinModel);
            reloadPolicies();
            log.info("Casbin enforcer initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Casbin enforcer: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize Casbin", e);
        }
    }

    public synchronized void reloadPolicies() {
        try {
            enforcer.clearPolicy();

            // Load role-permission policies
            List<RolePermission> rolePerms = rolePermissionRepository.findAll();
            for (RolePermission rp : rolePerms) {
                Permission perm = permissionRepository.findById(rp.getPermissionId()).orElse(null);
                if (perm != null) {
                    String resourceKey = formatResourceKeyForRole(rp.getRoleId(), perm.getCode());
                    enforcer.addPolicy("r:" + rp.getRoleId(), resourceKey, perm.getAction() != null ? perm.getAction() : "access");
                }
            }

            // Load user-role assignments
            List<UserRole> userRoles = userRoleRepository.findAll();
            for (UserRole ur : userRoles) {
                enforcer.addGroupingPolicy("u:" + ur.getUserId(), "r:" + ur.getRoleId());
            }

            log.info("Policies reloaded from database successfully");
        } catch (Exception e) {
            log.error("Failed to reload policies: {}", e.getMessage());
        }
    }

    public boolean enforce(Long userId, String resourceScope, String action) {
        try {
            return enforcer.enforce("u:" + userId, resourceScope, action);
        } catch (Exception e) {
            log.error("Casbin enforce error: {}", e.getMessage());
            return false;
        }
    }

    public boolean isSuperAdmin(Long userId) {
        return roleRepository.findByCode("admin")
                .map(role -> userRoleRepository.findByUserIdAndRoleId(userId, role.getId()).isPresent())
                .orElse(false);
    }

    public boolean isOrgAdmin(Long userId) {
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        for (UserRole ur : userRoles) {
            Role role = roleRepository.findById(ur.getRoleId()).orElse(null);
            if (role != null && role.getCode().startsWith("org_admin_")) {
                return true;
            }
        }
        return false;
    }

    public Long getOrganizationId(Long userId) {
        // First check users.organization_id
        // Then check user_organizations table
        return userOrganizationRepository.findOrganizationIdByUserId(userId).orElse(null);
    }

    public static String formatResourceKey(Long orgId, String resource) {
        if (orgId != null) {
            return "org:" + orgId + ":" + resource;
        }
        return "system:" + resource;
    }

    private String formatResourceKeyForRole(Long roleId, String permissionCode) {
        Role role = roleRepository.findById(roleId).orElse(null);
        if (role != null && role.getOrganizationId() != null) {
            return "org:" + role.getOrganizationId() + ":" + permissionCode;
        }
        return "system:" + permissionCode;
    }
}
