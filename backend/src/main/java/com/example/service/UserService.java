package com.example.service;

import com.example.dto.CreateUserRequest;
import com.example.dto.UpdateUserRequest;
import com.example.entity.User;
import com.example.exception.BusinessException;
import com.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Page<User> getUsers(Pageable pageable, String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    keyword, keyword, pageable);
        }
        return userRepository.findAll(pageable);
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User does not exist"));
    }

    @Transactional
    public User createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username already exists");
        }
        if (request.getEmail() != null && !request.getEmail().isEmpty()
                && userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists");
        }
        if (request.getPhone() != null && !request.getPhone().isEmpty()
                && userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException("Phone already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phone(request.getPhone())
                .avatar(request.getAvatar())
                .status(request.getStatus() != null ? request.getStatus() : 1)
                .role(normalizeRole(request.getRole()))
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User does not exist"));

        if (request.getEmail() != null && !request.getEmail().isEmpty()
                && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            if (!request.getPhone().isEmpty() && userRepository.existsByPhone(request.getPhone())) {
                throw new BusinessException("Phone already exists");
            }
            user.setPhone(request.getPhone());
        }

        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        if (request.getStatus() != null) {
            if ("ADMIN".equals(user.getRole()) && request.getStatus() != 1
                    && userRepository.countByRoleAndStatus("ADMIN", 1) <= 1) {
                throw new BusinessException("Cannot disable the last active admin");
            }
            user.setStatus(request.getStatus());
        }

        if (request.getRole() != null) {
            String newRole = normalizeRole(request.getRole());
            if ("ADMIN".equals(user.getRole()) && !"ADMIN".equals(newRole)
                    && userRepository.countByRoleAndStatus("ADMIN", 1) <= 1) {
                throw new BusinessException("Cannot demote the last active admin");
            }
            user.setRole(newRole);
        }

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(UUID id, String currentUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User does not exist"));
        if (user.getUsername().equals(currentUsername)) {
            throw new BusinessException("Cannot delete current user");
        }
        if ("ADMIN".equals(user.getRole()) && userRepository.countByRoleAndStatus("ADMIN", 1) <= 1) {
            throw new BusinessException("Cannot delete the last active admin");
        }
        userRepository.delete(user);
    }

    @Transactional
    public void updateUserStatus(UUID id, Integer status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User does not exist"));
        if ("ADMIN".equals(user.getRole()) && status != 1 && userRepository.countByRoleAndStatus("ADMIN", 1) <= 1) {
            throw new BusinessException("Cannot disable the last active admin");
        }
        user.setStatus(status);
        userRepository.save(user);
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "USER";
        }
        String normalized = role.trim().toUpperCase();
        if (!normalized.equals("ADMIN") && !normalized.equals("USER")) {
            throw new BusinessException("Unsupported role: " + role);
        }
        return normalized;
    }
}
