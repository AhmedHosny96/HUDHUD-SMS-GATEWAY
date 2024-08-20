package com.hudhud.repository;

import com.hudhud.model.SmsHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SmsHistoryRepo extends JpaRepository<SmsHistory, Long> {

    Optional<SmsHistory> findByClientIdAndDate(Long clientId, LocalDate date);

    List<SmsHistory> findByDate(LocalDate today);

    List<SmsHistory> findByDateBetween(LocalDate startOfMonth, LocalDate endOfMonth);


    Optional<List<SmsHistory>> findByClientId(Long clientId);

    List<SmsHistory> findByClientIdAndDateBetween(Long clientId, LocalDate startDate, LocalDate endDate);

}
