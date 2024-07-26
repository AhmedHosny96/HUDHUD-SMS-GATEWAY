package com.hudhud.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "Kannel")
public class Kannel {

    @Id
    @GeneratedValue
    private Long id;
    private Long totalCount;
    private LocalDateTime startDate = LocalDateTime.now();

}



