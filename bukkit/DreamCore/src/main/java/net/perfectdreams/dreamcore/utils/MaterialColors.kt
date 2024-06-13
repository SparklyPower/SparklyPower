package net.perfectdreams.dreamcore.utils

import net.perfectdreams.dreamcore.DreamCore
import org.bukkit.Material
import java.awt.Color
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

object MaterialColors {
    private val averageColors = mutableMapOf<Material, Color>()
    private val materialFileNameRemaps = mapOf(
        Material.QUARTZ_BLOCK to "quartz_block_bottom"
    )

    fun initialize(m: DreamCore) {
        val materialsToBeAveraged = Material.entries.filter {
            !it.name.startsWith("SPARKLYPOWER_") && (it.name.endsWith("_CONCRETE") || (!it.name.contains("GLAZED") && it.name.endsWith("_TERRACOTTA")) || it == Material.TERRACOTTA || it == Material.QUARTZ_BLOCK || it.name.endsWith("_WOOL"))
        }

        for (material in materialsToBeAveraged) {
            val fileName = materialFileNameRemaps[material] ?: material.name.lowercase()
            println(File(m.dataFolder, "minecraft_assets/1_20_6/minecraft/textures/block/$fileName.png"))
            val image = ImageIO.read(File(m.dataFolder, "minecraft_assets/1_20_6/minecraft/textures/block/$fileName.png"))
            averageColors[material] = getAverageColorOfImage(image)
        }
    }

    private fun getAverageColorOfImage(image: BufferedImage): Color {
        return Color(toBufferedImage(image.getScaledInstance(1, 1, BufferedImage.SCALE_AREA_AVERAGING)).getRGB(0, 0))
    }

    fun getAverageColorOfMaterial(material: Material) = averageColors[material]

    fun getNearestMaterialThatMatchesColor(color: Color): Material {
        if (color.alpha == 0) Material.AIR
        val nearestColor = findNearestColor(color, averageColors.entries) { it.value }
        return nearestColor!!.key // Should NEVER be null (I hope so)
    }

    // Thanks ChatGPT
    private fun euclideanDistance(color1: Color, color2: Color): Double {
        val redDiff = (color1.red - color2.red).toDouble()
        val greenDiff = (color1.green - color2.green).toDouble()
        val blueDiff = (color1.blue - color2.blue).toDouble()
        return Math.sqrt(redDiff * redDiff + greenDiff * greenDiff + blueDiff * blueDiff)
    }

    private fun <T> findNearestColor(target: Color, colors: Collection<T>, colorProvider: (T) -> (Color)): T? {
        return colors.minByOrNull {
            val color = colorProvider.invoke(it)
            euclideanDistance(color, target)
        }
    }

    /**
     * Converts a given Image into a BufferedImage
     *
     * @param img The Image to be converted
     * @return The converted BufferedImage
     */
    private fun toBufferedImage(img: Image): BufferedImage {
        if (img is BufferedImage) {
            return img
        }

        // Create a buffered image with transparency
        val bimage = BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB)

        // Draw the image on to the buffered image
        val bGr = bimage.createGraphics()
        bGr.drawImage(img, 0, 0, null)
        bGr.dispose()

        // Return the buffered image
        return bimage
    }
}