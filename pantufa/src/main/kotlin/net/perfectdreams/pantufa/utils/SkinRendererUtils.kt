package net.perfectdreams.pantufa.utils

import java.awt.image.BufferedImage

object SkinRendererUtils {
    fun createStatueOfSkin(playerSkinImage: BufferedImage, isSlim: Boolean): BufferedImage {
        // "Old skins" are skins that are in the pre-1.8 format
        val isOldSkin = playerSkinImage.width == 64 && playerSkinImage.height == 32
        if (isOldSkin && isSlim)
            error("A skin cannot be old skin and slim at the same time!")

        // Render a small statue
        val skinStatueImage = BufferedImage(16, 32, BufferedImage.TYPE_INT_ARGB)
        val skinStatueGraphics = skinStatueImage.createGraphics()

        if (isOldSkin) {
            // HEAD
            skinStatueGraphics.drawImage(
                playerSkinImage.getSubimage(8, 8, 8, 8),
                4,
                0,
                null
            )

            // HEAD OVERLAY
            skinStatueGraphics.drawImage(
                playerSkinImage.getSubimage(40, 8, 8, 8),
                4,
                0,
                null
            )

            // BODY
            skinStatueGraphics.drawImage(
                playerSkinImage.getSubimage(20, 20, 8, 12),
                4,
                8,
                null
            )

            // LEFT ARM
            skinStatueGraphics.drawImage(
                playerSkinImage.getSubimage(44, 20, 4, 12),
                0,
                8,
                null
            )

            // RIGHT ARM
            skinStatueGraphics.drawImage(
                playerSkinImage.getSubimage(44, 20, 4, 12),
                12,
                8,
                null
            )

            // LEFT LEG
            skinStatueGraphics.drawImage(
                playerSkinImage.getSubimage(4, 20, 4, 12),
                4,
                20,
                null
            )

            // RIGHT LEG
            skinStatueGraphics.drawImage(
                playerSkinImage.getSubimage(4, 20, 4, 12),
                8,
                20,
                null
            )
        } else {
            // HEAD
            skinStatueGraphics.drawImage(
                playerSkinImage.getSubimage(8, 8, 8, 8),
                4,
                0,
                null
            )

            // HEAD OVERLAY
            skinStatueGraphics.drawImage(
                playerSkinImage.getSubimage(40, 8, 8, 8),
                4,
                0,
                null
            )

            // BODY
            skinStatueGraphics.drawImage(
                playerSkinImage.getSubimage(20, 20, 8, 12),
                4,
                8,
                null
            )

            // BODY OVERLAY
            skinStatueGraphics.drawImage(
                playerSkinImage.getSubimage(20, 36, 8, 12),
                4,
                8,
                null
            )

            if (isSlim) {
                // LEFT ARM
                skinStatueGraphics.drawImage(
                    playerSkinImage.getSubimage(36, 52, 3, 12),
                    1,
                    8,
                    null
                )

                // LEFT ARM OVERLAY
                skinStatueGraphics.drawImage(
                    playerSkinImage.getSubimage(52, 52, 3, 12),
                    1,
                    8,
                    null
                )

                // RIGHT ARM
                skinStatueGraphics.drawImage(
                    playerSkinImage.getSubimage(44, 20, 3, 12),
                    12,
                    8,
                    null
                )

                // RIGHT ARM OVERLAY
                skinStatueGraphics.drawImage(
                    playerSkinImage.getSubimage(44, 36, 3, 12),
                    12,
                    8,
                    null
                )
            } else {
                // LEFT ARM
                skinStatueGraphics.drawImage(
                    playerSkinImage.getSubimage(36, 52, 4, 12),
                    0,
                    8,
                    null
                )

                // LEFT ARM OVERLAY
                skinStatueGraphics.drawImage(
                    playerSkinImage.getSubimage(52, 52, 4, 12),
                    0,
                    8,
                    null
                )

                // RIGHT ARM
                skinStatueGraphics.drawImage(
                    playerSkinImage.getSubimage(44, 20, 4, 12),
                    12,
                    8,
                    null
                )

                // RIGHT ARM OVERLAY
                skinStatueGraphics.drawImage(
                    playerSkinImage.getSubimage(44, 36, 4, 12),
                    12,
                    8,
                    null
                )
            }

            // LEFT LEG
            skinStatueGraphics.drawImage(
                playerSkinImage.getSubimage(20, 52, 4, 12),
                4,
                20,
                null
            )

            // LEFT LEG OVERLAY
            skinStatueGraphics.drawImage(
                playerSkinImage.getSubimage(4, 52, 4, 12),
                4,
                20,
                null
            )

            // RIGHT LEG
            skinStatueGraphics.drawImage(
                playerSkinImage.getSubimage(4, 20, 4, 12),
                8,
                20,
                null
            )

            // RIGHT LEG OVERLAY
            skinStatueGraphics.drawImage(
                playerSkinImage.getSubimage(4, 36, 4, 12),
                8,
                20,
                null
            )
        }

        return skinStatueImage
    }
}