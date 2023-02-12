package net.perfectdreams.dreamchat.utils.bot.responses

import net.perfectdreams.dreamchat.utils.ChatUtils
import org.bukkit.ChatColor
import org.bukkit.entity.Player

class LulaAssinaResponse : AssinaResponse(
	"Lula",
	ChatColor.RED,
	"Lula",
	"ewogICJ0aW1lc3RhbXAiIDogMTY2NzE3MTU2NTc1MSwKICAicHJvZmlsZUlkIiA6ICJkMmM1MThjMzJjNTY0OGJmOTM2ZjY0YjhmZDQ1ZThjZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJEcmFjb0JlbGxzIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2RmNDYzYzA5NmQ2ZGU3MzZiMTczNjRkODdjY2I3N2VkNDU1Mjc3MGQxYmMwNzlkYzQ0NjBjODQ0OGQ0ODkyOTUiCiAgICB9CiAgfQp9",
	"qBLlTPSUE8ADw516eE6YUgvlicnm9puKE6D8I1yr3TWu5qpfWhIAb7GLpgX59ZU1kWVYa9CtPaFpw9cCXcpexCHgazttr3imH0bmdcJ5AaBM0fGGxzKZadsjRS9qM4gvgesNTIGET2lsD0hPIa005gb/iTrnNR5hR0JwkIPRpbuqNhFs9GkqC6MDLvVB6JIP/62lqtUY3Czin0af1g+fliz6p0/XF5TvWchkMpYgtX6Zm0H8Dw7zhFWPbQ3kcHaDAnI6OYa03uJaxk2gA8E0ppPCDDEu11WOkHXjyaoNscJoaXgT4zBIz/OloKXMtCiIc8t6+zOvW9fET6cTP5UMdRr7LF2TiBKuIQqhd0PYc+spgrzBzYy0Q+EwW8etOPFzpPYQRHO9Izfb4rzWlv65zss6Fxb2bLQGusQ9llBGZWthhrV6MCf/hQFbTMNgwVB+UX4LMHHn89NbZVlZt5yhmB3Nzv5nNEAgFT/3XwJIrTOL97lCf+qRcx7iV9VyymHyMXKa9rvcOMUlmFTQszHKy7sZt2C4gU+VXqgLZD++PnqFo0HHhlLjhlNyFlAqq6gPgJvft8fGbCcpHDSjNXBgjfWAUix08DcBDbSI1AlpIZaHsZFf7SvkJnjaWsCqHGIS1pNzniuSaCcvMJsZ+qhSuJGGm1zXORtVtrMGiE67vdc=",
	ChatUtils::sendResponseAsLula
) {
	override fun getSuccessResponse(player: Player) = "§aOpa meu bom companheiro §b${player.displayName}§a, eu assinei a sua casa."

	override fun getSignLines() = listOf(
		"§4§m---------",
		"§4✪$color$name§4✪",
		"§4aprova! ʕ•ᴥ•ʔ",
		"§4§m---------"
	)
}