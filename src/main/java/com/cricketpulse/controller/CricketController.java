package com.cricketpulse.controller;

import com.cricketpulse.dto.CreateMatchRequest;
import com.cricketpulse.dto.DeliveryRequest;
import com.cricketpulse.model.MatchStatus;
import com.cricketpulse.service.CricketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class CricketController {
    private final CricketService service;

    public CricketController(CricketService service) {
        this.service = service;
    }

    @GetMapping("/matches")
    public List<Map<String, Object>> matches(@RequestParam(required = false) String search, @RequestParam(required = false) MatchStatus status) {
        return service.getMatches(search, status);
    }

    @GetMapping("/matches/{id}")
    public Map<String, Object> match(@PathVariable Long id) {
        return service.getMatch(id);
    }

    @PostMapping("/matches")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createMatch(@Valid @RequestBody CreateMatchRequest request) {
        return service.createMatch(request);
    }

    @PatchMapping("/matches/{id}/status")
    public Map<String, Object> updateStatus(@PathVariable Long id, @RequestParam MatchStatus status) {
        return service.updateStatus(id, status);
    }

    @GetMapping("/matches/{id}/scorecard")
    public Map<String, Object> scorecard(@PathVariable Long id) {
        return service.scorecard(id);
    }

    @GetMapping("/matches/{id}/balls")
    public List<Map<String, Object>> balls(@PathVariable Long id) {
        return service.getBalls(id);
    }

    @PostMapping("/matches/{id}/balls")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> addBall(@PathVariable Long id, @Valid @RequestBody DeliveryRequest request) {
        return service.addDelivery(id, request);
    }

    @GetMapping("/players/stats")
    public List<Map<String, Object>> playerStats(@RequestParam(required = false) Long matchId) {
        return service.playerStats(matchId);
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        return service.summary();
    }
}
