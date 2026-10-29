package com.toll.repository;

import com.toll.models.FASTagAccount;
import java.util.*;

public class FASTagAccountRepository {
    private final Map<String, FASTagAccount> tagMap = new LinkedHashMap<>();

    public FASTagAccount save(FASTagAccount account) {
        if (account != null) {
            tagMap.put(account.getTagId().toUpperCase().trim(), account);
        }
        return account;
    }

    public Optional<FASTagAccount> findByTagId(String tagId) {
        if (tagId == null) return Optional.empty();
        return Optional.ofNullable(tagMap.get(tagId.toUpperCase().trim()));
    }

    public Optional<FASTagAccount> findByVehicleRegistrationNumber(String regNo) {
        if (regNo == null) return Optional.empty();
        return tagMap.values().stream()
                .filter(a -> a.getVehicleRegistrationNumber().equalsIgnoreCase(regNo.trim()))
                .findFirst();
    }

    public List<FASTagAccount> findAll() {
        return new ArrayList<>(tagMap.values());
    }

    public boolean existsByTagId(String tagId) {
        if (tagId == null) return false;
        return tagMap.containsKey(tagId.toUpperCase().trim());
    }
}
