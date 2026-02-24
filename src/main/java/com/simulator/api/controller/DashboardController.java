package com.simulator.api.controller;

import com.simulator.api.dto.DashboardDto;
import com.simulator.persistence.repository.TransactionLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dashboard KPIs endpoint.
 */
@RestController
@RequestMapping("/api/v1/dashboard")

public class DashboardController {

    private final TransactionLogRepository txLogRepo;

    public DashboardController(TransactionLogRepository txLogRepo) {
        this.txLogRepo = txLogRepo;
    }

    @GetMapping
    public ResponseEntity<DashboardDto> getDashboard() {
        DashboardDto dto = new DashboardDto();
        long total = txLogRepo.count();
        long success = txLogRepo.countSuccess();
        long refused = txLogRepo.countRefused();
        Double avgLatency = txLogRepo.avgDuration();

        dto.setTotalTransactions(total);
        dto.setSuccessCount(success);
        dto.setRefusedCount(refused);
        dto.setSuccessRate(total > 0 ? (double) success / total * 100 : 0);
        dto.setRefusedRate(total > 0 ? (double) refused / total * 100 : 0);
        dto.setAvgLatencyMs(avgLatency);

        return ResponseEntity.ok(dto);
    }
}
