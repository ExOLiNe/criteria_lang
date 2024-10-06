package com.exoline.lang

infix fun Expr.and(other: Expr): Expr.Bool = (if (this is Expr.Bool && other is Expr.Bool) {
    value && other.value
} else {
    false
}).let {
    Expr.Bool(it)
}

infix fun Expr.or(other: Expr): Expr.Bool = (if (this is Expr.Bool && other is Expr.Bool) {
    value || other.value
} else {
    false
}).let {
    Expr.Bool(it)
}

fun Number.plus(other: Number): Number {
    return when(this) {
        is Int -> when (other) {
            is Int -> this + other
            else -> TODO("Not implemented yet")
        }
        else -> TODO("Not implemented yet")
    }
}

fun Number.minus(other: Number): Number {
    return when(this) {
        is Int -> when (other) {
            is Int -> this - other
            else -> TODO("Not implemented yet")
        }
        else -> TODO("Not implemented yet")
    }
}

fun Number.times(other: Number): Number {
    return when(this) {
        is Int -> when (other) {
            is Int -> this * other
            else -> TODO("Not implemented yet")
        }
        else -> TODO("Not implemented yet")
    }
}

fun Number.div(other: Number): Number {
    return when(this) {
        is Int -> when (other) {
            is Int -> this / other
            else -> TODO("Not implemented yet")
        }
        else -> TODO("Not implemented yet")
    }
}