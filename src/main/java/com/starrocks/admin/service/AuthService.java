package com.starrocks.admin.service;

import com.starrocks.admin.exception.ApiException;
import com.starrocks.admin.model.dto.request.*;
import com.starrocks.admin.model.dto.response.*;
import com.starrocks.admin.model.entity.User;
import com.starrocks.admin.model.entity.UserRole;
import com.starrocks.admin.repository.UserRepository;
import com.starrocks.admin.repository.UserRoleRepository;
import com.starrocks.admin.repository.RoleRepository;
import com.starrocks.admin.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public User register(RegisterRequest req) {
        log.debug("Checking if username exists: {}", req.getUsername());

        if (userRepository.existsByUsername(req.getUsername())) {
            log.warn("Registration failed: username '{}' already exists", req.getUsername());
            throw ApiException.validationError("Username already exists");
        }

        log.debug("Hashing password for new user: {}", req.getUsername());
        String hashedPassword = passwordEncoder.encode(req.getPassword());

        User user = User.builder()
                .username(req.getUsername())
                .passwordHash(hashedPassword)
                .email(req.getEmail())
                .avatar(req.getAvatar())
                .build();

        user = userRepository.save(user);
        log.info("User registered successfully: {} (ID: {})", user.getUsername(), user.getId());
        return user;
    }

    public LoginResponse login(LoginRequest req) {
        log.info("Login attempt for username: {}", req.getUsername());

        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> ApiException.invalidCredentials());

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            log.warn("Login failed: invalid password for user {}", req.getUsername());
            throw ApiException.invalidCredentials();
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        boolean isSuperAdmin = isUserSuperAdmin(user.getId());
        boolean isOrgAdmin = isUserOrgAdmin(user.getId());
        UserResponse userResponse = UserResponse.from(user, isSuperAdmin, isOrgAdmin);

        log.info("User logged in successfully: {} (ID: {})", user.getUsername(), user.getId());
        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationMillis() / 1000)
                .user(userResponse)
                .build();
    }

    @Transactional
    public User updateUser(Long userId, UpdateUserRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.userNotFound(userId));

        if (req.getEmail() != null) {
            user.setEmail(req.getEmail());
        }
        if (req.getAvatar() != null) {
            user.setAvatar(req.getAvatar());
        }
        if (req.getNewPassword() != null && !req.getNewPassword().isBlank()) {
            if (req.getCurrentPassword() == null || !passwordEncoder.matches(req.getCurrentPassword(), user.getPasswordHash())) {
                throw ApiException.validationError("Current password is incorrect");
            }
            user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        }

        return userRepository.save(user);
    }

    public UserResponse toUserResponse(User user) {
        boolean isSuperAdmin = isUserSuperAdmin(user.getId());
        boolean isOrgAdmin = isUserOrgAdmin(user.getId());
        return UserResponse.from(user, isSuperAdmin, isOrgAdmin);
    }

    public boolean isUserSuperAdmin(Long userId) {
        return roleRepository.findByCode("super_admin")
                .map(role -> userRoleRepository.findByUserIdAndRoleId(userId, role.getId()).isPresent())
                .orElse(false);
    }

    public boolean isUserOrgAdmin(Long userId) {
        for (UserRole ur : userRoleRepository.findByUserId(userId)) {
            roleRepository.findById(ur.getRoleId()).ifPresent(role -> {
                // Just check if code starts with org_admin_
            });
        }
        return userRoleRepository.findByUserId(userId).stream()
                .map(ur -> roleRepository.findById(ur.getRoleId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .anyMatch(role -> role.getCode().startsWith("org_admin_"));
    }
}
