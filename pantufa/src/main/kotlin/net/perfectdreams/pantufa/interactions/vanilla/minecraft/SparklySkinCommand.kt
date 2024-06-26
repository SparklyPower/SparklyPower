package net.perfectdreams.pantufa.interactions.vanilla.minecraft

import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.messages.MessageCreate
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import mu.KotlinLogging
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.utils.FileUpload
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.api.commands.styled
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.Emotes
import net.perfectdreams.pantufa.utils.ImageUtils
import net.perfectdreams.pantufa.utils.ImageUtils.clone
import net.perfectdreams.pantufa.utils.ImageUtils.getResizedInstance
import net.perfectdreams.pantufa.utils.SkinRendererUtils
import net.perfectdreams.pantufa.utils.extensions.await
import net.perfectdreams.pantufa.utils.extensions.toJDA
import net.sparklypower.rpc.UpdatePlayerSkinRequest
import net.sparklypower.rpc.UpdatePlayerSkinResponse
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

class SparklySkinCommand(val m: PantufaBot) : SlashCommandDeclarationWrapper {
    override fun command() = slashCommand("sparklyskin", "Altera a sua skin no SparklyPower", CommandCategory.MINECRAFT) {
        requireMinecraftAccount = true

        executor = SparklySkinExecutor(m)
    }

    class SparklySkinExecutor(val m: PantufaBot) : LorittaSlashCommandExecutor() {
        companion object {
            private val logger = KotlinLogging.logger {}
        }

        private val mineSkinRequestMutex = Mutex()
        private var mineSkinRatelimitHeaders: MineSkinRatelimitHeaders? = null

        inner class Options : ApplicationCommandOptions() {
            val image = attachment("image", "A imagem da skin que você deseja colocar na sua conta")
            val variant = string("variant", "A variante do modelo de braço que você quer") {
                choice("Auto-detectar", "AUTO_DETECT")
                choice("Braço Largo", ModelVariant.CLASSIC.name)
                choice("Braço Fino", ModelVariant.SLIM.name)
            }
            val sweatshirtStyle = string(
                "sweatshirt_style",
                "O moletom da Loritta é tão fofinho... e agora você pode ter ele na sua skin!"
            ) {
                choice("Não colocar o moletom da Loritta", "AUTO_DETECT")
                choice("Padrão (Claro)", SweatshirtStyle.LIGHT.name)
                choice("Padrão (Escuro)", SweatshirtStyle.DARK.name)
                choice("Misto (Onda)", SweatshirtStyle.MIX_WAVY.name)
                choice("Misto (Onda com Pontos)", SweatshirtStyle.MIX_WAVY_WITH_STITCHES.name)
                choice("Misto (Vertical)", SweatshirtStyle.MIX_VERTICAL.name)
                choice("Misto (Vertical com Pontos)", SweatshirtStyle.MIX_VERTICAL_WITH_STITCHES.name)
            }
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val attachment = args[options.image]
            val userProvidedVariant =
                args[options.variant].let { if (it == "AUTO_DETECT") null else ModelVariant.valueOf(it) }
            val sweatshirtStyle =
                args[options.sweatshirtStyle].let { if (it == "AUTO_DETECT") null else SweatshirtStyle.valueOf(it) }

            context.deferChannelMessage(true)

            val account = context.retrieveConnectedMinecraftAccountOrFail()

            // First we need to validate if the player sent a valid Minecraft skin
            val image = ImageUtils.downloadImage(attachment.url)
            if (image == null) {
                context.reply(true) {
                    styled(
                        "Imagem da skin inválida!",
                        Constants.ERROR
                    )
                }
                return
            }

            val isValidNewStyleSkin = image.width == 64 && image.height == 64
            val isValidOldStyleSkin = image.width == 64 || image.height == 32
            val isValidSkin = isValidNewStyleSkin || isValidOldStyleSkin
            if (!isValidSkin) {
                context.reply(true) {
                    styled(
                        "Skin inválida! Uma skin válida precisa ter uma dimensão de 64x64 (skins 1.8+) ou uma dimensão de 64x32 (skins antes da 1.8)",
                        Constants.ERROR
                    )

                    styled(
                        "Para te ajudar a entender melhor, aqui está o arquivo da minha skin!"
                    )

                    files += FileUpload.fromData(
                        PantufaBot::class.java.getResourceAsStream("/skins/pantufa.png"),
                        "skin.png"
                    )
                }
                return
            }

            val variant = if (userProvidedVariant != null)
                userProvidedVariant
            else {
                val alexTestColor = Color(image.getRGB(50, 16), true)
                val isSlim = alexTestColor.alpha != 255
                if (isSlim)
                    ModelVariant.SLIM
                else
                    ModelVariant.CLASSIC
            }

            val skinImage = BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB) // Fixes paletted skins (Like Notch's skin)
            val skinImageGraphics = skinImage.createGraphics()
            skinImageGraphics.drawImage(image, 0, 0, null)

