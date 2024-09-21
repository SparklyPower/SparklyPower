package net.perfectdreams.dreamsocial.gui.profile

import com.gmail.nossr50.datatypes.skills.PrimarySkillType
import com.gmail.nossr50.mcMMO
import com.gmail.nossr50.util.skills.SkillTools
import com.mojang.authlib.GameProfile
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.luckperms.api.LuckPermsProvider
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcasamentos.dao.Marriage
import net.perfectdreams.dreamcasamentos.tables.Marriages
import net.perfectdreams.dreamcash.utils.Cash
import net.perfectdreams.dreamclubes.utils.ClubeAPI
import net.perfectdreams.dreamclubes.utils.ClubePermissionLevel
import net.perfectdreams.dreamcore.dao.Ban
import net.perfectdreams.dreamcore.tables.Bans
import net.perfectdreams.dreamcore.tables.EventVictories
import net.perfectdreams.dreamcore.utils.adventure.append
import net.perfectdreams.dreamcore.utils.adventure.lore
import net.perfectdreams.dreamcore.utils.extensions.*
import net.perfectdreams.dreamloja.dao.Shop
import net.perfectdreams.dreamloja.dao.UserShopVote
import net.perfectdreams.dreamloja.tables.Shops
import net.perfectdreams.dreamloja.tables.UserShopVotes
import net.perfectdreams.dreamraffle.dao.Gambler
import net.perfectdreams.dreamsocial.DreamSocial
import net.perfectdreams.dreamsocial.dao.ProfileEntity
import net.perfectdreams.dreamsocial.gui.profile.helper.*
import net.perfectdreams.dreamsocial.gui.profile.helper.item.getPlayerAnniversaryCustomModelData
import net.perfectdreams.dreamsocial.gui.profile.settings.renderSettingsMenu
import net.perfectdreams.dreamvanish.DreamVanishAPI
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Statistic
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

private val brazilZoneId = ZoneId.of("America/Sao_Paulo")
private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/YYYY")
private val luckPermsApi = LuckPermsProvider.get()

