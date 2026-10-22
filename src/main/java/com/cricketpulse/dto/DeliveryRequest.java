package com.cricketpulse.dto;

import com.cricketpulse.model.ExtraType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record DeliveryRequest(
    @NotBlank String batter,
    @NotBlank String bowler,
    @Min(0) @Max(6) int runs,
    ExtraType extraType,
    @Min(0) @Max(6) int extraRuns,
    boolean wicket,
    String wicketType,
    @Min(1) @Max(2) int inningsNumber
) {}
