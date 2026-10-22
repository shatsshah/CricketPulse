package com.cricketpulse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries")
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private CricketMatch match;

    @Column(nullable = false)
    private String batter;

    @Column(nullable = false)
    private String bowler;

    private int runs;

    @Enumerated(EnumType.STRING)
    private ExtraType extraType = ExtraType.NONE;

    private int extraRuns;
    private boolean wicket;
    private String wicketType = "";
    private int inningsNumber = 1;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Delivery() {}

    public Delivery(CricketMatch match, String batter, String bowler, int runs, ExtraType extraType, int extraRuns, boolean wicket, String wicketType) {
        this.match = match;
        this.batter = batter;
        this.bowler = bowler;
        this.runs = runs;
        this.extraType = extraType == null ? ExtraType.NONE : extraType;
        this.extraRuns = extraRuns;
        this.wicket = wicket;
        this.wicketType = wicketType == null ? "" : wicketType;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public CricketMatch getMatch() { return match; }
    public void setMatch(CricketMatch match) { this.match = match; }
    public String getBatter() { return batter; }
    public void setBatter(String batter) { this.batter = batter; }
    public String getBowler() { return bowler; }
    public void setBowler(String bowler) { this.bowler = bowler; }
    public int getRuns() { return runs; }
    public void setRuns(int runs) { this.runs = runs; }
    public ExtraType getExtraType() { return extraType; }
    public void setExtraType(ExtraType extraType) { this.extraType = extraType; }
    public int getExtraRuns() { return extraRuns; }
    public void setExtraRuns(int extraRuns) { this.extraRuns = extraRuns; }
    public boolean isWicket() { return wicket; }
    public void setWicket(boolean wicket) { this.wicket = wicket; }
    public String getWicketType() { return wicketType; }
    public void setWicketType(String wicketType) { this.wicketType = wicketType; }
    public int getInningsNumber() { return inningsNumber; }
    public void setInningsNumber(int inningsNumber) { this.inningsNumber = inningsNumber; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
