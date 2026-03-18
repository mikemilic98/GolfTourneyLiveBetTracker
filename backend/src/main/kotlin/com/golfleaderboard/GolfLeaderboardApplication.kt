package com.golfleaderboard

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class GolfLeaderboardApplication

fun main(args: Array<String>) {
    runApplication<GolfLeaderboardApplication>(*args)
}
