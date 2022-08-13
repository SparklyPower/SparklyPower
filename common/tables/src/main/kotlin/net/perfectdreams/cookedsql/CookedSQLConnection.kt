package net.perfectdreams.cookedsql

import java.sql.Connection
import java.sql.PreparedStatement

class CookedSQLConnection(val connection: Connection) {
    fun query(sql: String, statementBuilder: PreparedStatement.() -> (Unit) = {}): CookedSQLResultSet = connection.prepareStatement(sql)
        .apply(statementBuilder)
        .executeQuery()
        .let { CookedSQLResultSet(it) }
}