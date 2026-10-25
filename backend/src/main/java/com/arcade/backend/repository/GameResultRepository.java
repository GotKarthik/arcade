package com.arcade.backend.repository;

import com.arcade.backend.model.GameResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameResultRepository extends JpaRepository<GameResult, Long> {

    /** Fetch top 10 wins for a game type, ordered by fewest guesses then most recent. */
    @Query("""
        SELECT g FROM GameResult g
        WHERE g.gameType = :gameType AND g.outcome = 'WON'
        ORDER BY g.guessesUsed ASC, g.completedAt DESC
        """)
    List<GameResult> findLeaderboard(String gameType);
}
