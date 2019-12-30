package net.perfectdreams.dreamcore.utils.exposed

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Index
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.TransactionManager

// https://github.com/JetBrains/Exposed/issues/167
class UpsertStatement<Key : Any>(table: Table, conflictColumn: Column<*>? = null, conflictIndex: Index? = null)
	: InsertStatement<Key>(table, false) {
	val indexName: String
	val indexColumns: List<Column<*>>

	init {
		when {
			conflictIndex != null -> {
				indexName = conflictIndex.indexName
				indexColumns = conflictIndex.columns
			}
			conflictColumn != null -> {
				indexName = conflictColumn.name
				indexColumns = listOf(conflictColumn)
			}
			else -> throw IllegalArgumentException()
		}
	}

	override fun prepareSQL(transaction: Transaction) = buildString {
		append(super.prepareSQL(transaction))

		val dialect = transaction.db.vendor
		if (dialect == "postgresql") {
			append(" ON CONFLICT(")
			append(indexName)
			append(") DO UPDATE SET ")

			values.keys.filter { it !in indexColumns }.joinTo(this) { "${transaction.identity(it)}=EXCLUDED.${transaction.identity(it)}" }

		} else {
			append (" ON DUPLICATE KEY UPDATE ")
			values.keys.filter { it !in indexColumns }.joinTo(this) { "${transaction.identity(it)}=VALUES(${transaction.identity(it)})" }
		}
	}
}

inline fun <T : Table> T.upsert(conflictColumn: Column<*>? = null, conflictIndex: Index? = null, body: T.(UpsertStatement<Number>) -> Unit) =
		UpsertStatement<Number>(this, conflictColumn, conflictIndex).apply {
			body(this)
			execute(TransactionManager.current())
		}

fun Table.indexR(customIndexName: String? = null, isUnique: Boolean = false, vararg columns: Column<*>): Index {
	val index = Index(columns.toList(), isUnique, customIndexName)
	indices.add(index)
	return index
}

fun Table.uniqueIndexR(customIndexName: String? = null, vararg columns: Column<*>): Index = indexR(customIndexName, true, *columns)