            if (isValidOldStyleSkin) {
                // Converts a skin to 1.8 format
                // This is *technically* not needed if we aren't editing the user's sweatshirt, but oh well...
                fun flipAndPaste(bufferedImage: BufferedImage, x: Int, y: Int) {
                    skinImageGraphics.drawImage(
                        bufferedImage,
                        x + bufferedImage.width,
                        y,
                        -bufferedImage.width,
                        bufferedImage.height,
                        null
                    )
                }

                // i have no idea what I'm doing
                val leg0 = skinImage.getSubimage(0, 16, 16, 4).clone()
                val leg1 = skinImage.getSubimage(4, 20, 8, 12).clone()
                val leg2 = skinImage.getSubimage(0, 20, 4, 12).clone()
                val leg3 = skinImage.getSubimage(12, 20, 4, 12).clone()

                val arm0 = skinImage.getSubimage(40, 16, 16, 4).clone()
                val arm1 = skinImage.getSubimage(4 + 40, 20, 8, 12).clone()
                val arm2 = skinImage.getSubimage(0 + 40, 20, 4, 12).clone()
                val arm3 = skinImage.getSubimage(12 + 40, 20, 4, 12).clone()

                skinImageGraphics.drawImage(leg0, 16, 48, null)
                flipAndPaste(leg1, 16, 52)
                flipAndPaste(leg2, 24, 52)
                flipAndPaste(leg3, 28, 52)

                skinImageGraphics.drawImage(arm0, 32, 48, null)
                flipAndPaste(arm1, 32, 52)
                flipAndPaste(arm2, 40, 52)
                flipAndPaste(arm3, 44, 52)
            }

