package com.exoline.lang

infix fun Number.plus(other: Number): Number {
    return when(this) {
        is Int -> when (other) {
            is Int -> this + other
            is Double -> this + other
            else -> TODO("Not implemented yet")
        }
        is Double -> when (other) {
            is Int -> this + other
            is Double -> this + other
            else -> TODO("Not implemented yet")
        }
        else -> TODO("Not implemented yet")
    }
}

infix fun Number.minus(other: Number): Number {
    return when(this) {
        is Int -> when (other) {
            is Int -> this - other
            is Double -> this - other
            else -> TODO("Not implemented yet")
        }
        is Double -> when (other) {
            is Int -> this - other
            is Double -> this - other
            else -> TODO("Not implemented yet")
        }
        else -> TODO("Not implemented yet")
    }
}

infix fun Number.times(other: Number): Number {
    return when(this) {
        is Int -> when (other) {
            is Int -> this * other
            is Double -> this * other
            else -> TODO("Not implemented yet")
        }
        is Double -> when (other) {
            is Int -> this * other
            is Double -> this * other
            else -> TODO("Not implemented yet")
        }
        else -> TODO("Not implemented yet")
    }
}

infix fun Number.div(other: Number): Number {
    return when(this) {
        is Int -> when (other) {
            is Int -> this / other
            is Double -> this / other
            else -> TODO("Not implemented yet")
        }
        is Double -> when (other) {
            is Int -> this / other
            is Double -> this / other
            else -> TODO("Not implemented yet")
        }
        else -> TODO("Not implemented yet")
    }
}