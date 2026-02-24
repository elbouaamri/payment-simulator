package com.simulator.persistence.repository;

import com.simulator.persistence.entity.SimulationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SimulationRuleRepository extends JpaRepository<SimulationRule, Long> {
    List<SimulationRule> findByEnabledTrueOrderByPriorityAsc();
}
