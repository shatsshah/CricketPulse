package com.cricketpulse.repo;

import com.cricketpulse.model.CricketMatch;
import com.cricketpulse.model.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MatchRepository extends JpaRepository<CricketMatch, Long> {
    List<CricketMatch> findByStatusOrderByStartTimeDesc(MatchStatus status);
}
