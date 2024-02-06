package com.hudhud.repository;

import com.hudhud.model.Sms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SmsRepository extends JpaRepository<Sms, Long> {

    @Query("SELECT COUNT(m) FROM Sms m WHERE m.clientId = :clientId AND m.date BETWEEN :startDate AND :endDate")
    long countByClientIdAndDateBetween(
            @Param("clientId") Long clientId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    List<Sms> findByClientId(String clientId);

    @Query(value = "SELECT TOP 50 * FROM Sms s WHERE s.sent = 0 ORDER BY s.date DESC", nativeQuery = true)
    List<Sms> findPendingSms();

    @Modifying
    @Query(value = "UPDATE Sms SET sent = 1 WHERE id = :smsId", nativeQuery = true)
    @Transactional
    void markSmsAsSent(@Param("smsId") Long smsId);

    Sms findTopBySentOrderByIdAsc(int sent);
}
