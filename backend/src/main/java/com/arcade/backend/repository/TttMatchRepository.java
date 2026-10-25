package com.arcade.backend.repository;

import com.arcade.backend.model.TttMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TttMatchRepository extends JpaRepository<TttMatch, Long> {

    @Query("SELECT m FROM TttMatch m ORDER BY m.completedAt DESC")
    List<TttMatch> findRecentMatches();
}
