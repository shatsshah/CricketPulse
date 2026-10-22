package com.cricketpulse.service;

import com.cricketpulse.dto.CreateMatchRequest;
import com.cricketpulse.dto.DeliveryRequest;
import com.cricketpulse.model.*;
import com.cricketpulse.repo.DeliveryRepository;
import com.cricketpulse.repo.MatchRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CricketService {
    private final MatchRepository matchRepo;
    private final DeliveryRepository deliveryRepo;

    public CricketService(MatchRepository matchRepo, DeliveryRepository deliveryRepo) {
        this.matchRepo = matchRepo;
        this.deliveryRepo = deliveryRepo;
    }

    @PostConstruct
    public void seedMatches() {
        if (matchRepo.count() > 0) return;
        CricketMatch a = matchRepo.save(new CricketMatch("Mumbai Falcons", "Pune Strikers", "MCA Stadium, Pune", "T20", MatchStatus.LIVE));
        CricketMatch b = matchRepo.save(new CricketMatch("Delhi Kings", "Chennai Royals", "Arun Jaitley Stadium, Delhi", "T20", MatchStatus.SCHEDULED));
        CricketMatch c = matchRepo.save(new CricketMatch("Bengaluru Blazers", "Hyderabad Hawks", "M. Chinnaswamy Stadium, Bengaluru", "ODI", MatchStatus.COMPLETED));
        addSeedBall(a, "A. Sharma", "R. Patil", 4, false);
        addSeedBall(a, "A. Sharma", "R. Patil", 1, false);
        addSeedBall(a, "V. Deshmukh", "R. Patil", 0, false);
        addSeedBall(a, "V. Deshmukh", "R. Patil", 6, false);
        addSeedBall(a, "V. Deshmukh", "R. Patil", 0, true);
        addSeedBall(a, "K. More", "R. Patil", 2, false);
        addSeedBall(c, "S. Rao", "A. Khan", 3, false);
        addSeedBall(c, "S. Rao", "A. Khan", 4, false);
        addSeedBall(c, "P. Nair", "A. Khan", 1, false);
    }

    private void addSeedBall(CricketMatch match, String batter, String bowler, int runs, boolean wicket) {
        deliveryRepo.save(new Delivery(match, batter, bowler, runs, ExtraType.NONE, 0, wicket, wicket ? "Caught" : ""));
    }

    public List<Map<String, Object>> getMatches(String search, MatchStatus status) {
        List<CricketMatch> matches = status == null ? matchRepo.findAll() : matchRepo.findByStatusOrderByStartTimeDesc(status);
        return matches.stream()
            .filter(m -> search == null || search.isBlank() || (m.getTeamOne() + " " + m.getTeamTwo() + " " + m.getVenue()).toLowerCase().contains(search.toLowerCase()))
            .map(this::matchView).collect(Collectors.toList());
    }

    public Map<String, Object> getMatch(Long id) {
        return matchView(findMatch(id));
    }

    public Map<String, Object> createMatch(CreateMatchRequest req) {
        if (req.teamOne().trim().equalsIgnoreCase(req.teamTwo().trim())) throw new IllegalArgumentException("A team cannot play against itself.");
        CricketMatch match = new CricketMatch(req.teamOne().trim(), req.teamTwo().trim(), req.venue().trim(), req.format().toUpperCase(), req.status() == null ? MatchStatus.SCHEDULED : req.status());
        return matchView(matchRepo.save(match));
    }

    public Map<String, Object> updateStatus(Long id, MatchStatus status) {
        CricketMatch match = findMatch(id);
        match.setStatus(status);
        return matchView(matchRepo.save(match));
    }

    @Transactional
    public Map<String, Object> addDelivery(Long matchId, DeliveryRequest req) {
        CricketMatch match = findMatch(matchId);
        if (match.getStatus() == MatchStatus.COMPLETED) throw new IllegalArgumentException("This match is already completed.");
        if (req.runs() > 6) throw new IllegalArgumentException("Runs must be between 0 and 6.");
        ExtraType extraType = req.extraType() == null ? ExtraType.NONE : req.extraType();
        int extraRuns = req.extraRuns();
        if (extraType == ExtraType.NONE) extraRuns = 0;
        if ((extraType == ExtraType.WIDE || extraType == ExtraType.NO_BALL) && extraRuns == 0) extraRuns = 1;
        Delivery delivery = new Delivery(match, req.batter().trim(), req.bowler().trim(), req.runs(), extraType, extraRuns, req.wicket(), req.wicketType());
        delivery.setInningsNumber(req.inningsNumber() == 0 ? 1 : req.inningsNumber());
        deliveryRepo.save(delivery);
        if (match.getStatus() == MatchStatus.SCHEDULED) {
            match.setStatus(MatchStatus.LIVE);
            matchRepo.save(match);
        }
        return Map.of("message", "Ball added", "delivery", ballView(delivery), "scorecard", scorecard(matchId));
    }

    public List<Map<String, Object>> getBalls(Long matchId) {
        findMatch(matchId);
        return deliveryRepo.findByMatchIdOrderByIdAsc(matchId).stream().map(this::ballView).collect(Collectors.toList());
    }

    public Map<String, Object> scorecard(Long matchId) {
        CricketMatch match = findMatch(matchId);
        List<Delivery> balls = deliveryRepo.findByMatchIdOrderByIdAsc(matchId);
        Map<Integer, List<Delivery>> innings = balls.stream().collect(Collectors.groupingBy(Delivery::getInningsNumber, TreeMap::new, Collectors.toList()));
        List<Map<String, Object>> inningsCards = new ArrayList<>();
        for (int inn = 1; inn <= 2; inn++) {
            List<Delivery> list = innings.getOrDefault(inn, List.of());
            int total = list.stream().mapToInt(d -> d.getRuns() + d.getExtraRuns()).sum();
            int wickets = (int) list.stream().filter(Delivery::isWicket).count();
            int legal = (int) list.stream().filter(d -> d.getExtraType() != ExtraType.WIDE && d.getExtraType() != ExtraType.NO_BALL).count();
            double overs = legal / 6 + (legal % 6) / 10.0;
            double runRate = legal == 0 ? 0 : (double) total * 6 / legal;
            String battingTeam = inn == 1 ? match.getTeamOne() : match.getTeamTwo();
            inningsCards.add(Map.of("innings", inn, "team", battingTeam, "runs", total, "wickets", wickets, "legalBalls", legal, "overs", String.format(Locale.US, "%d.%d", legal / 6, legal % 6), "runRate", Math.round(runRate * 100.0) / 100.0, "balls", list.size()));
        }
        return Map.of("match", matchView(match), "innings", inningsCards, "recentBalls", balls.stream().skip(Math.max(0, balls.size() - 12)).map(this::ballView).collect(Collectors.toList()));
    }

    public List<Map<String, Object>> playerStats(Long matchId) {
        List<Delivery> balls;
        if (matchId == null) balls = deliveryRepo.findAll();
        else {
            findMatch(matchId);
            balls = deliveryRepo.findByMatchIdOrderByIdAsc(matchId);
        }
        Map<String, Map<String, Object>> stats = new LinkedHashMap<>();
        for (Delivery d : balls) {
            Map<String, Object> batter = stats.computeIfAbsent(d.getBatter(), k -> new HashMap<>(Map.of("player", k, "runs", 0, "ballsFaced", 0, "fours", 0, "sixes", 0, "wickets", 0, "ballsBowled", 0)));
            batter.put("runs", (int)batter.get("runs") + d.getRuns());
            if (d.getExtraType() != ExtraType.WIDE) batter.put("ballsFaced", (int)batter.get("ballsFaced") + 1);
            if (d.getRuns() == 4) batter.put("fours", (int)batter.get("fours") + 1);
            if (d.getRuns() == 6) batter.put("sixes", (int)batter.get("sixes") + 1);
            Map<String, Object> bowler = stats.computeIfAbsent(d.getBowler(), k -> new HashMap<>(Map.of("player", k, "runs", 0, "ballsFaced", 0, "fours", 0, "sixes", 0, "wickets", 0, "ballsBowled", 0)));
            bowler.put("ballsBowled", (int)bowler.get("ballsBowled") + ((d.getExtraType() == ExtraType.WIDE || d.getExtraType() == ExtraType.NO_BALL) ? 0 : 1));
            if (d.isWicket()) bowler.put("wickets", (int)bowler.get("wickets") + 1);
        }
        return stats.values().stream().sorted((a, b) -> Integer.compare((int)b.get("runs"), (int)a.get("runs"))).collect(Collectors.toList());
    }

    public Map<String, Object> summary() {
        List<CricketMatch> matches = matchRepo.findAll();
        return Map.of(
            "totalMatches", matches.size(),
            "liveMatches", matches.stream().filter(m -> m.getStatus() == MatchStatus.LIVE).count(),
            "scheduledMatches", matches.stream().filter(m -> m.getStatus() == MatchStatus.SCHEDULED).count(),
            "completedMatches", matches.stream().filter(m -> m.getStatus() == MatchStatus.COMPLETED).count(),
            "totalDeliveries", deliveryRepo.count()
        );
    }

    private CricketMatch findMatch(Long id) {
        return matchRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Match " + id + " was not found."));
    }

    private Map<String, Object> matchView(CricketMatch match) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", match.getId());
        result.put("teamOne", match.getTeamOne());
        result.put("teamTwo", match.getTeamTwo());
        result.put("venue", match.getVenue());
        result.put("format", match.getFormat());
        result.put("status", match.getStatus());
        result.put("startTime", match.getStartTime());
        List<Delivery> balls = deliveryRepo.findByMatchIdOrderByIdAsc(match.getId());
        for (int inn = 1; inn <= 2; inn++) {
            int inningsNo = inn;
            List<Delivery> part = balls.stream().filter(d -> d.getInningsNumber() == inningsNo).toList();
            int runs = part.stream().mapToInt(d -> d.getRuns() + d.getExtraRuns()).sum();
            int wickets = (int) part.stream().filter(Delivery::isWicket).count();
            int legal = (int) part.stream().filter(d -> d.getExtraType() != ExtraType.WIDE && d.getExtraType() != ExtraType.NO_BALL).count();
            result.put(inn == 1 ? "teamOneScore" : "teamTwoScore", runs + "/" + wickets);
            result.put(inn == 1 ? "teamOneOvers" : "teamTwoOvers", (legal / 6) + "." + (legal % 6));
        }
        return result;
    }

    private Map<String, Object> ballView(Delivery d) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", d.getId());
        map.put("batter", d.getBatter());
        map.put("bowler", d.getBowler());
        map.put("runs", d.getRuns());
        map.put("extraType", d.getExtraType());
        map.put("extraRuns", d.getExtraRuns());
        map.put("totalRuns", d.getRuns() + d.getExtraRuns());
        map.put("wicket", d.isWicket());
        map.put("wicketType", d.getWicketType());
        map.put("inningsNumber", d.getInningsNumber());
        map.put("createdAt", d.getCreatedAt());
        return map;
    }
}
