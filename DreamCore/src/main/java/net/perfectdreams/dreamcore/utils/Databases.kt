package net.perfectdreams.dreamcore.utils

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.perfectdreams.dreamcore.DreamCore
import org.jetbrains.exposed.sql.Database

object Databases {
	val hikariConfig by lazy {
		val config = HikariConfig()
		config.jdbcUrl = "jdbc:postgresql://${DreamCore.dreamConfig.postgreSqlIp}:${DreamCore.dreamConfig.postgreSqlPort}/${DreamCore.dreamConfig.databaseName}"
		config.username = DreamCore.dreamConfig.postgreSqlUser
		if (DreamCore.dreamConfig.postgreSqlPassword.isNotEmpty())
			config.password = DreamCore.dreamConfig.postgreSqlPassword
		config.driverClassName = "org.postgresql.Driver"

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

	val databaseNetwork by lazy { Database.connect(dataSource) }
	@Deprecated("Please use databaseNetwork")
	val databaseServer = databaseNetwork
}