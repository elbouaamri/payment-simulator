package com.simulator.api.controller;

import com.simulator.persistence.entity.SimulationProfile;
import com.simulator.persistence.repository.SimulationProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for simulation profile CRUD.
 */
@RestController
@RequestMapping("/api/v1/profiles")

public class ProfileController {

    private final SimulationProfileRepository profileRepo;

    public ProfileController(SimulationProfileRepository profileRepo) {
        this.profileRepo = profileRepo;
    }

    @GetMapping
    public ResponseEntity<List<SimulationProfile>> getAll() {
        return ResponseEntity.ok(profileRepo.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SimulationProfile> getById(@PathVariable Long id) {
        return profileRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SimulationProfile> create(@RequestBody SimulationProfile profile) {
        SimulationProfile saved = profileRepo.save(profile);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SimulationProfile> update(@PathVariable Long id, @RequestBody SimulationProfile profile) {
        return profileRepo.findById(id)
                .map(existing -> {
                    existing.setName(profile.getName());
                    existing.setDescription(profile.getDescription());
                    existing.setDefaultScenario(profile.getDefaultScenario());
                    existing.setLatencyMs(profile.getLatencyMs());
                    existing.setAmountLimit(profile.getAmountLimit());
                    existing.setActive(profile.isActive());
                    return ResponseEntity.ok(profileRepo.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (profileRepo.existsById(id)) {
            profileRepo.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
