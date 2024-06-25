package net.perfectdreams.dreamcore.utils.exposed

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.postgresql.util.PGobject
import java.sql.ResultSet

// From ExposedPowerUtils but updated to work with Exposed 0.50.1
class JsonBinary : ColumnType<String>() {
	override fun sqlType() = "JSONB"

	override fun valueFromDB(value: Any): String {
		return when {
			value is PGobject -> value.value!!
			value is String -> value
			else -> error("Unexpected value $value of type ${value::class.qualifiedName}")
		}
	}

	override fun readObject(rs: ResultSet, index: Int): Any? {
		return rs.getString(index)
	}

	override fun setParameter(stmt: PreparedStatementApi, index: Int, value: Any?) {
		val obj = PGobject()
		obj.type = "jsonb"
		obj.value = value as String?
		stmt[index] = obj
	}
}

fun Table.jsonb(name: String): Column<String> = registerColumn(name, JsonBinary())