package net.perfectdreams.dreamtrails.utils

import org.bukkit.Color
import org.bukkit.Material

enum class Halo(val title: String, val material: Material, val color: Color) {
    DARK_RED("§4§lAuréola Vermelho Escuro", Material.RED_WOOL, Color.fromRGB(170, 0, 0)),
    RED("§c§lAuréola Vermelha", Material.PINK_WOOL, Color.fromRGB(255, 85, 85)),
    ORANGE("§6§lAuréola Dourada", Material.ORANGE_WOOL, Color.fromRGB(255, 170, 0)),
    YELLOW("§e§lAuréola Amarela", Material.YELLOW_WOOL, Color.fromRGB(255, 255, 85)),
    GREEN("§2§lAuréola Verde Escura", Material.GREEN_WOOL, Color.fromRGB(0, 170, 0)),
    LIME("§a§lAuréola Verde", Material.LIME_WOOL, Color.fromRGB(85, 255, 85)),
    AQUA("§b§lAuréola Azul Clara", Material.LIGHT_BLUE_WOOL, Color.fromRGB(85, 255, 255)),
    CYAN("§3§lAuréola Ciana", Material.CYAN_WOOL, Color.fromRGB(0, 170, 170)),
    DARK_BLUE("§1§lAuréola Azul Escura", Material.BLUE_WOOL, Color.fromRGB(0, 0, 170)),
    BLUE("§9§lAuréola Azul", Material.CYAN_WOOL, Color.fromRGB(85, 85, 255)),
    MAGENTA("§d§lAuréola Rosa Choque", Material.MAGENTA_WOOL, Color.fromRGB(255, 85, 255)),
    PURPLE("§5§lAuréola Roxa", Material.PURPLE_WOOL, Color.fromRGB(170, 0, 170)),
    WHITE("§f§lAuréola Branca", Material.WHITE_WOOL, Color.fromRGB(255, 255, 255)),
    LIGHT_GRAY("§f§lAuréola Cinza Claro", Material.LIGHT_GRAY_WOOL, Color.fromRGB(170, 170, 170)),
    GRAY("§8§lAuréola Cinza", Material.GRAY_WOOL, Color.fromRGB(85, 85, 85)),
    BLACK("§0§lAuréola Preta", Material.BLACK_WOOL, Color.fromRGB(0, 0, 0)),
    RAINBOW("§d§lArco-Íris (Troca a cor automaticamente!)", Material.GOLD_BLOCK, Color.fromRGB(127, 127, 127))
}