package net.perfectdreams.dreamcore.utils

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
import net.perfectdreams.dreamcore.DreamCore
import org.jetbrains.exposed.sql.DEFAULT_REPETITION_ATTEMPTS
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig

object Databases {
	private val DRIVER_CLASS_NAME = "org.postgresql.Driver"
	private val ISOLATION_LEVEL = IsolationLevel.TRANSACTION_REPEATABLE_READ // We use repeatable read to avoid dirty and non-repeatable reads! Very useful and safe!!

	val hikariConfig by lazy {
		val databaseConfig = DreamCore.dreamConfig.networkDatabase
		val hikariConfig = HikariConfig()

		hikariConfig.jdbcUrl = "jdbc:postgresql://${databaseConfig?.ip}:${databaseConfig?.port}/${databaseConfig?.databaseName}"

		hikariConfig.username = databaseConfig?.user
		if (databaseConfig?.password != null)
			hikariConfig.password = databaseConfig.password

		hikariConfig.driverClassName = DRIVER_CLASS_NAME

		// Exposed uses autoCommit = false, so we need to set this to false to avoid HikariCP resetting the connection to
		// autoCommit = true when the transaction goes back to the pool, because resetting this has a "big performance impact"
		// https://stackoverflow.com/a/41206003/7271796
		hikariConfig.isAutoCommit = false

		// Useful to check if a connection is not returning to the pool, will be shown in the log as "Apparent connection leak detected"
		hikariConfig.leakDetectionThreshold = 30L * 1000
		hikariConfig.transactionIsolation = ISOLATION_LEVEL.name // We use repeatable read to avoid dirty and non-repeatable reads! Very useful and safe!!

		hikariConfig
	}

	val dataSource by lazy { HikariDataSource(hikariConfig) }

	val databaseNetwork by lazy {
		Database.connect(
			HikariDataSource(dataSource),
			databaseConfig = DatabaseConfig {
				defaultRepetitionAttempts = DEFAULT_REPETITION_ATTEMPTS
				defaultIsolationLevel = ISOLATION_LEVEL.levelId // Change our default isolation level
			}
		)
	}
}