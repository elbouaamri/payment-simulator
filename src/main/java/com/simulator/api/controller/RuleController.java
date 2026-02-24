package com.simulator.api.controller;

import com.simulator.persistence.entity.SimulationRule;
import com.simulator.persistence.repository.SimulationRuleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for simulation rule CRUD.
 */
@RestController
@RequestMapping("/api/v1/rules")

public class RuleController {

    private final SimulationRuleRepository ruleRepo;

    public RuleController(SimulationRuleRepository ruleRepo) {
        this.ruleRepo = ruleRepo;
    }

    @GetMapping
    public ResponseEntity<List<SimulationRule>> getAll() {
        return ResponseEntity.ok(ruleRepo.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SimulationRule> getById(@PathVariable Long id) {
        return ruleRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SimulationRule> create(@RequestBody SimulationRule rule) {
        SimulationRule saved = ruleRepo.save(rule);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SimulationRule> update(@PathVariable Long id, @RequestBody SimulationRule rule) {
        return ruleRepo.findById(id)
                .map(existing -> {
                    existing.setName(rule.getName());
                    existing.setPriority(rule.getPriority());
                    existing.setEnabled(rule.isEnabled());
                    existing.setConditionType(rule.getConditionType());
                    existing.setConditionValue(rule.getConditionValue());
                    existing.setOutcomeScenario(rule.getOutcomeScenario());
                    existing.setResponseCode39(rule.getResponseCode39());
                    return ResponseEntity.ok(ruleRepo.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (ruleRepo.existsById(id)) {
            ruleRepo.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
