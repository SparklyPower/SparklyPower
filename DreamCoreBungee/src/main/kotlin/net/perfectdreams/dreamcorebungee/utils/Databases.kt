package net.perfectdreams.dreamcorebungee.utils

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.perfectdreams.dreamcorebungee.DreamCoreBungee
import org.jetbrains.exposed.sql.Database

object Databases {
	val hikariConfig by lazy {
		val config = HikariConfig()
		config.jdbcUrl = "jdbc:postgresql://${DreamCoreBungee.dreamConfig.postgreSqlIp}:${DreamCoreBungee.dreamConfig.postgreSqlPort}/${DreamCoreBungee.dreamConfig.databaseName}"
		config.username = DreamCoreBungee.dreamConfig.postgreSqlUser
		if (DreamCoreBungee.dreamConfig.postgreSqlPassword.isNotEmpty())
			config.password = DreamCoreBungee.dreamConfig.postgreSqlPassword
		config.driverClassName = "org.postgresql.Driver"

		config.maximumPoolSize = 10
		config.addDataSourceProperty("cachePrepStmts", "true")
		config.addDataSourceProperty("prepStmtCacheSize", "250")
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
		return@lazy config
	}
	val dataSource by lazy { HikariDataSource(hikariConfig) }
	val hikariConfigServer by lazy {
		val config = HikariConfig()
		config.jdbcUrl = "jdbc:postgresql://${DreamCoreBungee.dreamConfig.postgreSqlIp}:${DreamCoreBungee.dreamConfig.postgreSqlPort}/${DreamCoreBungee.dreamConfig.serverDatabaseName}"
		config.username = DreamCoreBungee.dreamConfig.postgreSqlUser
		if (DreamCoreBungee.dreamConfig.postgreSqlPassword.isNotEmpty())
			config.password = DreamCoreBungee.dreamConfig.postgreSqlPassword
		config.driverClassName = "org.postgresql.Driver"

		config.maximumPoolSize = 10
		config.addDataSourceProperty("cachePrepStmts", "true")
		config.addDataSourceProperty("prepStmtCacheSize", "250")
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
		return@lazy config
	}
	val dataSourceServer by lazy { HikariDataSource(hikariConfigServer) }

	val databaseNetwork by lazy { Database.connect(dataSource) }
	val databaseServer by lazy { Database.connect(dataSourceServer) }
}