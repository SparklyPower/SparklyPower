package net.perfectdreams.dreamcore.utils.displays.user

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.LocationReference
import net.perfectdreams.dreamcore.utils.adventure.append
import net.perfectdreams.dreamcore.utils.adventure.textComponent
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.commands.options.SuggestsBlock
import net.perfectdreams.dreamcore.utils.displays.DisplayBlock
import org.bukkit.Material
import org.bukkit.entity.Display
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation

class SparklyDisplayCommand(val m: DreamCore) : SparklyCommandDeclarationWrapper {
    companion object {
        val hologramNameAutocomplete: SuggestsBlock = { context, builder ->
            DreamCore.INSTANCE.sparklyUserDisplayManager.createdTextDisplays.forEach {
                builder.suggest(it.value.id)
            }
        }

        inline fun <reified T : DisplayBlock> getDisplayLines(context: CommandContext, hologramData: UserCreatedSparklyDisplay, displayTextLine: String): List<T>? {
            if (displayTextLine == "all")
                return hologramData.sparklyDisplay.blocks.filterIsInstance<T>()
            else {
                val displayLinePosition = displayTextLine.toIntOrNull()
                if (displayLinePosition == null) {
                    context.sendMessage {
                        color(NamedTextColor.RED)
                        content("Linha inválida!")
                    }
                    return null
                }

                val displayLineIndex = displayLinePosition - 1
                val displayLine = hologramData.sparklyDisplay.blocks.getOrNull(displayLineIndex)
                if (displayLine == null) {
                    context.sendMessage {
                        color(NamedTextColor.RED)
                        content("Linha não existe!")
                    }
                    return null
                }

                if (displayLine !is T) {
                    context.sendMessage {
                        color(NamedTextColor.RED)
                        when (T::class) {
                            DisplayBlock.TextDisplayBlock::class -> content("Linha não é um text display!")
                            DisplayBlock.ItemDropDisplayBlock::class -> content("Linha não é um item drop!")
                            else -> error("Unknown display type!")
                        }
                    }
                    return null
                }

                return listOf(displayLine)
            }
        }
    }

    // The hologram and hd aliases are mostly due to "old habits die hard" (HolographicDisplays)
    override fun declaration() = sparklyCommand(listOf("sparklydisplay", "hologram", "hd")) {
        permission = "sparklydisplay.manage"

        subcommand(listOf("create")) {
            executor = SparklyDisplayCreateTextExecutor(m)
        }

        subcommand(listOf("movehere")) {
            executor = SparklyDisplayMoveHereExecutor(m)
        }

        subcommand(listOf("near")) {
            executor = SparklyDisplayNearExecutor(m)
        }

        subcommand(listOf("delete")) {
            executor = SparklyDisplayDeleteExecutor(m)
        }

        subcommand(listOf("relmove")) {
            executor = SparklyDisplayRelativeMovementExecutor(m)
        }

        subcommand(listOf("align")) {
            executor = SparklyDisplayAlignExecutor(m)
        }

        subcommand(listOf("center")) {
            executor = SparklyDisplayCenterExecutor(m)
        }

        subcommand(listOf("lines")) {
            subcommand(listOf("add")) {
                subcommand(listOf("text")) {
                    executor = SparklyDisplayAddTextLinesExecutor(m)
                }

                subcommand(listOf("multiblocktextlines")) {
                    executor = SparklyDisplayAddMultiBlockTextLinesExecutor(m)
                }

                subcommand(listOf("itemdrop")) {
                    executor = SparklyDisplayAddItemLineExecutor(m)
                }
            }

            subcommand(listOf("set")) {
                subcommand(listOf("text")) {
                    executor = SparklyDisplaySetTextLinesExecutor(m)
                }

                subcommand(listOf("itemdrop")) {
                    executor = SparklyDisplaySetItemDropLineExecutor(m)
                }
            }

            subcommand(listOf("setall")) {
                subcommand(listOf("multiblocktextlines")) {
                    executor = SparklyDisplaySetMultiBlockTextLinesExecutor(m)
                }
            }

            subcommand(listOf("edit")) {
                subcommand(listOf("text")) {
                    subcommand(listOf("content")) {
                        executor = SparklyDisplaySetTextLinesContentExecutor(m)
                    }

                    subcommand(listOf("transformation")) {
                        subcommand(listOf("scale")) {
                            executor = SparklyDisplayTransformationScaleExecutor(m)
                        }
                    }

                    subcommand(listOf("billboard")) {
                        executor = SparklyDisplayBillboardExecutor(m)
                    }

                    subcommand(listOf("textshadow")) {
                        executor = SparklyDisplayTextShadowExecutor(m)
                    }

                    subcommand(listOf("backgroundcolor")) {
                        executor = SparklyDisplayBackgroundExecutor(m)
                    }

                    /* subcommand(listOf("transformation")) {
                        subcommand(listOf("leftrotation")) {
                            executor = SparklyDisplayTransformationLeftRotationExecutor(m)
                        }

                        subcommand(listOf("rightrotation")) {
                            executor = SparklyDisplayTransformationRightRotationExecutor(m)
                        }

                        subcommand(listOf("scale")) {
                            executor = SparklyDisplayTransformationScaleExecutor(m)
                        }

                        subcommand(listOf("reset")) {
                            executor = SparklyDisplayTransformationResetExecutor(m)
                        }
                    }
                     */
                }

                subcommand(listOf("item")) {
                    subcommand(listOf("itemstack")) {
                        executor = SparklyDisplaySetItemDropLineItemStackExecutor(m)
                    }
                }
            }

            subcommand(listOf("removeline")) {
                executor = SparklyDisplayRemoveLineExecutor(m)
            }
        }

        subcommand(listOf("save")) {
            executor = SparklyDisplaySaveExecutor(m)
        }

        subcommand(listOf("reload")) {
            executor = SparklyDisplayReloadExecutor(m)
        }
    }

