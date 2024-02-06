package com.hudhud.repository;

import com.hudhud.model.Packages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PackageRepository extends JpaRepository<Packages, Integer> {


    Optional<Packages> findByName(String name);

    Optional<Packages> findByType(String type);
}
