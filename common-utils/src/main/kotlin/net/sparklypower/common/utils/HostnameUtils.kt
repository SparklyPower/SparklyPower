package net.sparklypower.common.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.TimeUnit

object HostnameUtils {
    private val logger = KotlinLogging.logger {}

    fun getHostname(): String {
        // From hostname command
        try {
            val proc = ProcessBuilder("hostname")
                .start()

            proc.waitFor(5, TimeUnit.SECONDS)
            val hostname = proc.inputStream.readAllBytes().toString(Charsets.UTF_8).removeSuffix("\n")
            proc.destroyForcibly()

            logger.warn { "Machine Hostname via \"hostname\" command: $hostname" }
            return hostname.replace("\n", "").replace("\r", "")
        } catch (e: Exception) {
            logger.warn(e) { "Something went wrong while trying to get the machine's hostname via the \"hostname\" command!" }
        }

        // From hostname env variable
        System.getenv("HOSTNAME")?.let {
            logger.warn { "Machine Hostname via \"HOSTNAME\" env variable: $it" }
            return it.replace("\n", "").replace("\r", "")
        }

        // From computername env variable
        System.getenv("COMPUTERNAME")?.let {
            logger.warn { "Machine Hostname via \"COMPUTERNAME\" env variable: $it" }
            return it.replace("\n", "").replace("\r", "")
        }

        logger.warn { "I wasn't able to get the machine's hostname! Falling back to \"Unknown\"..." }
        return "Unknown"
    }
}