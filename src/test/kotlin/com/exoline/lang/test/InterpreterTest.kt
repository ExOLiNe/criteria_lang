package com.exoline.lang.test

import com.exoline.lang.Interpreter
import kotlinx.serialization.json.*
import me.alllex.parsus.parser.ParseException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class InterpreterTest {
    @Test
    fun test() {
        val interpreter = Interpreter()
        val testsDir = getFile("interpreter")
        testsDir.listFiles()?.forEach {
            val appStr = it.resolve("app.txt").readText()
            val test = Json.decodeFromString<JsonObject>(it.resolve("test.json").readText())
            val ignore = test["ignore"]?.jsonPrimitive?.booleanOrNull
            if (ignore != true) {
                val map = test["map"] as JsonObject
                val expected = test["expected"]?.jsonPrimitive?.boolean!!
                try {
                    val app = interpreter.parseOrThrow(appStr)
                    val actual = app(map)
                    Assertions.assertEquals(expected, actual, "Test#${it.name} failed. Actual: ${actual}. Expected: $expected")
                } catch(ex: ParseException) {
                    throw Exception("Test#${it.name} failed with parse exception", ex)
                } catch(ex: Exception) {
                    throw Exception("Test#${it.name} failed with exception", ex)
                }
            }
        } ?: throw Exception("Where are files, Carl?")
    }
}