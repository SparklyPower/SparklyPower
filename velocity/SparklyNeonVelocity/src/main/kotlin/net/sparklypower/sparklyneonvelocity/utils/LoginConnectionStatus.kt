package net.sparklypower.sparklyneonvelocity.utils

enum class LoginConnectionStatus(val fancyName: String, val color: String) {
	OK("OK", "§a"),
	BANNED("Banido", "§c"),
	IP_BANNED("Banido por IP", "§c"),
	INVALID_NAME("Nome Inválido", "§8"),
	USING_YOUTUBER_NAME("Nome de YouTuber", "§c"),
	USING_STAFF_NAME("Nome de Staff", "§c"),
	BLACKLISTED_ASN("ASN suspeito (Proxy, VPN)", "§c")
}