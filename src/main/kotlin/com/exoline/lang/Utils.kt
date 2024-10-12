package com.exoline.lang

import kotlinx.serialization.json.*
import me.alllex.parsus.parser.Parser
import me.alllex.parsus.parser.or

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

operator fun Number.compareTo(other: Number): Int {
    return when(this) {
        is Int -> when (other) {
            is Int -> this.compareTo(other)
            is Double -> this.compareTo(other)
            else -> TODO("Not implemented yet")
        }
        is Double -> when (other) {
            is Int -> this.compareTo(other)
            is Double -> this.compareTo(other)
            else -> TODO("Not implemented yet")
        }
        else -> TODO("Not implemented yet")
    }
}

fun VarType.getRecursively(field: String): Any {
    val fieldTrimmed = field.trim('/')
    val segments = fieldTrimmed.split("/")
    val value = segments.fold<String, JsonElement>(this) { acc, segment ->
        (acc as JsonObject)[segment]!!
    }.jsonPrimitive
    return when {
        value.intOrNull != null -> value.int
        value.isString -> value.content
        value.booleanOrNull != null -> value.boolean
        else -> throw RuntimeException("Unknown type")
    }
}