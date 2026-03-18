package com.golfleaderboard.domain

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "leaderboard_entries", indexes = [
    Index(name = "idx_leaderboard_tournament_fetched", columnList = "tournament_id, fetched_at")
])
data class LeaderboardEntry(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    val tournament: Tournament,

    @Column(name = "player_name", nullable = false)
    val playerName: String,

    /** Parsed position: 1, 4 (for T4), 200 for WD/DQ/DNS */
    @Column(nullable = false)
    val position: Int,

    @Column(name = "to_par")
    val toPar: String? = null,

    @Column(name = "thru")
    val thru: String? = null,

    @Column(name = "fetched_at", nullable = false)
    val fetchedAt: Instant = Instant.now()
)
