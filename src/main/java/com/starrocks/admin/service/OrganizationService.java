package com.starrocks.admin.service;

import com.starrocks.admin.exception.ApiException;
import com.starrocks.admin.model.dto.request.*;
import com.starrocks.admin.model.dto.response.*;
import com.starrocks.admin.model.entity.Organization;
import com.starrocks.admin.model.entity.User;
import com.starrocks.admin.model.entity.UserOrganization;
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
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final UserOrganizationRepository userOrganizationRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public List<OrganizationResponse> listOrganizations(Long organizationId, boolean isSuperAdmin) {
        List<Organization> orgs;
        if (isSuperAdmin) {
            orgs = organizationRepository.findAllOrdered();
        } else {
            orgs = organizationId != null
                    ? organizationRepository.findById(organizationId).map(List::of).orElse(List.of())
                    : List.of();
        }

        List<OrganizationResponse> responses = new ArrayList<>();
        for (Organization org : orgs) {
            Long adminUserId = findOrgAdminUserId(org.getId());
            responses.add(OrganizationResponse.builder()
                    .id(org.getId())
                    .code(org.getCode())
                    .name(org.getName())
                    .description(org.getDescription())
                    .isSystem(org.getIsSystem())
                    .adminUserId(adminUserId)
                    .createdAt(org.getCreatedAt())
                    .build());
        }
        return responses;
    }

    public OrganizationResponse getOrganization(Long orgId) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> ApiException.organizationNotFound(orgId));
        Long adminUserId = findOrgAdminUserId(org.getId());
        return OrganizationResponse.builder()
                .id(org.getId())
                .code(org.getCode())
                .name(org.getName())
                .description(org.getDescription())
                .isSystem(org.getIsSystem())
                .adminUserId(adminUserId)
                .createdAt(org.getCreatedAt())
                .build();
    }

    @Transactional
    public OrganizationResponse createOrganization(CreateOrganizationRequest req) {
        if (organizationRepository.existsByCode(req.getCode())) {
            throw ApiException.validationError("Organization code already exists");
        }

        Organization org = Organization.builder()
                .code(req.getCode())
                .name(req.getName())
                .description(req.getDescription())
                .isSystem(false)
                .build();
        org = organizationRepository.save(org);

        // Create org_admin role
        String orgAdminCode = "org_admin_" + org.getCode();
        if (!roleRepository.existsByCode(orgAdminCode)) {
            Role orgAdminRole = Role.builder()
                    .code(orgAdminCode)
                    .name(org.getName() + " Administrator")
                    .description("Organization administrator for " + org.getName())
                    .organizationId(org.getId())
                    .isSystem(true)
                    .build();
            roleRepository.save(orgAdminRole);
        }

        // Create or assign admin user
        if (req.getAdminUserId() != null) {
            assignOrgAdmin(org.getId(), req.getAdminUserId());
        } else if (req.getAdminUsername() != null && !req.getAdminUsername().isBlank()) {
            User admin = User.builder()
                    .username(req.getAdminUsername())
                    .passwordHash(passwordEncoder.encode(req.getAdminPassword()))
                    .email(req.getAdminEmail())
                    .build();
            admin = userRepository.save(admin);
            assignOrgAdmin(org.getId(), admin.getId());
        }

        log.info("Organization created: {} (ID: {})", org.getCode(), org.getId());
        return getOrganization(org.getId());
    }

    @Transactional
    public OrganizationResponse updateOrganization(Long orgId, UpdateOrganizationRequest req,
                                                    Long organizationId, boolean isSuperAdmin) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> ApiException.organizationNotFound(orgId));

        if (req.getName() != null) org.setName(req.getName());
        if (req.getDescription() != null) org.setDescription(req.getDescription());

        if (req.getAdminUserId() != null) {
            assignOrgAdmin(org.getId(), req.getAdminUserId());
        }

        org = organizationRepository.save(org);
        log.info("Organization updated: {} (ID: {})", org.getCode(), org.getId());
        return getOrganization(org.getId());
    }

    @Transactional
    public void deleteOrganization(Long orgId, Long organizationId, boolean isSuperAdmin) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> ApiException.organizationNotFound(orgId));

        // Remove all users from org
        userOrganizationRepository.deleteByOrganizationId(orgId);

        // Delete org_admin role
        String orgAdminCode = "org_admin_" + org.getCode();
        roleRepository.findByCode(orgAdminCode).ifPresent(role -> {
            userRoleRepository.deleteByRoleId(role.getId());
            roleRepository.delete(role);
        });

        organizationRepository.delete(org);
        log.warn("Organization deleted: {} (ID: {})", org.getCode(), orgId);
    }

    private Long findOrgAdminUserId(Long orgId) {
        String orgAdminCode = "org_admin_" + organizationRepository.findById(orgId)
                .map(Organization::getCode).orElse("");
        return roleRepository.findByCode(orgAdminCode)
                .map(role -> userRoleRepository.findByUserIdAndRoleId(
                        userOrganizationRepository.findUserIdsByOrganizationId(orgId).stream().findFirst().orElse(null),
                        role.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(UserRole::getUserId)
                .orElse(null);
    }

    private void assignOrgAdmin(Long orgId, Long userId) {
        userOrganizationRepository.deleteByUserId(userId);
        UserOrganization uo = UserOrganization.builder()
                .userId(userId)
                .organizationId(orgId)
                .build();
        userOrganizationRepository.save(uo);

        String orgAdminCode = "org_admin_" + organizationRepository.findById(orgId)
                .map(Organization::getCode).orElse("");
        roleRepository.findByCode(orgAdminCode).ifPresent(role -> {
            if (userRoleRepository.findByUserIdAndRoleId(userId, role.getId()).isEmpty()) {
                UserRole ur = UserRole.builder().userId(userId).roleId(role.getId()).build();
                userRoleRepository.save(ur);
            }
        });
    }
}