            if (sweatshirtStyle != null) {
                // This code is from Gabriela's Image Server!
                val templateFileName = if (variant == ModelVariant.SLIM)
                    sweatshirtStyle.slimFileName
                else
                    sweatshirtStyle.classicFileName

                val template = ImageIO.read(PantufaBot::class.java.getResourceAsStream("/loritta_sweatshirt_skin_templates/$templateFileName.png"))

                // This does... something?
                // Sorry, I'm not really sure what this is for, I tried remembering what it does but I just *can't* figure out
                // it seems it tries to fix the skin's hand for some reason...?
                val handColor = if (variant == ModelVariant.SLIM) {
                    Color(skinImage.getRGB(48, 17), true)
                } else {
                    Color(skinImage.getRGB(49, 17), true)
                }

                var wrong = 0
                for (x in 40..43) {
                    val color = Color(skinImage.getRGB(x, 31))
                    if (handColor.red in color.red - 40 until color.red + 40) {
                        if (handColor.green in color.green - 40 until color.green + 40) {
                            if (handColor.blue in color.blue - 40 until color.blue + 40) {
                                continue
                            }
                        }
                    }

                    wrong++
                }
                val lowerBarWrong = wrong > 2

                for (x in 40..43) {
                    val color = Color(skinImage.getRGB(x, 30))
                    if (handColor.red in color.red - 40 until color.red + 40) {
                        if (handColor.green in color.green - 40 until color.green + 40) {
                            if (handColor.blue in color.blue - 40 until color.blue + 40) {
                                continue
                            }
                        }
                    }

                    wrong++
                }

                if (wrong > 2) {
                    skinImageGraphics.color = handColor
                    val arm1 = skinImage.getSubimage(40, 31, if (variant == ModelVariant.SLIM) 14 else 16, 1).clone()
                    val arm2 = skinImage.getSubimage(32, 63, if (variant == ModelVariant.SLIM) 14 else 16, 1).clone()

                    // ARMS
                    skinImageGraphics.fillRect(
                        40,
                        30,
                        if (variant == ModelVariant.SLIM) 14 else 16,
                        if (!lowerBarWrong) 1 else 2
                    )
                    skinImageGraphics.fillRect(
                        32,
                        62,
                        if (variant == ModelVariant.SLIM) 14 else 16,
                        if (!lowerBarWrong) 1 else 2
                    )

                    // HANDS
                    if (lowerBarWrong) {
                        skinImageGraphics.fillRect(
                            if (variant == ModelVariant.SLIM) 47 else 48,
                            16,
                            if (variant == ModelVariant.SLIM) 3 else 4,
                            4
                        )
                        skinImageGraphics.fillRect(
                            if (variant == ModelVariant.SLIM) 39 else 40,
                            48,
                            if (variant == ModelVariant.SLIM) 3 else 4,
                            4
                        )
                    } else {
                        // println("Fixing arm by copying lower pixels")
                        skinImageGraphics.drawImage(arm1, 40, 30, null)
                        skinImageGraphics.drawImage(arm2, 32, 62, null)
                    }
                }

                skinImageGraphics.background = Color(255, 255, 255, 0)
                skinImageGraphics.clearRect(16, 32, 48, 16)
                skinImageGraphics.clearRect(48, 48, 16, 16)
                skinImageGraphics.drawImage(template, 0, 0, null)
            }

            val imageAsByteArray = ByteArrayOutputStream().use {
                ImageIO.write(skinImage, "png", it)
                it.toByteArray()
            }

            // Okay, now we can attempt to upload it to MineSkin!
            // While we do have a Discord attachment URL for it, MineSkin rejects the ephemeral attachment for some reason

            // We lock the requests within a mutex to avoid rate limits
            val responseAsJson = mineSkinRequestMutex.withLock {
                val mineSkinRatelimitHeaders = this.mineSkinRatelimitHeaders
                if (mineSkinRatelimitHeaders != null) {
                    // Do we need to wait?
                    if (mineSkinRatelimitHeaders.remaining == 0) {
                        // Yeah, we do!
                        val diff = System.currentTimeMillis() - mineSkinRatelimitHeaders.reset
                        logger.info { "Backing off for ${diff}ms because we are ratelimited!" }
                        delay(diff)
                    }
                }

                val mineSkinResponse = PantufaBot.http.submitFormWithBinaryData(
                    "https://api.mineskin.org/generate/upload",
                    formData = formData {
                        append("name", "Player Uploaded Skin")
                        append("visibility", 1)
                        append(
                            "variant",
                            when (variant) {
                                ModelVariant.CLASSIC -> "classic"
                                ModelVariant.SLIM -> "slim"
                            }
                        )
                        append("file", imageAsByteArray, Headers.build {
                            append(HttpHeaders.ContentType, "image/png")
                            append(HttpHeaders.ContentDisposition, "filename=\"skin.png\"")
                        })
                    }
                ) {
                    userAgent("SparklyPower-Pantufa")
                    header("Authorization", "Bearer ${m.config.mineSkin.apiKey}")
                }

                val responseAsString = mineSkinResponse.bodyAsText()

                if (mineSkinResponse.status != HttpStatusCode.OK) {
                    logger.warn { "Something went wrong while sending skin! Status: ${mineSkinResponse.status}; Body: $responseAsString" }
                    context.reply(true) {
                        styled(
                            "Algo deu errado ao enviar a sua skin!",
                            Constants.ERROR
                        )
                    }
                    return
                }

                this.mineSkinRatelimitHeaders = MineSkinRatelimitHeaders(
                    mineSkinResponse.headers["x-ratelimit-remaining"]!!.toInt(),
                    mineSkinResponse.headers["x-ratelimit-reset"]!!.toLong(),
                )

                return@withLock Json.parseToJsonElement(responseAsString)
            }

