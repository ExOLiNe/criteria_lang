package com.exoline.lang.test

import com.exoline.lang.Interpreter
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.Test

// `main function` of this library - to make new syntax and immediately test it
class DevelopmentTest {
    @Test
    fun main() {
        val interpreter = Interpreter()
        val app = interpreter.parseOrThrow(getFile("development/code.txt").readText()).app
        val map = JsonObject(
            mapOf("some" to JsonPrimitive(10))
        )
        println(app(map))
    }
}