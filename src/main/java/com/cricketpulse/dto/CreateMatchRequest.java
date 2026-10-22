package com.cricketpulse.dto;

import com.cricketpulse.model.MatchStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateMatchRequest(
    @NotBlank String teamOne,
    @NotBlank String teamTwo,
    @NotBlank String venue,
    @NotBlank @Pattern(regexp = "T20|ODI|TEST", flags = Pattern.Flag.CASE_INSENSITIVE) String format,
    MatchStatus status
) {}
