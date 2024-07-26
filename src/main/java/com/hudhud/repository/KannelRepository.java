package com.hudhud.repository;

import com.hudhud.model.Kannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KannelRepository extends JpaRepository<Kannel , Long> {
}
