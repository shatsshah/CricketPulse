package com.cricketpulse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cricket_matches")
public class CricketMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String teamOne;

    @Column(nullable = false)
    private String teamTwo;

    @Column(nullable = false)
    private String venue;

    @Column(nullable = false)
    private String format;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status;

    private LocalDateTime startTime = LocalDateTime.now();

    public CricketMatch() {}

    public CricketMatch(String teamOne, String teamTwo, String venue, String format, MatchStatus status) {
        this.teamOne = teamOne;
        this.teamTwo = teamTwo;
        this.venue = venue;
        this.format = format;
        this.status = status;
        this.startTime = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getTeamOne() { return teamOne; }
    public void setTeamOne(String teamOne) { this.teamOne = teamOne; }
    public String getTeamTwo() { return teamTwo; }
    public void setTeamTwo(String teamTwo) { this.teamTwo = teamTwo; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public MatchStatus getStatus() { return status; }
    public void setStatus(MatchStatus status) { this.status = status; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
}
