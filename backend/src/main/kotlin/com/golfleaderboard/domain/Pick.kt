package com.golfleaderboard.domain

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "picks", uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "tournament_id"])])
data class Pick(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    val tournament: Tournament,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pick_players", joinColumns = [JoinColumn(name = "pick_id")])
    @Column(name = "player_name")
    @OrderColumn(name = "player_order")
    var playerNames: MutableList<String> = mutableListOf(),

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(nullable = false)
    var updatedAt: Instant = Instant.now()
)