            val mineSkinAccountUniqueId = responseAsJson
                .jsonObject["data"]!!
                .jsonObject["uuid"]!!
                .jsonPrimitive
                .content

            val mineSkinTextureValue = responseAsJson
                .jsonObject["data"]!!
                .jsonObject["texture"]!!
                .jsonObject["value"]!!
                .jsonPrimitive
                .content

            val mineSkinTextureSignature = responseAsJson
                .jsonObject["data"]!!
                .jsonObject["texture"]!!
                .jsonObject["signature"]!!
                .jsonPrimitive
                .content

            val skinUpdateResponse = Json.decodeFromString<UpdatePlayerSkinResponse>(
                PantufaBot.http.post("${m.config.sparklyPower.server.sparklyPowerSurvival.apiUrl.removeSuffix("/")}/pantufa/update-player-skin") {
                    setBody(
                        Json.encodeToString(
                            UpdatePlayerSkinRequest(
                                account.uniqueId.toString(),
                                mineSkinAccountUniqueId,
                                mineSkinTextureValue,
                                mineSkinTextureSignature
                            )
                        )
                    )
                }.bodyAsText()
            )

            when (skinUpdateResponse) {
                is UpdatePlayerSkinResponse.Success -> {
                    val skinStatue = SkinRendererUtils.createStatueOfSkin(skinImage, variant == ModelVariant.SLIM)
                    // streeeetch it because it will be blurry if we don't do this
                    val scaledSkinStatue = skinStatue.getResizedInstance(skinStatue.width * 8, skinStatue.height * 8, ImageUtils.InterpolationType.NEAREST_NEIGHBOR)
                    val scaledSkinStatueAsBytes = ByteArrayOutputStream().use {
                        ImageIO.write(scaledSkinStatue, "png", it)
                        it.toByteArray()
                    }

                    context.reply(true) {
                        if (skinUpdateResponse.playerIsOnline) {
                            styled(
                                "Skin alterada! Volte para o Minecraft e veja o seu novo look, amei o novo look amigah!",
                                Emotes.PantufaPickaxe
                            )
                        } else {
                            styled(
                                "Skin alterada! Na próxima vez que você entrar no SparklyPower você já estará com o seu novo look!",
                                Emotes.PantufaPickaxe
                            )
                        }

                        files += FileUpload.fromData(scaledSkinStatueAsBytes, "new_skin_statue.png")
                    }

                    val channel = m.jda.getTextChannelById(m.config.sparklyPower.guild.sparklySkinsLogChannelId) ?: return
                    channel.sendMessage(
                        MessageCreate {
                            styled(
                                "**Skin enviada na Pantufa por:** ${context.user.asMention} [**`${account.username}`** (`${account.uniqueId}`)]"
                            )

                            styled(
                                "**Texture Value:** `$mineSkinTextureValue`"
                            )

                            styled(
                                "**Texture Signature:** `$mineSkinTextureSignature`"
                            )


                            files += FileUpload.fromData(imageAsByteArray, "skin_image.png")
                            files += FileUpload.fromData(scaledSkinStatueAsBytes, "new_skin_statue.png")
                        }
                    ).await()
                }
            }
        }
    }

    enum class ModelVariant {
        CLASSIC,
        SLIM
    }

    enum class SweatshirtStyle(val classicFileName: String, val slimFileName: String) {
        LIGHT("classic_light", "slim_light"),
        DARK("classic_dark", "slim_dark"),
        MIX_WAVY("classic_mix_wavy", "slim_mix_wavy"),
        MIX_WAVY_WITH_STITCHES("classic_mix_wavy_stitches", "slim_mix_wavy_stitches"),
        MIX_VERTICAL("classic_mix_vertical", "slim_mix_vertical"),
        MIX_VERTICAL_WITH_STITCHES("classic_mix_vertical_stitches", "slim_mix_vertical_stitches"),
    }

    class MineSkinRatelimitHeaders(
        val remaining: Int,
        val reset: Long
    )
}