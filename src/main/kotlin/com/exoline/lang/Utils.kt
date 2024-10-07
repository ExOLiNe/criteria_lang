package com.exoline.lang

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