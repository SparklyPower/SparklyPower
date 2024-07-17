package net.perfectdreams.pantufa.utils.exposed

import org.jetbrains.exposed.sql.*

class ILikeOp(expr1: Expression<*>, expr2: Expression<*>) : ComparisonOp(expr1, expr2, "ILIKE")
infix fun ExpressionWithColumnType<String>.ilike(pattern: String) = ILikeOp(this, QueryParameter(pattern, columnType))