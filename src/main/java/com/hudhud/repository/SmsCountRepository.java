package com.hudhud.repository;

import com.hudhud.model.Sms;
import com.hudhud.model.SmsCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SmsCountRepository extends JpaRepository<SmsCount, Long> {


    SmsCount findByClientId(Long clientId);

    @Query("SELECT m.count FROM SmsCount m WHERE m.clientId = :clientId AND m.createdAt BETWEEN :startDate AND :endDate")
    long findSmsCountByClientIdAndDateBetween(
            @Param("clientId") Long clientId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT s.count FROM SmsCount s WHERE s.clientId = :clientId AND s.createdAt >= :startDate AND s.updatedAt <= :endDate")
    long smsCountBetweenDatesForClient(@Param("clientId") Long clientId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

}
