package com.toll.repository;

import com.toll.models.Admin;
import java.util.*;

public class AdminRepository {
    private final Map<String, Admin> adminMap = new LinkedHashMap<>();

    public Admin save(Admin admin) {
        if (admin != null) {
            adminMap.put(admin.getUsername().toLowerCase(), admin);
        }
        return admin;
    }

    public Optional<Admin> findByUsername(String username) {
        if (username == null) return Optional.empty();
        return Optional.ofNullable(adminMap.get(username.toLowerCase().trim()));
    }

    public Optional<Admin> findById(String id) {
        return adminMap.values().stream()
                .filter(a -> a.getId().equalsIgnoreCase(id))
                .findFirst();
    }

    public List<Admin> findAll() {
        return new ArrayList<>(adminMap.values());
    }

    public boolean existsByUsername(String username) {
        if (username == null) return false;
        return adminMap.containsKey(username.toLowerCase().trim());
    }
}
