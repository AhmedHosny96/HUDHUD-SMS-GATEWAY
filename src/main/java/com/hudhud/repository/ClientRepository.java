package com.hudhud.repository;

import com.hudhud.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findClientByName(String name);

    Optional<Client> findClientByEmail(String email);

    Optional<Client> findClientBySenderId(String senderId);

    Optional<Client> findClientByUsername(String username);

    Optional<Client> findClientByPackageId(int packageId);


}
