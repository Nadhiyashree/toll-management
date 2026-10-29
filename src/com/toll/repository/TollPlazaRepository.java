package com.toll.repository;

import com.toll.models.TollPlaza;
import java.util.*;

public class TollPlazaRepository {
    private final Map<String, TollPlaza> plazaMap = new LinkedHashMap<>();

    public TollPlaza save(TollPlaza plaza) {
        if (plaza != null) {
            plazaMap.put(plaza.getId().toUpperCase(), plaza);
        }
        return plaza;
    }

    public Optional<TollPlaza> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(plazaMap.get(id.toUpperCase().trim()));
    }

    public Optional<TollPlaza> findByName(String name) {
        if (name == null) return Optional.empty();
        return plazaMap.values().stream()
                .filter(p -> p.getName().equalsIgnoreCase(name.trim()))
                .findFirst();
    }

    public List<TollPlaza> findAll() {
        return new ArrayList<>(plazaMap.values());
    }

    public boolean existsById(String id) {
        if (id == null) return false;
        return plazaMap.containsKey(id.toUpperCase().trim());
    }

    public boolean existsByName(String name) {
        return findByName(name).isPresent();
    }
}
