package net.perfectdreams.dreamcore.utils

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.perfectdreams.dreamcore.DreamCore
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.io.File
import java.sql.Connection

object Databases {
	val hikariConfig by lazy {
		val config = HikariConfig()


		var jdbcUrlPrefix: String? = null
		var driverClassName: String? = null
		var dbPath: String? = null
		val sqLiteDbFile = File(DreamCore.INSTANCE.dataFolder, "dream.db")

		val databaseConfig = DreamCore.dreamConfig.networkDatabase
		val databaseType = databaseConfig?.type ?: "SQLite"

		when (databaseType) {
			"SQLite" -> {
				jdbcUrlPrefix = "sqlite"
				driverClassName = "org.sqlite.JDBC"
				dbPath = "${sqLiteDbFile.toPath()}"
			}
			"SQLiteMemory" -> {
				jdbcUrlPrefix = "sqlite"
				driverClassName = "org.sqlite.JDBC"
				dbPath = ":memory:"
			}
			"PostgreSQL" -> {
				if (databaseConfig != null) {
					jdbcUrlPrefix = "postgresql"
					driverClassName = "org.postgresql.Driver"
					dbPath = "//${databaseConfig.ip}:${databaseConfig.port}/${databaseConfig.databaseName}"
				}

			}
			else -> throw RuntimeException("Unsupported Database Dialect $databaseType")
		}

		config.jdbcUrl = "jdbc:$jdbcUrlPrefix:$dbPath"
		config.username = databaseConfig?.user
		if (databaseConfig?.password != null)
			config.password = databaseConfig.password

		config.driverClassName = driverClassName

		config.maximumPoolSize = 10
		config.addDataSourceProperty("cachePrepStmts", "true")
		config.addDataSourceProperty("prepStmtCacheSize", "250")
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
		return@lazy config
	}
	val dataSource by lazy { HikariDataSource(hikariConfig) }
	@Deprecated("Please use hikariConfig")
	val hikariConfigServer = hikariConfig

	@Deprecated("Please use dataSource")
	val dataSourceServer = dataSource

	val databaseNetwork by lazy {
		val db = Database.connect(dataSource)
		if (true) {
			TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
		}
		db
	}
	@Deprecated("Please use databaseNetwork")
	val databaseServer = databaseNetwork
}