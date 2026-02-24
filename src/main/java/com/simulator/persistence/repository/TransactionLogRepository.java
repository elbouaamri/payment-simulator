package com.simulator.persistence.repository;

import com.simulator.persistence.entity.TransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionLogRepository extends JpaRepository<TransactionLog, String> {

    List<TransactionLog> findByTerminalIdOrderByCreatedAtDesc(String terminalId);

    List<TransactionLog> findByResponseCode39OrderByCreatedAtDesc(String responseCode39);

    @Query("SELECT t FROM TransactionLog t WHERE " +
           "(:from IS NULL OR t.createdAt >= :from) AND " +
           "(:to IS NULL OR t.createdAt <= :to) AND " +
           "(:status IS NULL OR t.responseCode39 = :status) AND " +
           "(:terminalId IS NULL OR t.terminalId = :terminalId) " +
           "ORDER BY t.createdAt DESC")
    List<TransactionLog> findFiltered(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("status") String status,
            @Param("terminalId") String terminalId);

    @Query("SELECT COUNT(t) FROM TransactionLog t WHERE t.responseCode39 = '00'")
    long countSuccess();

    @Query("SELECT COUNT(t) FROM TransactionLog t WHERE t.responseCode39 <> '00' AND t.responseCode39 IS NOT NULL")
    long countRefused();

    @Query("SELECT AVG(t.durationMs) FROM TransactionLog t WHERE t.durationMs IS NOT NULL")
    Double avgDuration();
}
