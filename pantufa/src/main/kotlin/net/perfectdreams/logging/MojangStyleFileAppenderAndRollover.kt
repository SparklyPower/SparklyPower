package net.perfectdreams.logging

import ch.qos.logback.core.FileAppender
import ch.qos.logback.core.rolling.RolloverFailure
import ch.qos.logback.core.rolling.helper.CompressionMode
import ch.qos.logback.core.rolling.helper.Compressor
import ch.qos.logback.core.spi.ContextAwareBase
import ch.qos.logback.core.status.ErrorStatus
import ch.qos.logback.core.status.WarnStatus
import ch.qos.logback.core.util.FileUtil
import com.github.luben.zstd.Zstd
import com.github.luben.zstd.ZstdOutputStream
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.Future
import java.util.zip.GZIPOutputStream


/**
 * A Mojang-like log rollover
 *
 * The log files are rolled over on application startup, and after each day
 *
 * Yes, I know, this should be implemented as a rolling policy... but I don't care lmao, Logback is very complex.
 *
 * @author MrPowerGamerBR
 */
class MojangStyleFileAppenderAndRollover<E> : FileAppender<E>() {
    private lateinit var nextRolloverDate: LocalDateTime
    private lateinit var compressor: Any
    private var compressionFuture: Future<*>? = null
    private var archivedLogsFilePrefix: String? = null
    private var archivedLogsFileExtension: String? = null
    private var archivedLogsCompression: String? = null

    fun setArchivedLogsFilePrefix(archivedLogsFilePrefix: String) {
        this.archivedLogsFilePrefix = archivedLogsFilePrefix
    }

    fun setArchivedLogsFileExtension(archivedLogsFileExtension: String) {
        this.archivedLogsFileExtension = archivedLogsFileExtension
    }

    fun setArchivedLogsCompression(archivedLogsCompression: String) {
        this.archivedLogsCompression = archivedLogsCompression
    }

    override fun start() {
        when (archivedLogsCompression) {
            "GZ" -> {
                compressor = Compressor(CompressionMode.GZ).apply {
                    context = this@MojangStyleFileAppenderAndRollover.context
                }
            }
            "ZIP" -> {
                compressor = Compressor(CompressionMode.ZIP).apply {
                    context = this@MojangStyleFileAppenderAndRollover.context
                }
            }
            "ZSTD" -> {
                compressor = ZstdCompressor().apply {
                    context = this@MojangStyleFileAppenderAndRollover.context
                }
            }
            else -> error("Archived Logs Compression type is unsupported! $archivedLogsCompression")
        }

        // Trigger the rollover when the application first starts, we need to do it here on start instead of on isTriggeringEvent, because Logback WILL write the log line to the log file BEFORE it is triggered!
        // This must be ran BEFORE the super.start(), to avoid an empty file being created due to the openFile call
        val lastActiveLogFile = File(file)
        if (lastActiveLogFile.exists()) {
            // The lastActiveLogFile exists, let's rollover!
            rollover(
                Instant.ofEpochMilli(lastActiveLogFile.lastModified())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
            )
        }

        nextRolloverDate = getNextRolloverDate(LocalDateTime.now())

        super.start()
    }

    override fun writeOut(event: E) {
        // Check if we need to rotate the log file
        val date = LocalDateTime.now()

        // Trigger rollover if the current date is >= the nextRolloverDate, the nextRolloverDate will be updated on rollover
        if (date >= nextRolloverDate) {
            rollover()
        }

        super.writeOut(event)
    }

    private fun rollover() {
        val date = LocalDateTime.now()

        // Calculate next rollover date
        nextRolloverDate = getNextRolloverDate(date)

        rollover(date)
    }

