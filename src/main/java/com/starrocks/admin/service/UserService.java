package com.starrocks.admin.service;

import com.starrocks.admin.exception.ApiException;
import com.starrocks.admin.model.dto.request.AdminCreateUserRequest;
import com.starrocks.admin.model.dto.request.AdminUpdateUserRequest;
import com.starrocks.admin.model.dto.response.RoleResponse;
import com.starrocks.admin.model.dto.response.UserResponse;
import com.starrocks.admin.model.dto.response.UserWithRolesResponse;
import com.starrocks.admin.model.entity.User;
import com.starrocks.admin.model.entity.UserRole;
import com.starrocks.admin.model.entity.Role;
import com.starrocks.admin.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final UserOrganizationRepository userOrganizationRepository;
    private final OrganizationRepository organizationRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public List<UserWithRolesResponse> listUsers(Long organizationId, boolean isSuperAdmin) {
        List<User> users;
        if (isSuperAdmin) {
            users = userRepository.findAll();
        } else if (organizationId != null) {
            users = userRepository.findByOrganizationId(organizationId);
            users.addAll(userRepository.findByOrgViaUserOrganization(organizationId));
        } else {
            users = List.of();
        }

        List<UserWithRolesResponse> result = new ArrayList<>();
        for (User user : users) {
            List<RoleResponse> roles = getUserRoles(user.getId());
            boolean isSA = isUserSuperAdmin(user.getId());
            boolean isOA = isUserOrgAdmin(user.getId());
            UserResponse userResp = UserResponse.from(user, isSA, isOA);
            result.add(UserWithRolesResponse.builder().user(userResp).roles(roles).build());
        }
        return result;
    }

    public UserWithRolesResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.userNotFound(userId));
        List<RoleResponse> roles = getUserRoles(userId);
        boolean isSA = isUserSuperAdmin(userId);
        boolean isOA = isUserOrgAdmin(userId);
        UserResponse userResp = UserResponse.from(user, isSA, isOA);
        return UserWithRolesResponse.builder().user(userResp).roles(roles).build();
    }

    @Transactional
    public UserWithRolesResponse createUser(AdminCreateUserRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw ApiException.validationError("Username already exists");
        }

        User user = User.builder()
                .username(req.getUsername())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .email(req.getEmail())
                .avatar(req.getAvatar())
                .organizationId(req.getOrganizationId())
                .build();
        user = userRepository.save(user);

        // Assign roles
        if (req.getRoleIds() != null) {
            for (Long roleId : req.getRoleIds()) {
                assignRoleToUser(user.getId(), roleId);
            }
        }

        List<RoleResponse> roles = getUserRoles(user.getId());
        boolean isSA = isUserSuperAdmin(user.getId());
        boolean isOA = isUserOrgAdmin(user.getId());
        UserResponse userResp = UserResponse.from(user, isSA, isOA);
        return UserWithRolesResponse.builder().user(userResp).roles(roles).build();
    }

    @Transactional
    public UserWithRolesResponse updateUser(Long userId, AdminUpdateUserRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.userNotFound(userId));

        if (req.getEmail() != null) user.setEmail(req.getEmail());
        if (req.getAvatar() != null) user.setAvatar(req.getAvatar());
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }
        if (req.getOrganizationId() != null) {
            user.setOrganizationId(req.getOrganizationId());
        }

        user = userRepository.save(user);

        // Update roles
        if (req.getRoleIds() != null) {
            userRoleRepository.deleteByUserId(userId);
            for (Long roleId : req.getRoleIds()) {
                assignRoleToUser(userId, roleId);
            }
        }

        List<RoleResponse> roles = getUserRoles(userId);
        boolean isSA = isUserSuperAdmin(userId);
        boolean isOA = isUserOrgAdmin(userId);
        UserResponse userResp = UserResponse.from(user, isSA, isOA);
        return UserWithRolesResponse.builder().user(userResp).roles(roles).build();
    }

    @Transactional
    public void deleteUser(Long userId, Long organizationId, boolean isSuperAdmin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.userNotFound(userId));

        // Check organization access
        if (!isSuperAdmin && organizationId != null
                && !organizationId.equals(user.getOrganizationId())) {
            throw ApiException.forbidden("Cannot delete user from another organization");
        }

        userRoleRepository.deleteByUserId(userId);
        userOrganizationRepository.deleteByUserId(userId);
        userRepository.delete(user);
    }

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

    private void assignRoleToUser(Long userId, Long roleId) {
        if (userRoleRepository.findByUserIdAndRoleId(userId, roleId).isEmpty()) {
            UserRole ur = UserRole.builder()
                    .userId(userId)
                    .roleId(roleId)
                    .build();
            userRoleRepository.save(ur);
        }
    }

    private boolean isUserSuperAdmin(Long userId) {
        return roleRepository.findByCode("super_admin")
                .map(role -> userRoleRepository.findByUserIdAndRoleId(userId, role.getId()).isPresent())
                .orElse(false);
    }

    private boolean isUserOrgAdmin(Long userId) {
        return userRoleRepository.findByUserId(userId).stream()
                .map(ur -> roleRepository.findById(ur.getRoleId()))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .anyMatch(role -> role.getCode().startsWith("org_admin_"));
    }
}
