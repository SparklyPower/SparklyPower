package net.perfectdreams.dreamcore.utils

import com.github.luben.zstd.Zstd
import kotlinx.coroutines.delay
import me.lucko.spark.bukkit.BukkitSparkPlugin
import me.lucko.spark.common.SparkPlatform
import me.lucko.spark.common.command.sender.CommandSender
import me.lucko.spark.common.sampler.Sampler
import me.lucko.spark.common.sampler.node.MergeMode
import me.lucko.spark.common.sampler.source.ClassSourceLookup
import me.lucko.spark.common.util.MethodDisambiguator
import net.perfectdreams.dreamcore.DreamCore
import java.io.File
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Level
import kotlin.time.Duration.Companion.hours

class SparkSnap(val m: DreamCore, val spark: BukkitSparkPlugin) {
    companion object {
        val getPlatformFieldHandler: MethodHandle by lazy {
            val lookup: MethodHandles.Lookup = MethodHandles.lookup()
            val clazz = BukkitSparkPlugin::class.java
            val f = clazz.getDeclaredField("platform")

            // Allow access to private fields
            f.isAccessible = true

            // Get a MethodHandle for the field
            lookup.unreflectGetter(f)
        }
    }

    fun startTask() {
        m.launchAsyncThread {
            while (true) {
                // We delay first because we don't want to create a snap right after the server started up
                delay(1.hours)
                try {
                    snap()
                } catch (e: Exception) {
                    m.logger.log(Level.WARNING, e) { "Something went wrong while trying to save current spark profile to a file!" }
                }
            }
        }
    }

    fun snap() {
        m.logger.info { "Automagically saving current spark profile to a file..." }
        val platform = getPlatformFieldHandler.invoke(spark) as SparkPlatform

        val sampler = platform.samplerContainer.activeSampler
        if (sampler == null) {
            m.logger.warning("There isn't a profile active! Ignoring...")
            return
        }

        val exportProps = Sampler.ExportProps()
            .creator(CommandSender.Data("SparklyPower", null))
            .comment("Automagically saved by ${m.name} - ${DreamCore.dreamConfig.bungeeName}")
            .mergeMode {
                val methodDisambiguator = MethodDisambiguator()
                MergeMode.sameMethod(methodDisambiguator)
            }
            .classSourceLookup { ClassSourceLookup.create(platform) }

        val output = sampler.toProto(platform, exportProps)

        val sparkProfilesFolder = File(m.dataFolder, "sparkprofiles")
        sparkProfilesFolder.mkdirs()

        val date = SimpleDateFormat("yyyy-MM-dd-HHmmss").format(Date())
        File(sparkProfilesFolder, "${date}.sparkprofile.zst")
            .writeBytes(Zstd.compress(output.toByteArray()))
        m.logger.info { "Successfully compressed and saved current spark profile to a file!" }
    }
}