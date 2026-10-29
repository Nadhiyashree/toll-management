package com.toll.services;

import com.toll.models.Admin;
import com.toll.repository.AdminRepository;

import java.util.Optional;

public class AdminService {
    private final AdminRepository adminRepository;
    private Admin currentUser;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
        this.adminRepository.save(new Admin("ADM-1", "admin", "admin123", "System Administrator", Admin.UserRole.ADMIN));
    }

    public boolean login(String username, String password) {
        if (username == null || password == null) return false;
        Optional<Admin> adminOpt = adminRepository.findByUsername(username.trim());
        if (adminOpt.isPresent() && adminOpt.get().getPassword().equals(password.trim())) {
            this.currentUser = adminOpt.get();
            return true;
        }
        return false;
    }

    public void logout() {
        this.currentUser = null;
    }

    public Admin getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == Admin.UserRole.ADMIN;
    }

    public void requireAdmin() {
        if (!isAdmin()) {
            throw new InvalidOperationException("Access Denied: This operation requires an active Administrator session. Please login as Admin.");
        }
    }

    public Admin createAdmin(String username, String password, String fullName, Admin.UserRole role) {
        requireAdmin();
        if (username == null || username.trim().isEmpty()) {
            throw new InvalidOperationException("Username cannot be empty.");
        }
        if (password == null || password.trim().length() < 4) {
            throw new InvalidOperationException("Password must be at least 4 characters.");
        }
        if (adminRepository.existsByUsername(username.trim())) {
            throw new InvalidOperationException("User with username '" + username + "' already exists.");
        }
        Admin admin = new Admin("ADM-" + (adminRepository.findAll().size() + 1), username.trim(), password.trim(), fullName.trim(), role);
        return adminRepository.save(admin);
    }
}
