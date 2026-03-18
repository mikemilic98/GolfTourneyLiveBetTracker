package com.golfleaderboard.scraper

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/** ESPN API response DTOs for site.api.espn.com/apis/site/v2/sports/golf/leaderboard */
@JsonIgnoreProperties(ignoreUnknown = true)
data class EspnLeaderboardResponse(
    val events: List<EspnEvent>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EspnEvent(
    val id: String? = null,
    val name: String? = null,
    val date: String? = null,
    val endDate: String? = null,
    val competitions: List<EspnCompetition>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EspnCompetition(
    val id: String? = null,
    val competitors: List<EspnCompetitor>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EspnCompetitor(
    val athlete: EspnAthlete? = null,
    val status: EspnCompetitorStatus? = null,
    val statistics: List<EspnStatistic>? = null,
    val score: EspnScore? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EspnAthlete(
    val id: String? = null,
    val displayName: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EspnCompetitorStatus(
    val thru: Int? = null,
    val position: EspnPosition? = null,
    val detail: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EspnPosition(
    val displayName: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EspnStatistic(
    val name: String? = null,
    val displayValue: String? = null,
    val value: Double? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EspnScore(
    val displayValue: String? = null
)
