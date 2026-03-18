package com.golfleaderboard.util

/**
 * Normalizes player names for matching between picks and leaderboard.
 * Handles: trim, lowercase, collapse spaces, apostrophes (O'Brien, O'Hair), accents.
 */
object PlayerNameNormalizer {

    private val apostropheVariants = listOf("'", "&#39;", "`", "′")
    private val accentMap = mapOf(
        "ø" to "o", "Ø" to "o",
        "æ" to "ae", "Æ" to "ae",
        "é" to "e", "è" to "e", "ê" to "e",
        "ü" to "u", "ú" to "u",
        "á" to "a", "à" to "a",
        "í" to "i", "ï" to "i",
        "ó" to "o", "ö" to "o",
        "ñ" to "n", "ã" to "a"
    )

    fun normalize(name: String): String {
        var s = name.trim().lowercase()
        for (apostrophe in apostropheVariants) {
            s = s.replace(apostrophe, "")
        }
        for ((acc, rep) in accentMap) {
            s = s.replace(acc, rep)
        }
        return s.replace(Regex("\\s+"), " ")
    }

}
