package com.hudhud.repository;

import com.hudhud.model.DeliveryReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryReportRepo extends JpaRepository<DeliveryReport , Long> {
}
