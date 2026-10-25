package com.arcade.backend.repository;

import com.arcade.backend.model.MemoryMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemoryMatchRepository extends JpaRepository<MemoryMatch, Long> {

    @Query("SELECT m FROM MemoryMatch m ORDER BY m.movesCount ASC, m.durationSeconds ASC, m.completedAt DESC")
    List<MemoryMatch> findLeaderboard();
}
