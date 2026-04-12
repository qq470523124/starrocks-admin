package com.starrocks.admin.service;

import com.starrocks.admin.exception.ApiException;
import com.starrocks.admin.model.dto.request.AssignUserRoleRequest;
import com.starrocks.admin.model.dto.response.RoleResponse;
import com.starrocks.admin.model.entity.UserRole;
import com.starrocks.admin.repository.UserRoleRepository;
import com.starrocks.admin.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    public List<RoleResponse> getUserRoles(Long userId) {
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        List<RoleResponse> roles = new ArrayList<>();
        for (UserRole ur : userRoles) {
            roleRepository.findById(ur.getRoleId())
                    .map(RoleResponse::from)
                    .ifPresent(roles::add);
        }
        return roles;
    }

    @Transactional
    public void assignRoleToUser(Long userId, AssignUserRoleRequest req) {
        roleRepository.findById(req.getRoleId())
                .orElseThrow(() -> ApiException.roleNotFound(req.getRoleId()));

        if (userRoleRepository.findByUserIdAndRoleId(userId, req.getRoleId()).isEmpty()) {
            UserRole ur = UserRole.builder()
                    .userId(userId)
                    .roleId(req.getRoleId())
                    .build();
            userRoleRepository.save(ur);
            log.info("Assigned role {} to user {}", req.getRoleId(), userId);
        }
    }

    @Transactional
    public void removeRoleFromUser(Long userId, Long roleId) {
        userRoleRepository.findByUserIdAndRoleId(userId, roleId)
                .orElseThrow(() -> ApiException.resourceNotFound("User role assignment not found"));
        userRoleRepository.deleteByUserIdAndRoleId(userId, roleId);
        log.info("Removed role {} from user {}", roleId, userId);
    }
}
