package com.golfleaderboard.domain

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "user_scores", uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "tournament_id"])])
data class UserScore(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    val tournament: Tournament,

    @Column(name = "total_score", nullable = false)
    var totalScore: Int = 0,

    @Column(name = "rank")
    var rank: Int? = null,

    @Column(name = "computed_at", nullable = false)
    var computedAt: Instant = Instant.now()
)
