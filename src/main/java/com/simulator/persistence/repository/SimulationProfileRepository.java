package com.simulator.persistence.repository;

import com.simulator.persistence.entity.SimulationProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SimulationProfileRepository extends JpaRepository<SimulationProfile, Long> {
    Optional<SimulationProfile> findByName(String name);
    List<SimulationProfile> findByActiveTrue();
}
