package net.perfectdreams.pantufa

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.perfectdreams.pantufa.tables.CraftConomyAccounts
import net.perfectdreams.pantufa.tables.CraftConomyBalance
import net.perfectdreams.pantufa.tables.PlayerSonecas
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.*

fun main() {
    val hikariConfigPantufa by lazy {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:postgresql://sparkly-postgresql:5432/sparklypower?ApplicationName=PantufaSparkly"
        config.username = "postgres"
        config.password = "8q7tdqjZEDTbcmYG"
        config.driverClassName = "org.postgresql.Driver"
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

        return@lazy config
    }

    val dataSourcePantufa by lazy { HikariDataSource(hikariConfigPantufa); }
    val sparklyPower by lazy {
        Database.connect(
            dataSourcePantufa,
            databaseConfig = DatabaseConfig {
                defaultRepetitionAttempts = 5
            }
        )
    }

    val hikariConfigCraftConomy by lazy {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:mysql://sparkly-mariadb:3306/sparklypower_survival"
        config.username = "root"
        config.password = "B3tterTh4nMySQLPerform4nce"
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

        return@lazy config
    }

    val dataSourceCraftConomy by lazy { HikariDataSource(hikariConfigCraftConomy) }
    val craftConomy by lazy {
        Database.connect(
            dataSourceCraftConomy,
            databaseConfig = DatabaseConfig {
                defaultRepetitionAttempts = 5
            }
        )
    }

    val results = transaction(craftConomy) {
        CraftConomyAccounts.innerJoin(CraftConomyBalance, { CraftConomyBalance.id }, { CraftConomyAccounts.id })
            .selectAll()
            .where { CraftConomyBalance.balance neq 250.0 }
            .toList()
    }

    var i = 0
    results.filter { it[CraftConomyAccounts.uuid] != null }.chunked(1_000).forEach {
        transaction(sparklyPower) {
            PlayerSonecas.batchUpsert(it, PlayerSonecas.id) {
                this[PlayerSonecas.id] = UUID.fromString(it[CraftConomyAccounts.uuid])
                this[PlayerSonecas.money] = it[CraftConomyBalance.balance].toBigDecimal()
                this[PlayerSonecas.updatedAt] = Instant.now()
            }
        }
        i += it.size
        println("Progress: $i/${results.size}")
    }
}