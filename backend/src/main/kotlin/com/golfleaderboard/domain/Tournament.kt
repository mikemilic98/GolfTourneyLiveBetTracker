package com.golfleaderboard.domain

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "tournaments")
data class Tournament(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var name: String,

    @Column(name = "espn_url", nullable = false)
    var espnUrl: String,

    /** ESPN event ID extracted from URL or API (e.g. for site.api.espn.com/.../leaderboard?event=401155513) */
    @Column(name = "espn_event_id")
    var espnEventId: String? = null,

    /** JSON array of player names from roster at tournament start */
    @Column(name = "roster_snapshot", columnDefinition = "TEXT")
    var rosterSnapshot: String? = null,

    @Column(name = "start_time")
    var startTime: Instant? = null,

    @Column(name = "end_time")
    var endTime: Instant? = null,

    @Column(name = "picks_cutoff")
    var picksCutoff: Instant? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(nullable = false)
    var updatedAt: Instant = Instant.now()
)
