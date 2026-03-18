package com.golfleaderboard.scraper

import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.time.Instant

/**
 * Fetches golf leaderboard and roster from ESPN.
 * Uses ESPN API (site.api.espn.com) for leaderboard when event is in progress.
 * Uses Jsoup to scrape roster from Tournament Field on the HTML page.
 */
@Service
class EspnScraperService(
    private val restTemplate: RestTemplate
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val espnApiBase = "https://site.api.espn.com/apis/site/v2/sports/golf/leaderboard"

    /**
     * Fetches leaderboard from ESPN API. Pass null to use default/current tournament.
     */
    fun fetchLeaderboardFromApi(espnEventId: String?): List<RawLeaderboardEntry> {
        val url = if (espnEventId != null) {
            "$espnApiBase?event=$espnEventId"
        } else {
            espnApiBase
        }
        return try {
            val response = restTemplate.getForObject(url, EspnLeaderboardResponse::class.java)
                ?: return emptyList()
            parseApiResponse(response)
        } catch (e: Exception) {
            log.warn("ESPN API fetch failed for $url: ${e.message}")
            emptyList()
        }
    }

    private fun parseApiResponse(response: EspnLeaderboardResponse): List<RawLeaderboardEntry> {
        val events = response.events ?: return emptyList()
        val now = Instant.now()
        return events.flatMap { event ->
            event.competitions?.flatMap { comp ->
                comp.competitors?.mapNotNull { c -> competitorToEntry(c, now) } ?: emptyList()
            } ?: emptyList()
        }
    }

    private fun competitorToEntry(c: EspnCompetitor, fetchedAt: Instant): RawLeaderboardEntry? {
        val name = c.athlete?.displayName ?: return null
        val posStr = c.status?.position?.displayName ?: "-"
        val position = parsePosition(posStr)
        val toPar = c.statistics?.find { it.name == "scoreToPar" }?.displayValue
        val thru = when {
            c.status?.thru != null && c.status!!.thru!! >= 18 -> "F"
            c.status?.thru != null -> c.status!!.thru!!.toString()
            else -> c.status?.detail?.take(1)
        }
        return RawLeaderboardEntry(
            playerName = name,
            position = position,
            toPar = toPar,
            thru = thru,
            fetchedAt = fetchedAt
        )
    }

    /**
     * Parses position string: "1" -> 1, "T4" -> 4, "-" or "WD"/"DQ"/"DNS" -> 200
     */
    fun parsePosition(posStr: String): Int {
        val s = posStr.trim()
        if (s.isEmpty() || s == "-") return 200
        if (s.uppercase() in listOf("WD", "DQ", "DNS", "CUT")) return 200
        return s.removePrefix("T").toIntOrNull() ?: 200
    }

    /**
     * Fetches roster (player names) from Tournament Field on ESPN HTML page.
     * Used when tournament hasn't started and API has no leaderboard.
     */
    fun fetchRosterFromHtml(espnUrl: String): List<String> {
        return try {
            val doc = Jsoup.connect(espnUrl)
                .userAgent("Mozilla/5.0 (compatible; GolfLeaderboard/1.0)")
                .timeout(10000)
                .get()
            parseRosterFromHtml(doc.toString())
        } catch (e: Exception) {
            log.warn("ESPN HTML roster fetch failed for $espnUrl: ${e.message}")
            emptyList()
        }
    }

    /**
     * Parses roster from HTML. Looks for:
     * 1. Tournament Field table: links with player names
     * 2. Leaderboard table: .leaderboard-table tbody tr.player-overview (legacy)
     */
    internal fun parseRosterFromHtml(html: String): List<String> {
        val doc = Jsoup.parse(html)
        val names = mutableSetOf<String>()

        // Tournament Field: table with [Player Name](url) in first column
        doc.select("table a[href*='/golf/player/']").forEach { link ->
            val text = link.text().trim()
            if (text.isNotBlank() && !text.equals("PLAYER", ignoreCase = true)) {
                names.add(decodeHtmlEntities(text))
            }
        }

        // Fallback: leaderboard table rows
        if (names.isEmpty()) {
            doc.select(".leaderboard-table tbody tr.player-overview").forEach { row ->
                val playerCell = row.select("td").getOrNull(2)
                val text = playerCell?.select("a, span")?.firstOrNull()?.text()?.trim()
                if (!text.isNullOrBlank()) {
                    names.add(decodeHtmlEntities(text))
                }
            }
        }

        return names.sorted()
    }

    private fun decodeHtmlEntities(s: String): String =
        s.replace("&#39;", "'").replace("&amp;", "&").replace("&nbsp;", " ")
}

/** Raw entry from scraper before persistence */
data class RawLeaderboardEntry(
    val playerName: String,
    val position: Int,
    val toPar: String?,
    val thru: String?,
    val fetchedAt: Instant
)
