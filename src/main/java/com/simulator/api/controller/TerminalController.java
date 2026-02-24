package com.simulator.api.controller;

import com.simulator.persistence.entity.Terminal;
import com.simulator.persistence.repository.TerminalRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for terminal management.
 */
@RestController
@RequestMapping("/api/v1/terminals")

public class TerminalController {

    private final TerminalRepository terminalRepo;

    public TerminalController(TerminalRepository terminalRepo) {
        this.terminalRepo = terminalRepo;
    }

    @GetMapping
    public ResponseEntity<List<Terminal>> getAll() {
        return ResponseEntity.ok(terminalRepo.findAll());
    }

    @PostMapping
    public ResponseEntity<Terminal> create(@RequestBody Terminal terminal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(terminalRepo.save(terminal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (terminalRepo.existsById(id)) {
            terminalRepo.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
