package com.arcade.backend.repository;

import com.arcade.backend.model.RpsMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RpsMatchRepository extends JpaRepository<RpsMatch, Long> {

    @Query("SELECT m FROM RpsMatch m ORDER BY m.completedAt DESC")
    List<RpsMatch> findRecentMatches();
}
