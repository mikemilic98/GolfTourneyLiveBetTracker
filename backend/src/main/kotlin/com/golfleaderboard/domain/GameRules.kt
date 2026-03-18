package com.golfleaderboard.domain

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "game_rules")
data class GameRules(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false, unique = true)
    val tournament: Tournament,

    @Column(name = "num_picks", nullable = false)
    var numPicks: Int = 5,

    @Column(name = "num_dropped", nullable = false)
    var numDropped: Int = 1,

    /** Tie rule: e.g. "BEST_SINGLE_PICK", "LOWEST_SUM" */
    @Column(name = "tie_rule")
    var tieRule: String = "LOWEST_SUM",

    /** WD/DQ/DNS position penalty (default 200) */
    @Column(name = "wd_penalty_position")
    var wdPenaltyPosition: Int = 200,

    @Column(name = "locked_at")
    var lockedAt: Instant? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(nullable = false)
    var updatedAt: Instant = Instant.now()
)
