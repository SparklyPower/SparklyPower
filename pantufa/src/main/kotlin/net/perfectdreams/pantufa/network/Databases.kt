package net.perfectdreams.pantufa.network

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
import net.perfectdreams.pantufa.pantufa
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig

object Databases {
    private val DRIVER_CLASS_NAME = "org.postgresql.Driver"
    private val ISOLATION_LEVEL = IsolationLevel.TRANSACTION_REPEATABLE_READ // We use repeatable read to avoid dirty and non-repeatable reads! Very useful and safe!!

    val hikariConfigPantufa by lazy {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:postgresql://${pantufa.config.postgreSqlSparklyPower.ip}:${pantufa.config.postgreSqlSparklyPower.port}/${pantufa.config.postgreSqlSparklyPower.databaseName}?ApplicationName=PantufaSparkly"
        config.username = pantufa.config.postgreSqlSparklyPower.username
        config.password = pantufa.config.postgreSqlSparklyPower.password
        config.driverClassName = DRIVER_CLASS_NAME
        // Exposed uses autoCommit = false, so we need to set this to false to avoid HikariCP resetting the connection to
        // autoCommit = true when the transaction goes back to the pool, because resetting this has a "big performance impact"
        // https://stackoverflow.com/a/41206003/7271796
        config.isAutoCommit = false

        config.maximumPoolSize = 4
        config.addDataSourceProperty("cachePrepStmts", "true")
        config.addDataSourceProperty("prepStmtCacheSize", "250")
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")

        // Useful to check if a connection is not returning to the pool, will be shown in the log as "Apparent connection leak detected"
        config.leakDetectionThreshold = 30L * 1000
        config.transactionIsolation = ISOLATION_LEVEL.name // We use repeatable read to avoid dirty and non-repeatable reads! Very useful and safe!!

        return@lazy config
    }

    val dataSourcePantufa by lazy { HikariDataSource(hikariConfigPantufa); }
    val sparklyPower by lazy {
        Database.connect(
            dataSourcePantufa,
            databaseConfig = DatabaseConfig {
                defaultRepetitionAttempts = 5
                defaultIsolationLevel = ISOLATION_LEVEL.levelId // Change our default isolation level
            }
        )
    }

    val hikariConfigLuckPerms by lazy {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:postgresql://${pantufa.config.postgreSqlLuckPerms.ip}:${pantufa.config.postgreSqlLuckPerms.port}/${pantufa.config.postgreSqlLuckPerms.databaseName}?ApplicationName=PantufaLuckPerms"
        config.username = pantufa.config.postgreSqlLuckPerms.username
        config.password = pantufa.config.postgreSqlLuckPerms.password
        config.driverClassName = DRIVER_CLASS_NAME
        // Exposed uses autoCommit = false, so we need to set this to false to avoid HikariCP resetting the connection to
        // autoCommit = true when the transaction goes back to the pool, because resetting this has a "big performance impact"
        // https://stackoverflow.com/a/41206003/7271796
        config.isAutoCommit = false

        config.maximumPoolSize = 4
        config.addDataSourceProperty("cachePrepStmts", "true")
        config.addDataSourceProperty("prepStmtCacheSize", "250")
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")

        // Useful to check if a connection is not returning to the pool, will be shown in the log as "Apparent connection leak detected"
        config.leakDetectionThreshold = 30L * 1000
        config.transactionIsolation = ISOLATION_LEVEL.name // We use repeatable read to avoid dirty and non-repeatable reads! Very useful and safe!!

        return@lazy config
    }

    val dataSourceLuckPerms by lazy { HikariDataSource(hikariConfigLuckPerms) }
    val sparklyPowerLuckPerms by lazy {
        Database.connect(
            dataSourceLuckPerms,
            databaseConfig = DatabaseConfig {
                defaultRepetitionAttempts = 5
                defaultIsolationLevel = ISOLATION_LEVEL.levelId // Change our default isolation level
            }
        )
    }

    val hikariConfigLoritta by lazy {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:postgresql://${pantufa.config.postgreSqlLoritta.ip}:${pantufa.config.postgreSqlLoritta.port}/${pantufa.config.postgreSqlLoritta.databaseName}?ApplicationName=PantufaLori"
        config.username = pantufa.config.postgreSqlLoritta.username
        config.password = pantufa.config.postgreSqlLoritta.password
        config.driverClassName = DRIVER_CLASS_NAME
        // Exposed uses autoCommit = false, so we need to set this to false to avoid HikariCP resetting the connection to
        // autoCommit = true when the transaction goes back to the pool, because resetting this has a "big performance impact"
        // https://stackoverflow.com/a/41206003/7271796
        config.isAutoCommit = false

        config.maximumPoolSize = 4
        config.addDataSourceProperty("cachePrepStmts", "true")
        config.addDataSourceProperty("prepStmtCacheSize", "250")
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")

        // Useful to check if a connection is not returning to the pool, will be shown in the log as "Apparent connection leak detected"
        config.leakDetectionThreshold = 30L * 1000
        config.transactionIsolation = ISOLATION_LEVEL.name // We use repeatable read to avoid dirty and non-repeatable reads! Very useful and safe!!

        return@lazy config
    }

    val dataSourceLoritta by lazy { HikariDataSource(hikariConfigLoritta) }
    val loritta by lazy {
        Database.connect(
            dataSourceLoritta,
            databaseConfig = DatabaseConfig {
                defaultRepetitionAttempts = 5
                defaultIsolationLevel = ISOLATION_LEVEL.levelId // Change our default isolation level
            }
        )
    }
}