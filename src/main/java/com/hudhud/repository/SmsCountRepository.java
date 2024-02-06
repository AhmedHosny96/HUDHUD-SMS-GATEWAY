package com.hudhud.repository;

import com.hudhud.model.Client;
import com.hudhud.model.dto.SmsCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SmsCountRepository extends JpaRepository<SmsCount , Long> {


    SmsCount findByClient(Optional<Client> client);

//    SmsCount findByClient
}