fun renderProfileMenu(plugin: DreamSocial, targetUUID: UUID, profileLayout: ProfileLayout, isCheckingSelf: Boolean) =
    createMenu(54, profileLayout.menuTitle) {
        val offlinePlayer = Bukkit.getOfflinePlayer(targetUUID)

        // Hacky workaround: While setting the skull owner, a NullPointerException was thrown
        // Because the `.playerProfile` cannot guarantee that the `name` is not null, and we need that, even if is an empty string
        // So I manually created a GameProfile object to set it to the SkullMeta
        val playerProfile = Bukkit.createProfile(targetUUID, offlinePlayer.name ?: " ").apply {
            offlinePlayer.playerProfile.properties.forEach {
                setProperty(it)
            }
        }

        val isGirl = MeninaAPI.isGirl(targetUUID)
        val pronoun = if (isCheckingSelf) "você" else MeninaAPI.getPronome(targetUUID)
        val pronounOrName = if (isCheckingSelf) "Você" else offlinePlayer.name
        val lowercasePronounOrName = if (isCheckingSelf) "você" else offlinePlayer.name

        val article = MeninaAPI.getArtigo(targetUUID)
        val indefiniteArticle = if (isGirl) "uma" else "um"

        /* Player Head */
        slot(5, 1) {
            item = ItemStack(Material.PLAYER_HEAD).meta<SkullMeta> {
                ownerProfile = playerProfile
                setCustomModelData(1)
            }
        }

        /* Login Status */
        val isBanned = transaction(Databases.databaseNetwork) { Ban.find { Bans.player eq targetUUID }.any() }
        val isOffline = !offlinePlayer.isOnline || offlinePlayer.player?.let(DreamVanishAPI::isQueroTrabalhar) ?: false

        slot(7, 3) {
            item = ItemStack(Material.PAPER).meta<ItemMeta> {
                setCustomModelData(if (isBanned) 118 else if (isOffline) 119 else 120)
                displayName(" ".asComponent)
            }
        }

        /* Player Rank */
        val luckPermsUser = luckPermsApi.userManager.loadUser(targetUUID).join()
        val highestNode = luckPermsUser.nodes.maxBy { PlayerRole.getFromPermission(it.key) }
        val highestRole = PlayerRole.getFromPermission(highestNode.key)

        slot(4, 3) {
            item = highestRole.item(isGirl)
        }

        /* Money */
        val money = offlinePlayer.balance.toLong()
        val cash = Cash.getCash(targetUUID)

        slot(0, 5) {
            item = ItemStack(Material.PAPER).meta<ItemMeta> {
                setCustomModelData(90)
                displayName("Dinheiro".asBoldComponent.color { 0x90cf3a })

                lore {
                    textWithoutDecorations {
                        color { 0xa6d962 }
                        append("$pronounOrName tem um total de ")
                        append(money.formatted.asComponent.color { 0xc5e698 })
                    }

                    textWithoutDecorations {
                        color { 0xa6d962 }
                        append(money.pluralize("soneca", false).asComponent.color { 0xc5e698 })
                        append(" e ")
                        append(cash.pluralize("pesadelo").asComponent.color { 0xc5e698 })
                        append(".")
                    }
                }
            }
        }

        /* Gambling */
        val gambler = Gambler.fetch(targetUUID)

        slot(1, 5) {
            item = ItemStack(Material.PAPER).meta<ItemMeta> {
                setCustomModelData(86)
                displayName("Apostas".asBoldComponent.color { 0xb477ff })

                lore {
                    if (gambler == null)
                        textWithoutDecorations {
                            color { 0xcfa8ff }
                            append("$pronounOrName nunca apostou no servidor.")
                        }
                    else {
                        textWithoutDecorations {
                            color { 0xcfa8ff }
                            append(if (isCheckingSelf) "Suas estatísticas:" else "Estatísticas de $pronounOrName:")
                        }

                        emptyLine()

                        textWithoutDecorations {
                            color { 0xcfa8ff }
                            append("Vitórias na rifa: ")
                            append("${gambler.victories}".asComponent.color { 0xe6d2ff })
                        }

                        val sonecasProfit = (gambler.wonSonecas - gambler.spentSonecas).formatted
                        val cashProfit = (gambler.wonCash - gambler.spentCash).formatted

                        textWithoutDecorations {
                            color { 0xcfa8ff }
                            append("Lucro de sonecas na rifa: ")
                            append(sonecasProfit.asComponent.color { 0xe6d2ff })
                        }

                        textWithoutDecorations {
                            color { 0xcfa8ff }
                            append("Lucro de pesadelos na rifa: ")
                            append(cashProfit.asComponent.color { 0xe6d2ff })
                        }
                    }
                }
            }
        }

        /* Clan */
        val clan = ClubeAPI.getPlayerClube(targetUUID)

        slot(3, 5) {
            item = ItemStack(Material.PAPER).meta<ItemMeta> {
                setCustomModelData(83)
                displayName("Clube".asBoldComponent.color { 0x58d2ff })

                lore {
                    if (clan == null)
                        textWithoutDecorations {
                            color { 0xade9ff }
                            append("$pronounOrName não está em um clube.")
                        }
                    else {
                        val memberRole = when (clan.retrieveMember(targetUUID)!!.permissionLevel) {
                            ClubePermissionLevel.OWNER -> "$article líder"
                            ClubePermissionLevel.ADMIN -> "$indefiniteArticle admin"
                            ClubePermissionLevel.MEMBER -> "$indefiniteArticle membr$article"
                        }.asComponent.color(NamedTextColor.WHITE)

                        textWithoutDecorations {
                            color { 0xade9ff }
                            append("$pronounOrName é ")
                            append(memberRole)
                            append(" do clube ")
                            append(clan.cleanName.asComponent.color(NamedTextColor.WHITE))
                            append(".")
                        }

                        textWithoutDecorations {
                            color { 0xade9ff }
                            append("Este clube tem um total de ")
                            append(clan.retrieveMembers().size.pluralize("membro").asComponent.color(NamedTextColor.WHITE))
                            append(".")
                        }
                    }
                }
            }
        }

        /* KDR */
        val kdr = runBlocking { ClubeAPI.getPlayerKD(targetUUID) }

        slot(4, 5) {
            item = ItemStack(Material.PAPER).meta<ItemMeta> {
                setCustomModelData(87)
                displayName("KDR".asBoldComponent.color { 0xff5410 })

                lore {
                    if (kdr.deaths == 0L && kdr.kills == 0L) {
                        textWithoutDecorations {
                            color { 0xff7c48 }
                            append("$pronounOrName nunca lutou PvP no servidor.")
                        }

                        if (isCheckingSelf)
                            textWithoutDecorations {
                                color { 0xff7c48 }
                                append("Aumente seu KDR na ")
                                append("/warp pvp".asComponent.color { 0xffbfa5 })
                                append("!")
                            }
                    } else {
                        val kills = kdr.kills.pluralize("jogador" to "jogadores").asComponent.color { 0xffbfa5 }
                        val deaths = kdr.deaths.pluralize("vez" to "vezes").asComponent.color { 0xffbfa5 }

                        textWithoutDecorations {
                            color { 0xff7c48 }
                            append("$pronounOrName já matou ")
                            append(kills)
                            append(" e")
                        }

                        textWithoutDecorations {
                            color { 0xff7c48 }
                            append("morreu ")
                            append(deaths)
                            append(".")
                        }

                        emptyLine()

                        textWithoutDecorations {
                            color { 0xff7c48 }
                            append("${pronoun.replaceFirstChar(Char::uppercase)} tem um KDR de ")
                            append("${kdr.getRatio()}".asComponent.color { 0xffbfa5 })
                            append("!")
                        }
                    }
                }
            }
        }

        /* Events */
        val now = getZonedDate()
        val start = now.startOfTheMonthInMillis

        val overallVictories = transaction(Databases.databaseNetwork) {
            EventVictories.select {
                (EventVictories.user eq targetUUID) and (EventVictories.event neq "Chat")
            }.count()
        }

        val thisMonthVictories = transaction(Databases.databaseNetwork) {
            EventVictories.select {
                (EventVictories.wonAt greaterEq start) and (EventVictories.user eq targetUUID) and (EventVictories.event neq "Chat")
            }.count()
        }

        slot(6, 5) {
            item = ItemStack(Material.PAPER).meta<ItemMeta> {
                setCustomModelData(85)
                displayName("Eventos".asBoldComponent.color { 0xe8e345 })

                lore {
                    if (overallVictories == 0L)
                        textWithoutDecorations {
                            color { 0xf1ed8c }
                            append("$pronounOrName nunca venceu um evento no servidor.")
                        }
                    else {
                        textWithoutDecorations {
                            color { 0xf1ed8c }
                            append("Ao todo, $lowercasePronounOrName já venceu ")
                            append(overallVictories.pluralize("evento").asComponent.color { 0xf7f5c1 })
                            append(".")
                        }

                        if (thisMonthVictories > 0) {
                            emptyLine()

                            textWithoutDecorations {
                                color { 0xf1ed8c }
                                append("E, neste mês de ")
                                append(now.localizedMonth.asComponent.color { 0xf7f5c1 })
                                append(", $pronoun venceu ")
                            }

                            textWithoutDecorations {
                                color { 0xf1ed8c }
                                append(thisMonthVictories.pluralize("evento").asComponent.color { 0xf7f5c1 })
                                append("!")
                            }
                        }

                        emptyLine()

                        textWithoutDecorations {
                            color { 0xf1ed8c }
                            append("Nota: vitórias do evento chat não são")
                        }

                        textWithoutDecorations {
                            color { 0xf1ed8c }
                            append("consideradas.")
                        }
                    }
                }
            }
        }

        /* mcMMO */
        val mcMMOPlayerProfile = mcMMO.getDatabaseManager().loadPlayerProfile(targetUUID)
        var powerLevel = 0

        val skillCount = PrimarySkillType.entries.size
        // Because of the display name AND title AND the subtitle (fuck me I spent so much time looking at this)
        val skillCountPlusTwo = skillCount + 3

        val colors = Array(skillCountPlusTwo) {
            val c = Color.getHSBColor(it.toFloat() / skillCount, .4F, .95F)
            TextColor.color(c.red, c.blue, c.green)
        }.toMutableList()

        slot(7, 5) {
            item = ItemStack(Material.PAPER).meta<ItemMeta> {
                setCustomModelData(89)
                displayName("mcMMO".asBoldComponent.color(colors.removeFirst()))

                lore {
                    textWithoutDecorations {
                        color(colors.removeFirst())
                        append(if (isCheckingSelf) "Seus níveis de mcMMO:" else "Níveis de mcMMO de $pronounOrName:" )
                    }

                    emptyLine()

                    alphabeticallySortedList.forEach {
                        val skillLevel = mcMMOPlayerProfile.getSkillLevel(it)

                        textWithoutDecorations {
                            color(colors.removeFirst())
                            append("⤖ ${it.localizedName}: $skillLevel")
                        }

                        if (!SkillTools.isChildSkill(it)) powerLevel += skillLevel
                    }

                    emptyLine()

                    textWithoutDecorations {
                        color(colors.removeFirst())
                        append("Nível total: ${powerLevel.formatted}")
                    }
                }
            }
        }

        /* Optional information */
        val slots = mutableListOf(0 to 1, 1 to 1, 2 to 1, 0 to 2)
        val optionalItems = mutableListOf<DreamMenuSlotBuilder.() -> Unit>()

        val emptyPanel: DreamMenuSlotBuilder.() -> Unit = {
            item = profileLayout.panelItem
        }

        /* Marriage */
        val marriage = transaction(Databases.databaseNetwork) {
            Marriage.find {
                (Marriages.player1 eq targetUUID) or (Marriages.player2 eq targetUUID)
            }.firstOrNull()
        }

        if (marriage != null) {
            if (marriage.marriedAt == null)
                transaction(Databases.databaseNetwork) {
                    marriage.marriedAt = System.currentTimeMillis()
                }

            optionalItems.add {
                item = ItemStack(Material.PAPER).meta<ItemMeta> {
                    setCustomModelData(88)
                    displayName("Casamento".asBoldComponent.color { 0x61d2cd })

                    lore {
                        textWithoutDecorations {
                            val partnerName = Bukkit.getOfflinePlayer(marriage.getPartnerOf(targetUUID)).name ?: "???"

                            color { 0x9ae2df }
                            append("$pronounOrName está casad$article com ")
                            append(partnerName.asComponent.color { 0xd2f2f1 })
                        }

                        textWithoutDecorations {
                            val date = dateFormatter.format(Instant.ofEpochMilli(marriage.marriedAt!!).atZone(brazilZoneId))

                            color { 0x9ae2df }
                            append("desde ")
                            append(date.asComponent.color { 0xd2f2f1 })
                            append("!")
                        }
                    }
                }
            }
        }

        /* Player's Anniversary */
        val firstLogin = offlinePlayer.firstPlayed
        val today = System.currentTimeMillis()

        val numberOfYears = (today - firstLogin) / 31_556_926_000

        optionalItems.add {
            item = ItemStack(Material.PAPER).meta<ItemMeta> {
                setCustomModelData(getPlayerAnniversaryCustomModelData(numberOfYears))
                displayName("Primeiro login".asBoldComponent.color { 0xffc000 })

                lore {
                    textWithoutDecorations {
                        val date = dateFormatter.format(Instant.ofEpochMilli(firstLogin).atZone(brazilZoneId))

                        color { 0xffd44e }
                        append("$pronounOrName logou pela primeira vez em ")
                        append(date.asComponent.color { 0xffe79d })
                        append(".")
                    }

                    emptyLine()

                    textWithoutDecorations {
                        color { 0xffd44e }
                        append("Durante toda sua estadia conosco, $pronoun esteve")
                    }

                    textWithoutDecorations {
                        val onlineTime = (offlinePlayer.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20).toLong()

                        color { 0xffd44e }
                        append("online por ")
                        append(generateOnlineTimeMessage(onlineTime))
                    }

                    if (numberOfYears > 0) {
                        emptyLine()

                        textWithoutDecorations {
                            color { 0xffd44e }
                            append("Além disso, sabia que $pronoun joga há ")
                            append(numberOfYears.pluralize("ano").asComponent.color { 0xffe79d })
                            append("?")
                        }
                    }
                }
            }
        }

        /* Shop */
        val shops = transaction(Databases.databaseNetwork) { Shop.find { Shops.owner eq targetUUID }.count() }

        if (shops > 0) {
            val shopVotes = transaction(Databases.databaseNetwork) {
                UserShopVote.find { UserShopVotes.receivedBy eq targetUUID }.count()
            }

            optionalItems.add {
                item = ItemStack(Material.PAPER).meta<ItemMeta> {
                    setCustomModelData(128)
                    displayName("Loja".asBoldComponent.color { 0xa2b0b6 })

                    lore {
                        textWithoutDecorations {
                            color { 0xb7c2c7 }
                            append("$pronounOrName tem ")
                            append(shops.pluralize("loja").asComponent.color { 0xe2e7e8 })
                            append(". Por enquanto,")
                        }

                        textWithoutDecorations {
                            val theyGot = shops.pluralize("ela já recebeu" to "elas já receberam", false)

                            color { 0xb7c2c7 }
                            append("$theyGot ")
                            append(shopVotes.pluralize("voto").asComponent.color { 0xe2e7e8 })
                            append(".")
                        }

                        emptyLine()

                        textWithoutDecorations {
                            color { 0xb7c2c7 }
                            append("Clique aqui para ${shops.pluralize("visitá-la", false)}.")
                        }
                    }
                }

                onClick {
                    it.closeInventory()
                    (it as Player).performCommand("loja ${offlinePlayer.name}")
                }
            }
        }

        /* Settings */
        val settings: DreamMenuSlotBuilder.() -> Unit = {
            item = ItemStack(Material.PAPER).meta<ItemMeta> {
                setCustomModelData(84)
                displayName("Configurações".asBoldComponent.color { 0x89898c })

                lore {
                    textWithoutDecorations {
                        color { 0x908f92 }
                        append("Clique para acessar as suas ")
                        append("preferências").color { 0xd8d8d9 }
                    }

                    textWithoutDecorations {
                        color { 0x908f92 }
                        append("ou editar o ")
                        append("layout do seu perfil").color { 0xd8d8d9 }
                        append(".")
                    }
                }
            }

            onClick {
                renderSettingsMenu(plugin, offlinePlayer.player!!).sendTo(it as Player)
            }
        }

        /* Final Touches */
        if (optionalItems.isEmpty() && isCheckingSelf) {
            val coordinates = slots.removeFirst()
            slot(coordinates.first, coordinates.second, settings)
        } else {
            optionalItems.forEach {
                val coordinates = slots.removeFirst()
                slot(coordinates.first, coordinates.second, it)
            }

            if (isCheckingSelf) {
                val coordinates = slots.removeLast()
                slot(coordinates.first, coordinates.second, settings)
            }
        }

        slots.forEach {
            slot(it.first, it.second, emptyPanel)
        }
    }