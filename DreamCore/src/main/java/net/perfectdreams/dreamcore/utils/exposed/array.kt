package net.perfectdreams.dreamcore.utils.exposed

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager

fun <T> Table.array(name: String, columnType: ColumnType): Column<Array<T>> =
        registerColumn(name, ArrayColumnType(columnType))

class ArrayColumnType(private val type: ColumnType) : ColumnType() {
    override fun sqlType(): String = buildString {
        append(type.sqlType())
        append(" ARRAY")
    }

    override fun valueToDB(value: Any?): Any? {
        if (value is kotlin.Array<*>) {
            val columnType = type.sqlType().split("(")[0]
            return TransactionManager.currentOrNull()?.connection?.createArrayOf(columnType, value)
        } else {
            return super.valueToDB(value)
        }
    }

    override fun valueFromDB(value: Any): Any {
        if (value is java.sql.Array) {
            return value.array
        }
        if (value is Array<*>) {
            return value
        }
        error("Array does not support for this database")
    }
}

class AnyOp(val expr1: Expression<*>, val expr2: Expression<*>) : Op<Boolean>() {
    override fun toSQL(queryBuilder: QueryBuilder) = buildString {
        if (expr2 is OrOp<*>) {
            append("(").append(expr2.toSQL(queryBuilder)).append(")")
        } else {
            append(expr2.toSQL(queryBuilder))
        }
        append(" = ANY (")
        if (expr1 is OrOp<*>) {
            append("(").append(expr1.toSQL(queryBuilder)).append(")")
        } else {
            append(expr1.toSQL(queryBuilder))
        }
        append(")")
    }
}

class ContainsOp(expr1: Expression<*>, expr2: Expression<*>) : ComparisonOp(expr1, expr2, "@>")

infix fun <T, S> ExpressionWithColumnType<T>.any(t: S): Op<Boolean> {
    if (t == null) {
        return IsNullOp(this)
    }
    return AnyOp(this, QueryParameter(t, columnType))
}

infix fun <T, S> ExpressionWithColumnType<T>.contains(arry: Array<in S>): Op<Boolean> =
        ContainsOp(this, QueryParameter(arry, columnType))