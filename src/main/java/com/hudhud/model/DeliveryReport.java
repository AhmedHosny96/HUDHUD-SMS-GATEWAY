package com.hudhud.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "DeliveryReports")
public class DeliveryReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String dlr;
    private String phone;
    private String msgid;
    private String status;
    private LocalDateTime receivedAt;

    // Getters and setters
}