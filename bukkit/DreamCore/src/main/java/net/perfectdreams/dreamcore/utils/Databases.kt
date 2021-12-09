package net.perfectdreams.dreamcore.utils

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
import net.perfectdreams.dreamcore.DreamCore
import org.jetbrains.exposed.sql.DEFAULT_REPETITION_ATTEMPTS
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.transactions.DEFAULT_REPETITION_ATTEMPTS
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.io.File
import java.sql.Connection

object Databases {
	private val DRIVER_CLASS_NAME = "org.postgresql.Driver"
	private val ISOLATION_LEVEL = IsolationLevel.TRANSACTION_REPEATABLE_READ // We use repeatable read to avoid dirty and non-repeatable reads! Very useful and safe!!

	val hikariConfig by lazy {
		val hikariConfig = HikariConfig()

		hikariConfig.driverClassName = DRIVER_CLASS_NAME

		// Exposed uses autoCommit = false, so we need to set this to false to avoid HikariCP resetting the connection to
		// autoCommit = true when the transaction goes back to the pool, because resetting this has a "big performance impact"
		// https://stackoverflow.com/a/41206003/7271796
		hikariConfig.isAutoCommit = false

		// Useful to check if a connection is not returning to the pool, will be shown in the log as "Apparent connection leak detected"
		hikariConfig.leakDetectionThreshold = 30L * 1000
		hikariConfig.transactionIsolation = IsolationLevel.TRANSACTION_REPEATABLE_READ.name // We use repeatable read to avoid dirty and non-repeatable reads! Very useful and safe!!

		hikariConfig
	}

	val dataSource by lazy { HikariDataSource(hikariConfig) }
	@Deprecated("Please use hikariConfig")
	val hikariConfigServer = hikariConfig

	@Deprecated("Please use dataSource")
	val dataSourceServer = dataSource

	val databaseNetwork by lazy {
		Database.connect(
			HikariDataSource(dataSource),
			databaseConfig = DatabaseConfig {
				defaultRepetitionAttempts = DEFAULT_REPETITION_ATTEMPTS
				defaultIsolationLevel = ISOLATION_LEVEL.levelId // Change our default isolation level
			}
		)
	}
	@Deprecated("Please use databaseNetwork")
	val databaseServer = databaseNetwork
}