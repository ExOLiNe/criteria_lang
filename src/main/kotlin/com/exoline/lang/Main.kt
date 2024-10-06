package com.exoline.lang

import kotlinx.serialization.json.*
import me.alllex.parsus.parser.ParseException
import java.io.File

fun main() {
    val grammar = MyGrammar()
    File("test").listFiles()?.forEach {
        val appStr = it.resolve("app.txt").readText()
        val test = Json.decodeFromString<JsonObject>(it.resolve("test.json").readText())
        val map = test["map"] as JsonObject
        val expected = test["expected"]?.jsonPrimitive?.boolean!!
        try {
            val app = grammar.parseOrThrow(appStr)
            val actual = app(map)
            if (actual != expected) {
                throw Exception("Test#${it.name} failed. Actual: ${actual}. Expected: $expected")
            }
        } catch(ex: ParseException) {
            throw Exception("Test#${it.name} failed with parse exception", ex)
        }
    } ?: throw Exception("Where are files, Carl?")
    val app = grammar.parseOrThrow(File("code.txt").readText())
    val map = JsonObject(
        mapOf("some" to JsonPrimitive("hello"))
    )
    println(app(map))
}