package net.perfectdreams.dreamclubes.utils

enum class ClubeNameCheckResult {
    ALREADY_IN_USE,
    BLACKLISTED_NAME,
    TAG_TOO_SHORT,
    TAG_TOO_LONG,
    TAG_TOO_LONG_VIP,
    OK
}