    class SparklyDisplayCreateTextExecutor(val m: DreamCore) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val hologramName = word("hologram_name")

            val text = optionalGreedyString("hologram_text")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val player = context.requirePlayer()

            val hologramName = args[options.hologramName]
            val text = args[options.text] ?: "Olá, eu sou o holograma <aqua>$hologramName</aqua>! :3"

            if (m.sparklyUserDisplayManager.createdTextDisplays.contains(hologramName)) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("Já existe um holograma com este nome!")
                }
                return
            }

            val sparklyDisplay = m.sparklyDisplayManager.spawnDisplay(m, player.location)
            m.sparklyUserDisplayManager.createdTextDisplays[hologramName] = UserCreatedSparklyDisplay(hologramName, sparklyDisplay)
            val newBlock = sparklyDisplay.addDisplayBlock()

            newBlock.text(MiniMessage.miniMessage().deserialize(text))

            context.sendMessage {
                color(NamedTextColor.GREEN)
                content("Holograma criado!")
            }
        }
    }

    class SparklyDisplayAddMultiBlockTextLinesExecutor(val m: DreamCore) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val hologramName = word("hologram_name", hologramNameAutocomplete)

            val text = greedyString("hologram_text")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val hologramName = args[options.hologramName]
            val text = args[options.text]

            val hologramData = m.sparklyUserDisplayManager.createdTextDisplays[hologramName]

            if (hologramData == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("Holograma não existe!")
                }
                return
            }

            for (line in text.replace("{empty}", " ").split("\\n")) {
                val newBlock = hologramData.sparklyDisplay.addDisplayBlock()
                newBlock.text(MiniMessage.miniMessage().deserialize(line))
            }

            context.sendMessage {
                color(NamedTextColor.GREEN)
                content("Holograma editado!")
            }
        }
    }

    class SparklyDisplayAddTextLinesExecutor(val m: DreamCore) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val hologramName = word("hologram_name", hologramNameAutocomplete)

            val text = greedyString("hologram_text")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val hologramName = args[options.hologramName]
            val text = args[options.text]

            val hologramData = m.sparklyUserDisplayManager.createdTextDisplays[hologramName]

            if (hologramData == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("Holograma não existe!")
                }
                return
            }

            val newBlock = hologramData.sparklyDisplay.addDisplayBlock()
            newBlock.text(MiniMessage.miniMessage().deserialize(text.replace("\\n", "\n")))

            context.sendMessage {
                color(NamedTextColor.GREEN)
                content("Holograma editado!")
            }
        }
    }

    class SparklyDisplaySetTextLinesExecutor(val m: DreamCore) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val hologramName = word("hologram_name", hologramNameAutocomplete)
            val displayTextLine = word("display_text_line")

            val text = greedyString("hologram_text")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val hologramName = args[options.hologramName]
            val displayTextLine = args[options.displayTextLine].toInt() - 1
            val text = args[options.text]

            val hologramData = m.sparklyUserDisplayManager.createdTextDisplays[hologramName]

            if (hologramData == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("Holograma não existe!")
                }
                return
            }

            val oldElement = hologramData.sparklyDisplay.blocks.getOrNull(displayTextLine)
            if (oldElement == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("Linha inválida!")
                }
                return
            }

            // Replace it first BEFORE removing it, to avoid replacing the wrong element
            val newBlock = hologramData.sparklyDisplay.createDisplayBlock()
            hologramData.sparklyDisplay.blocks[displayTextLine] = newBlock
            newBlock.text(MiniMessage.miniMessage().deserialize(text.replace("\\n", "\n")))
            oldElement.remove()
            hologramData.sparklyDisplay.synchronizeBlocks()

            context.sendMessage {
                color(NamedTextColor.GREEN)
                content("Holograma editado!")
            }
        }
    }

    class SparklyDisplaySetTextLinesContentExecutor(val m: DreamCore) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val hologramName = word("hologram_name", hologramNameAutocomplete)
            val displayTextLine = word("display_text_line")
            val text = greedyString("hologram_text")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val hologramName = args[options.hologramName]
            val displayTextLine = args[options.displayTextLine]
            val text = args[options.text]

            val hologramData = m.sparklyUserDisplayManager.createdTextDisplays[hologramName]

            if (hologramData == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("Holograma não existe!")
                }
                return
            }

            val displayBlocksToBeEdited = getDisplayLines<DisplayBlock.TextDisplayBlock>(context, hologramData, displayTextLine) ?: return

            displayBlocksToBeEdited.forEach {
                it.text(MiniMessage.miniMessage().deserialize(text.replace("\\n", "\n")))
            }

            context.sendMessage {
                color(NamedTextColor.GREEN)
                content("Holograma editado!")
            }
        }
    }

    class SparklyDisplayAddItemLineExecutor(val m: DreamCore) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val hologramName = word("hologram_name", hologramNameAutocomplete)
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val player = context.requirePlayer()
            val hologramName = args[options.hologramName]

            val hologramData = m.sparklyUserDisplayManager.createdTextDisplays[hologramName]

            if (hologramData == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("Holograma não existe!")
                }
                return
            }

            val itemInMainHand = player.inventory.itemInMainHand
            val holoItem = if (itemInMainHand.type == Material.AIR)
                ItemStack(Material.DIAMOND)
            else
                itemInMainHand

            hologramData.sparklyDisplay.addItemDropDisplayBlock(holoItem)
            hologramData.sparklyDisplay.synchronizeBlocks()

            context.sendMessage {
                color(NamedTextColor.GREEN)
                content("Holograma editado!")
            }
        }
    }

    class SparklyDisplaySetItemDropLineExecutor(val m: DreamCore) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val hologramName = word("hologram_name", hologramNameAutocomplete)
            val displayTextLine = word("display_text_line")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val player = context.requirePlayer()
            val hologramName = args[options.hologramName]
            val displayTextLine = args[options.displayTextLine].toInt() - 1

            val hologramData = m.sparklyUserDisplayManager.createdTextDisplays[hologramName]

            if (hologramData == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("Holograma não existe!")
                }
                return
            }

            val itemInMainHand = player.inventory.itemInMainHand
            val holoItem = if (itemInMainHand.type == Material.AIR)
                ItemStack(Material.DIAMOND)
            else
                itemInMainHand

            val oldElement = hologramData.sparklyDisplay.blocks.getOrNull(displayTextLine)
            if (oldElement == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("Linha inválida!")
                }
                return
            }

            // Replace it first BEFORE removing it, to avoid replacing the wrong element
            val newBlock = hologramData.sparklyDisplay.createItemDropDisplayBlock(holoItem)
            hologramData.sparklyDisplay.blocks[displayTextLine] = newBlock
            oldElement.remove()
            hologramData.sparklyDisplay.synchronizeBlocks()

            context.sendMessage {
                color(NamedTextColor.GREEN)
                content("Holograma editado!")
            }
        }
    }

    class SparklyDisplaySetItemDropLineItemStackExecutor(val m: DreamCore) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val hologramName = word("hologram_name", hologramNameAutocomplete)
            val displayTextLine = word("display_text_line")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val player = context.requirePlayer()
            val hologramName = args[options.hologramName]
            val displayTextLine = args[options.displayTextLine]

            val hologramData = m.sparklyUserDisplayManager.createdTextDisplays[hologramName]

            if (hologramData == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("Holograma não existe!")
                }
                return
            }

            val displayBlocksToBeEdited = getDisplayLines<DisplayBlock.ItemDropDisplayBlock>(context, hologramData, displayTextLine) ?: return

            val itemInMainHand = player.inventory.itemInMainHand
            val holoItem = if (itemInMainHand.type == Material.AIR)
                ItemStack(Material.DIAMOND)
            else
                itemInMainHand

            for (displayBlock in displayBlocksToBeEdited) {
                displayBlock.itemStack = holoItem
            }

            context.sendMessage {
                color(NamedTextColor.GREEN)
                content("Holograma editado!")
            }
        }
    }

    class SparklyDisplayRemoveLineExecutor(val m: DreamCore) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val hologramName = word("hologram_name", hologramNameAutocomplete)
            val displayLine = integer("display_line")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val hologramName = args[options.hologramName]
            val displayLine = args[options.displayLine] - 1
            val hologramData = m.sparklyUserDisplayManager.createdTextDisplays[hologramName]

            if (hologramData == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("Holograma não existe!")
                }
                return
            }

            val displayBlock = hologramData.sparklyDisplay.blocks.getOrNull(displayLine)
            if (displayBlock == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("Linha não existe!")
                }
                return
            }

            displayBlock.remove()

            context.sendMessage {
                color(NamedTextColor.GREEN)
                content("Holograma editado!")

                if (hologramData.sparklyDisplay.blocks.isEmpty()) {
                    appendNewline()
                    append("Cuidado que você criou um SparklyDisplay sem nenhuma linha! Se você quiser deletar o SparklyDisplay, use o comando de deletar")
                }
            }
        }
    }

    class SparklyDisplaySetMultiBlockTextLinesExecutor(val m: DreamCore) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val hologramName = word("hologram_name", hologramNameAutocomplete)

            val text = greedyString("hologram_text")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val hologramName = args[options.hologramName]
            val text = args[options.text]

            val hologramData = m.sparklyUserDisplayManager.createdTextDisplays[hologramName]

            if (hologramData == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("Holograma não existe!")
                }
                return
            }

            hologramData.sparklyDisplay.removeAllBlocks()

            for (line in text.replace("{empty}", " ").split("\\n")) {
                val newBlock = hologramData.sparklyDisplay.addDisplayBlock()
                newBlock.text(MiniMessage.miniMessage().deserialize(line))
            }

            context.sendMessage {
                color(NamedTextColor.GREEN)
                content("Holograma editado!")
            }
        }
    }

    class SparklyDisplayMoveHereExecutor(val m: DreamCore) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val hologramName = word("hologram_name", hologramNameAutocomplete)
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val player = context.requirePlayer()

            val hologramName = args[options.hologramName]

            val hologramData = m.sparklyUserDisplayManager.createdTextDisplays[hologramName]

            if (hologramData == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("Holograma não existe!")
                }
                return
            }

            hologramData.sparklyDisplay.locationReference = LocationReference.fromBukkit(player.location)
            hologramData.sparklyDisplay.synchronizeBlocks()

            context.sendMessage {
                color(NamedTextColor.GREEN)
                content("Holograma teletransportado!")
            }
        }
    }

    class SparklyDisplayBillboardExecutor(val m: DreamCore) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val hologramName = word("hologram_name", hologramNameAutocomplete)
            val displayTextLine = word("display_text_line")

            val billboardType = word(
                "billboard_type"
            ) { context, builder ->
                Display.Billboard.entries.forEach {
                    builder.suggest(it.name.lowercase())
                }
            }
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val hologramName = args[options.hologramName]
            val displayTextLine = args[options.displayTextLine]
            val billboardType = args[options.billboardType]

            val hologramData = m.sparklyUserDisplayManager.createdTextDisplays[hologramName]

            if (hologramData == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("Holograma não existe!")
                }
                return
            }

            val displayBlocksToBeEdited = getDisplayLines<DisplayBlock.TextDisplayBlock>(context, hologramData, displayTextLine) ?: return

            val billboard = Display.Billboard.valueOf(billboardType.uppercase())

            for (displayBlock in displayBlocksToBeEdited) {
                displayBlock.billboard = billboard
            }

            context.sendMessage {
                color(NamedTextColor.GREEN)
                content("Holograma editado!")
            }
        }
    }

    /* class SparklyDisplayTransformationLeftRotationExecutor(val m: DreamCore) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val hologramName = word("hologram_name", hologramNameAutocomplete)

            val x = double("x")
            val y = double("y")
            val z = double("z")
            val w = double("w")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val hologramName = args[options.hologramName]
            val x = args[options.x].toFloat()
            val y = args[options.y].toFloat()
            val z = args[options.z].toFloat()
            val w = args[options.w].toFloat()

            val hologramData = m.sparklyUserDisplayManager.createdTextDisplays[hologramName]

            if (hologramData == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("Holograma não existe!")
                }
                return
            }

            hologramData.transformation = UserCreatedTextDisplayData.Transformation(
                hologramData.transformation.translation,
                UserCreatedTextDisplayData.Quaternionf(
                    x,
                    y,
                    z,
                    w
                ),
                hologramData.transformation.scale,
                hologramData.transformation.rightRotation,
            )

            context.sendMessage {
                color(NamedTextColor.GREEN)
                content("Holograma editado!")
            }
        }
    }

    class SparklyDisplayTransformationRightRotationExecutor(val m: DreamCore) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val hologramName = word("hologram_name", hologramNameAutocomplete)

            val x = double("x")
            val y = double("y")
            val z = double("z")
            val w = double("w")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val hologramName = args[options.hologramName]
            val x = args[options.x].toFloat()
            val y = args[options.y].toFloat()
            val z = args[options.z].toFloat()
            val w = args[options.w].toFloat()

            val hologramData = m.sparklyUserDisplayManager.createdTextDisplays[hologramName]

            if (hologramData == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("Holograma não existe!")
                }
                return
            }

            hologramData.transformation = UserCreatedTextDisplayData.Transformation(
                hologramData.transformation.translation,
                hologramData.transformation.leftRotation,
                hologramData.transformation.scale,
                UserCreatedTextDisplayData.Quaternionf(
                    x,
                    y,
                    z,
                    w
                )
            )

            context.sendMessage {
                color(NamedTextColor.GREEN)
                content("Holograma editado!")
            }
        }
    }
    */

    class SparklyDisplayTransformationScaleExecutor(val m: DreamCore) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val hologramName = word("hologram_name", hologramNameAutocomplete)
            val displayTextLine = word("display_text_line")

            val x = double("x")
            val y = double("y")
            val z = double("z")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val hologramName = args[options.hologramName]
            val displayTextLine = args[options.displayTextLine]
            val x = args[options.x].toFloat()
            val y = args[options.y].toFloat()
            val z = args[options.z].toFloat()

            val hologramData = m.sparklyUserDisplayManager.createdTextDisplays[hologramName]

            if (hologramData == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("Holograma não existe!")
                }
                return
            }

            val displayBlocksToBeEdited = getDisplayLines<DisplayBlock.TextDisplayBlock>(context, hologramData, displayTextLine) ?: return

            for (displayBlock in displayBlocksToBeEdited) {
                displayBlock.transformation = Transformation(
                    displayBlock.transformation.translation,
                    displayBlock.transformation.leftRotation,
                    org.joml.Vector3f(
                        x,
                        y,
                        z
                    ),
                    displayBlock.transformation.rightRotation,
                )
            }

            context.sendMessage {
                color(NamedTextColor.GREEN)
                content("Holograma editado!")
            }
        }
    }

    class SparklyDisplayTransformationResetExecutor(val m: DreamCore) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val hologramName = word("hologram_name", hologramNameAutocomplete)
            val displayTextLine = word("display_text_line")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val hologramName = args[options.hologramName]
            val displayTextLine = args[options.displayTextLine]

            val hologramData = m.sparklyUserDisplayManager.createdTextDisplays[hologramName]

            if (hologramData == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("Holograma não existe!")
                }
                return
            }

            val displayBlocksToBeEdited = getDisplayLines<DisplayBlock.TextDisplayBlock>(context, hologramData, displayTextLine) ?: return

            /* for (displayBlock in displayBlocksToBeEdited) {
                displayBlock.transformation = Transformation(
                    displayBlock.transformation.translation,
                    displayBlock.transformation.leftRotation,
                    org.joml.Vector3f(
                        x,
                        y,
                        z
                    ),
                    displayBlock.transformation.rightRotation,
                )
            } */

            context.sendMessage {
                color(NamedTextColor.GREEN)
                content("Holograma editado!")
            }
        }
    }

    class SparklyDisplayTextShadowExecutor(val m: DreamCore) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val hologramName = word("hologram_name", hologramNameAutocomplete)
            val displayTextLine = word("display_text_line")
            val shadow = boolean("shadow")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val hologramName = args[options.hologramName]
            val displayTextLine = args[options.displayTextLine]
            val shadow = args[options.shadow]

            val hologramData = m.sparklyUserDisplayManager.createdTextDisplays[hologramName]

            if (hologramData == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("Holograma não existe!")
                }
                return
            }

            val displayBlocksToBeEdited = getDisplayLines<DisplayBlock.TextDisplayBlock>(context, hologramData, displayTextLine) ?: return

            for (displayBlock in displayBlocksToBeEdited) {
                displayBlock.isShadowed = shadow
            }

            context.sendMessage {
                color(NamedTextColor.GREEN)
                content("Holograma editado!")
            }
        }
    }

    class SparklyDisplayBackgroundExecutor(val m: DreamCore) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val hologramName = word("hologram_name", hologramNameAutocomplete)
            val displayTextLine = word("display_text_line")

            val r = integer("r")
            val g = integer("g")
            val b = integer("b")
            val a = integer("a")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val hologramName = args[options.hologramName]
            val displayTextLine = args[options.displayTextLine]
            val r = args[options.r]
            val g = args[options.g]
            val b = args[options.b]
            val a = args[options.a]

            val hologramData = m.sparklyUserDisplayManager.createdTextDisplays[hologramName]

            if (hologramData == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("Holograma não existe!")
                }
                return
            }

            val displayBlocksToBeEdited = getDisplayLines<DisplayBlock.TextDisplayBlock>(context, hologramData, displayTextLine) ?: return

            val color = org.bukkit.Color.fromARGB(a, r, g, b)
            for (displayBlock in displayBlocksToBeEdited) {
                displayBlock.backgroundColor = color
            }

            context.sendMessage {
                color(NamedTextColor.GREEN)
                content("Holograma editado!")
            }
        }
    }

    class SparklyDisplayCenterExecutor(val m: DreamCore) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val hologramName = word("hologram_name", hologramNameAutocomplete)
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val hologramName = args[options.hologramName]

            val hologramData = m.sparklyUserDisplayManager.createdTextDisplays[hologramName]

            if (hologramData == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("Holograma não existe!")
                }
                return
            }

            hologramData.sparklyDisplay.locationReference = LocationReference.fromBukkit(
                hologramData.sparklyDisplay
                    .locationReference
                    .toBukkit()
                    .toCenterLocation()
            )


            hologramData.sparklyDisplay.synchronizeBlocks()

            context.sendMessage {
                color(NamedTextColor.GREEN)
                content("Holograma editado!")
            }
        }
    }

    class SparklyDisplayAlignExecutor(val m: DreamCore) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val hologramToAlign = word("hologram_to_align", hologramNameAutocomplete)
            val referenceHologram = word("reference_hologram", hologramNameAutocomplete)
            val alignment = word("alignment") { context, suggests ->
                suggests.suggest("x")
                suggests.suggest("y")
                suggests.suggest("z")
            }
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val hologramToAlign = args[options.hologramToAlign]
            val referenceHologram = args[options.referenceHologram]
            val alignment = args[options.alignment]

            val hologramToAlignData = m.sparklyUserDisplayManager.createdTextDisplays[hologramToAlign]
            val referenceHologramData = m.sparklyUserDisplayManager.createdTextDisplays[referenceHologram]

            if (hologramToAlignData == null || referenceHologramData == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("Holograma não existe!")
                }
                return
            }

            val referenceHologramLocation = referenceHologramData.sparklyDisplay.locationReference.toBukkit()
            val hologramToAlignLocation = hologramToAlignData.sparklyDisplay.locationReference.toBukkit()

            hologramToAlignData.sparklyDisplay.locationReference = LocationReference.fromBukkit(
                    hologramToAlignLocation.clone().apply {
                        for (char in alignment) {
                            if (char == 'x')
                                x = referenceHologramLocation.x
                            if (char == 'y')
                                y = referenceHologramLocation.y
                            if (char == 'z')
                                z = referenceHologramLocation.z
                        }
                    }
                    )
            hologramToAlignData.sparklyDisplay.synchronizeBlocks()

            context.sendMessage {
                color(NamedTextColor.GREEN)
                content("Holograma editado!")
            }
        }
    }

    class SparklyDisplayRelativeMovementExecutor(val m: DreamCore) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val hologramToMove = word("hologram_to_move", hologramNameAutocomplete)
            val axis = word("axis") { context, suggests ->
                suggests.suggest("x")
                suggests.suggest("y")
                suggests.suggest("z")
            }
            val quantity = double("quantity")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val hologramToMove = args[options.hologramToMove]
            val axis = args[options.axis]
            val quantity = args[options.quantity]

            val hologramToMoveData = m.sparklyUserDisplayManager.createdTextDisplays[hologramToMove]

            if (hologramToMoveData == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("Holograma não existe!")
                }
                return
            }

            hologramToMoveData.sparklyDisplay.locationReference = LocationReference.fromBukkit(
                    hologramToMoveData.sparklyDisplay.locationReference.toBukkit().apply {
                        for (char in axis) {
                            if (char == 'x')
                                x += quantity
                            if (char == 'y')
                                y += quantity
                            if (char == 'z')
                                z += quantity
                        }
                    }
                    )
            hologramToMoveData.sparklyDisplay.synchronizeBlocks()

            context.sendMessage {
                color(NamedTextColor.GREEN)
                content("Holograma editado!")
            }
        }
    }

    class SparklyDisplayDeleteExecutor(val m: DreamCore) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val hologramName = word("hologram_name", hologramNameAutocomplete)
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val hologramName = args[options.hologramName]

            val hologramData = m.sparklyUserDisplayManager.createdTextDisplays[hologramName]

            if (hologramData == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("Holograma não existe!")
                }
                return
            }

            m.sparklyUserDisplayManager.createdTextDisplays.remove(hologramName)
            hologramData.sparklyDisplay.removeAllBlocks()

            context.sendMessage {
                color(NamedTextColor.GREEN)
                content("Holograma deletado!")
            }
        }
    }

    class SparklyDisplayNearExecutor(val m: DreamCore) : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val distance = integer("distance")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val player = context.requirePlayer()
            val distance = args[options.distance]
            val powedDistance = distance * distance

            val hologramsNearMe = m.sparklyUserDisplayManager.createdTextDisplays.filter {
                val bukkitLocation = it.value.sparklyDisplay.locationReference.toBukkit()
                if (bukkitLocation.world != player.world)
                    false
                else
                    powedDistance > it.value.sparklyDisplay.locationReference.toBukkit().distanceSquared(player.location)
            }

            if (hologramsNearMe.isEmpty()) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("Não tem nenhum holograma perto de você!")
                }
            } else {
                context.sendMessage {
                    color(NamedTextColor.YELLOW)
                    for (hologram in hologramsNearMe) {
                        append("${hologram.value.id}")
                    }
                }
            }
        }
    }

    class SparklyDisplaySaveExecutor(val m: DreamCore) : SparklyCommandExecutor() {
        override fun execute(context: CommandContext, args: CommandArguments) {
            m.sparklyUserDisplayManager.save()

            context.sendMessage {
                color(NamedTextColor.GREEN)
                content("Displays salvos!")
            }
        }
    }

    class SparklyDisplayReloadExecutor(val m: DreamCore) : SparklyCommandExecutor() {
        override fun execute(context: CommandContext, args: CommandArguments) {
            m.sparklyUserDisplayManager.load()

            context.sendMessage {
                color(NamedTextColor.GREEN)
                content("Displays recarregados!")
            }
        }
    }
}