    private fun rollover(date: LocalDateTime) {
        try {
            // Check what index we should use
            var index = 1
            val compressedFile: File?
            while (true) {
                val fileName = "${date.year}-${date.monthValue.toString().padStart(2, '0')}-${date.dayOfMonth.toString().padStart(2, '0')}.$index"

                val newCompressedFile = File("$archivedLogsFilePrefix$fileName$archivedLogsFileExtension")
                println(newCompressedFile)
                if (!newCompressedFile.exists()) {
                    compressedFile = newCompressedFile
                    break
                }
                index++
            }

            closeOutputStream() // Close the output stream to let the file be moved

            // Bye to the raw file!!! If it exists it is GONE
            val compressedFileRaw = File("$archivedLogsFilePrefix${UUID.randomUUID()}.log")
            compressedFileRaw.delete()

            File(file).renameTo(compressedFileRaw)

            // Reopen the latest.log file
            openFile(file)

            when (val compressor = compressor) {
                is ZstdCompressor -> {
                    val future = compressor.asyncCompress(
                        compressedFileRaw.toString(),
                        compressedFile.toString()
                    )
                    this.compressionFuture = future
                }

                is Compressor -> {
                    val future = compressor.asyncCompress(
                        compressedFileRaw.toString(),
                        compressedFile.toString(),
                        "xd" // Not used for gz files
                    )
                    this.compressionFuture = future
                }

                else -> error("Invalid compressor $compressor")
            }


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Tomorrow at midnight
    private fun getNextRolloverDate(date: LocalDateTime) = date
        .plusDays(1)
        .withHour(0)
        .withMinute(0)
        .withNano(0)

    /**
     * A make-shift Zstd Compressor class based off Logback's [Compressor] class.
     *
     * @author MrPowerGamerBR
     */
    class ZstdCompressor : ContextAwareBase() {
        companion object {
            private const val BUFFER_SIZE = 8192
        }

        /**
         * @param nameOfFile2Compress
         * @param nameOfCompressedFile
         */
        fun compress(nameOfFile2Compress: String, nameOfCompressedFile: String) {
            zstdCompress(nameOfFile2Compress, nameOfCompressedFile)
        }

        private fun zstdCompress(nameOfFile2gz: String, nameOfgzedFile: String) {
            val file2gz = File(nameOfFile2gz)
            if (!file2gz.exists()) {
                addStatus(WarnStatus("The file to compress named [$nameOfFile2gz] does not exist.", this))
                return
            }
            val gzedFile = File(nameOfgzedFile)
            if (gzedFile.exists()) {
                addWarn(
                    "The target compressed file named [" + nameOfgzedFile
                            + "] exist already. Aborting file compression."
                )
                return
            }
            addInfo("ZSTD compressing [$file2gz] as [$gzedFile]")
            createMissingTargetDirsIfNecessary(gzedFile)
            try {
                BufferedInputStream(FileInputStream(nameOfFile2gz)).use { bis ->
                    ZstdOutputStream(FileOutputStream(nameOfgzedFile)).use { gzos ->
                        val inbuf = ByteArray(BUFFER_SIZE)
                        var n: Int
                        while ((bis.read(inbuf).also { n = it }) != -1) {
                            gzos.write(inbuf, 0, n)
                        }
                        addInfo("Done ZSTD compressing [" + file2gz + "] as [" + gzedFile + "]")
                    }
                }
            } catch (e: Exception) {
                addStatus(
                    ErrorStatus(
                        "Error occurred while compressing [$nameOfFile2gz] into [$nameOfgzedFile].", this,
                        e
                    )
                )
            }
            if (!file2gz.delete()) {
                addStatus(WarnStatus("Could not delete [$nameOfFile2gz].", this))
            }
        }

        fun createMissingTargetDirsIfNecessary(file: File) {
            val result = FileUtil.createMissingParentDirectories(file)
            if (!result) {
                addError("Failed to create parent directories for [" + file.absolutePath + "]")
            }
        }

        override fun toString(): String {
            return this.javaClass.name
        }

        @Throws(RolloverFailure::class)
        fun asyncCompress(
            nameOfFile2Compress: String,
            nameOfCompressedFile: String
        ): Future<*> {
            val runnable = CompressionRunnable(nameOfFile2Compress, nameOfCompressedFile)
            val executorService = context.executorService
            return executorService.submit(runnable)
        }

        internal inner class CompressionRunnable(
            val nameOfFile2Compress: String,
            val nameOfCompressedFile: String
        ) : Runnable {
            override fun run() {
                compress(nameOfFile2Compress, nameOfCompressedFile)
            }
        }
    }
}