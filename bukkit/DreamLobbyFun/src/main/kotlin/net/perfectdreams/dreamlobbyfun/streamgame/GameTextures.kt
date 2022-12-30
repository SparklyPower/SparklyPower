package net.perfectdreams.dreamlobbyfun.streamgame

import net.perfectdreams.dreamlobbyfun.DreamLobbyFun
import java.io.File
import javax.imageio.ImageIO

class GameTextures(val m: DreamLobbyFun) {
    val lorittaHurtTexture = ImageIO.read(File(m.dataFolder, "lori_sprites/dano.png"))
    val lorittaIdleTexture = ImageIO.read(File(m.dataFolder, "lori_sprites/repouso.png"))
    val lorittaJumpingTexture = ImageIO.read(File(m.dataFolder, "lori_sprites/pulo2.png"))
    val lorittaRunningTextures = listOf(
        ImageIO.read(File(m.dataFolder, "lori_sprites/corrida1.png")),
        ImageIO.read(File(m.dataFolder, "lori_sprites/corrida2.png")),
        ImageIO.read(File(m.dataFolder, "lori_sprites/corrida3.png")),
        ImageIO.read(File(m.dataFolder, "lori_sprites/corrida4.png")),
        ImageIO.read(File(m.dataFolder, "lori_sprites/corrida5.png")),
        ImageIO.read(File(m.dataFolder, "lori_sprites/corrida6.png"))
    )
    val lorittaDeadTexture = ImageIO.read(File(m.dataFolder, "lori_sprites/morte.png"))

    val pantufaHurtTexture = ImageIO.read(File(m.dataFolder, "pantufa_sprites/dano.png"))
    val pantufaIdleTexture = ImageIO.read(File(m.dataFolder, "pantufa_sprites/repouso.png"))
    val pantufaJumpingTexture = ImageIO.read(File(m.dataFolder, "pantufa_sprites/pulo2.png"))
    val pantufaRunningTextures = listOf(
        ImageIO.read(File(m.dataFolder, "pantufa_sprites/corrida1.png")),
        ImageIO.read(File(m.dataFolder, "pantufa_sprites/corrida2.png")),
        ImageIO.read(File(m.dataFolder, "pantufa_sprites/corrida3.png")),
        ImageIO.read(File(m.dataFolder, "pantufa_sprites/corrida4.png")),
        ImageIO.read(File(m.dataFolder, "pantufa_sprites/corrida5.png")),
        ImageIO.read(File(m.dataFolder, "pantufa_sprites/corrida6.png"))
    )

    val gabrielaHurtTexture = ImageIO.read(File(m.dataFolder, "gabi_sprites/dano.png"))
    val gabrielaIdleTexture = ImageIO.read(File(m.dataFolder, "gabi_sprites/repouso.png"))
    val gabrielaJumpingTexture = ImageIO.read(File(m.dataFolder, "gabi_sprites/pulo2.png"))
    val gabrielaRunningTextures = listOf(
        ImageIO.read(File(m.dataFolder, "gabi_sprites/corrida1.png")),
        ImageIO.read(File(m.dataFolder, "gabi_sprites/corrida2.png")),
        ImageIO.read(File(m.dataFolder, "gabi_sprites/corrida3.png")),
        ImageIO.read(File(m.dataFolder, "gabi_sprites/corrida4.png")),
        ImageIO.read(File(m.dataFolder, "gabi_sprites/corrida5.png")),
        ImageIO.read(File(m.dataFolder, "gabi_sprites/corrida6.png"))
    )
}