package com.simulator.api.controller;

import com.simulator.api.dto.TransactionRequest;
import com.simulator.api.dto.TransactionResponse;
import com.simulator.engine.TransactionService;
import com.simulator.persistence.entity.TransactionLog;
import com.simulator.persistence.repository.TransactionLogRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for ISO 8583 transaction operations.
 */
@RestController
@RequestMapping("/api/v1/transactions")

public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    private final TransactionService transactionService;
    private final TransactionLogRepository txLogRepo;

    public TransactionController(TransactionService transactionService,
            TransactionLogRepository txLogRepo) {
        this.transactionService = transactionService;
        this.txLogRepo = txLogRepo;
    }

    /**
     * POST /api/v1/transactions/authorize – Authorization (MTI 0200/0100)
     */
    @PostMapping("/authorize")
    public ResponseEntity<TransactionResponse> authorize(@Valid @RequestBody TransactionRequest request)
            throws Exception {
        request.setType("AUTHORIZE");
        return ResponseEntity.ok(transactionService.process(request));
    }

    /**
     * POST /api/v1/transactions/refund – Refund (MTI 0200, PC 200000)
     */
    @PostMapping("/refund")
    public ResponseEntity<TransactionResponse> refund(@Valid @RequestBody TransactionRequest request) throws Exception {
        request.setType("REFUND");
        return ResponseEntity.ok(transactionService.process(request));
    }

    /**
     * POST /api/v1/transactions/cancel – Cancellation/Void (MTI 0200, PC 020000)
     */
    @PostMapping("/cancel")
    public ResponseEntity<TransactionResponse> cancel(@Valid @RequestBody TransactionRequest request) throws Exception {
        request.setType("CANCEL");
        return ResponseEntity.ok(transactionService.process(request));
    }

    /**
     * POST /api/v1/transactions/reversal – Reversal (MTI 0400)
     */
    @PostMapping("/reversal")
    public ResponseEntity<TransactionResponse> reversal(@Valid @RequestBody TransactionRequest request)
            throws Exception {
        request.setType("REVERSAL");
        return ResponseEntity.ok(transactionService.process(request));
    }

    /**
     * GET /api/v1/transactions/{id} – Get single transaction by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionLog> getById(@PathVariable String id) {
        return txLogRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/v1/transactions?from=&to=&status=&terminalId= – Filtered list
     */
    @GetMapping
    public ResponseEntity<List<TransactionLog>> list(
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "terminalId", required = false) String terminalId) {

        List<TransactionLog> result = txLogRepo.findFiltered(from, to, status, terminalId);
        return ResponseEntity.ok(result);
    }
}
