package com.exoline.lang.test

import com.exoline.lang.Interpreter
import com.exoline.lang.toAny
import kotlinx.serialization.json.*
import me.alllex.parsus.parser.ParseException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class InterpreterTest {
    @Test
    fun test() {
        val testsDir = getFile("interpreter")
        val interpreter = Interpreter { ref ->
            testsDir.resolve("${ref}.txt").readText()
        }
        val only: Int = -1
        testsDir.listFiles()?.forEach {
            if (it.isDirectory) {
                if (only == -1 || it.name == only.toString()) {
                    val appStr = it.resolve("app.txt").readText()
                    val test = Json.decodeFromString<JsonObject>(it.resolve("test.json").readText())
                    val ignore = test["ignore"]?.jsonPrimitive?.booleanOrNull
                    if (ignore != true) {
                        val map = test["map"] as JsonObject
                        val expectedResult = test["expected"]?.jsonPrimitive?.boolean!!
                        try {
                            val (actualFields, appResult) = interpreter.parseOrThrow(appStr)
                            val actualResult = appResult.function(map)
                            val expectedFields = test["fields"]?.jsonArray?.map {
                                it.jsonPrimitive.content
                            }?.toSet()
                            if (expectedFields != null) {
                                Assertions.assertEquals(expectedFields, actualFields, "Test#${it.name}")
                            }
                            val expectedIdentifiersValues = test["identifiersValues"]?.jsonObject?.map { (identifier, value) ->
                                identifier to value.jsonPrimitive.toAny()
                            }?.toMap()
                            if (expectedIdentifiersValues != null) {
                                Assertions.assertEquals(expectedIdentifiersValues, appResult.identifiersValues, "Test#${it.name}")
                            }
                            Assertions.assertEquals(expectedResult, actualResult, "Test#${it.name}")
                        } catch(ex: ParseException) {
                            throw Exception("Test#${it.name} failed with parse exception", ex)
                        } catch(ex: Exception) {
                            throw Exception("Test#${it.name} failed with exception", ex)
                        }
                    }
                }
            }
        } ?: throw Exception("Where are files, Carl?")
    }
}