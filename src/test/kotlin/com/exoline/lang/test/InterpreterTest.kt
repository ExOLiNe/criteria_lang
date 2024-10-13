package com.exoline.lang.test

import com.exoline.lang.Interpreter
import com.exoline.lang.JObject
import com.exoline.lang.toAny
import com.exoline.lang.toJObject
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.TextNode
import me.alllex.parsus.parser.ParseException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class InterpreterTest {
    @Test
    fun test() {
        val testsDir = getFile("interpreter")
        val interpreter = Interpreter{ ref ->
            testsDir.resolve("${ref}.txt").readText()
        }
        val only: Int = -1
        testsDir.listFiles()?.forEach {
            if (it.isDirectory) {
                if (only == -1 || it.name == only.toString()) {
                    val appStr = it.resolve("app.txt").readText()
                    val test = it.resolve("test.json").readText().toJObject()
                    val ignore = (test["ignore"] as? BooleanNode)?.booleanValue()
                    if (ignore != true) {
                        val map = test["map"] as JObject
                        val expectedResult = (test["expected"] as? BooleanNode)?.booleanValue()!!
                        try {
                            val (actualFields, appResult) = interpreter.parseOrThrow(appStr)
                            val (actualResult, identifiersValues) = appResult(map)
                            val expectedFields = (test["fields"] as? ArrayNode)?.map {
                                (it as TextNode).textValue()
                            }?.toSet()
                            if (expectedFields != null) {
                                Assertions.assertEquals(expectedFields, actualFields, "Test#${it.name}")
                            }
                            val expectedIdentifiersValues = (test["identifiersValues"] as? JObject)?.fields()?.asSequence()?.map { (identifier, value) ->
                                identifier to value.toAny()
                            }?.toMap()
                            if (expectedIdentifiersValues != null) {
                                Assertions.assertEquals(expectedIdentifiersValues, identifiersValues, "Test#${it.name}")
                            }
                            Assertions.assertEquals(expectedResult, actualResult, "Test#${it.name}")
                        } catch(ex: ParseException) {
                            throw Exception("Test#${it.name} failed with parse exception", ex)
                        } catch(ex: Exception) {
                            test["throws"]?.textValue()?.let { exceptionName ->
                                Assertions.assertEquals(exceptionName, ex.javaClass.simpleName, "Test#${it.name} must throw $exceptionName")
                            } ?: throw Exception("Test#${it.name} failed with exception", ex)
                        }
                    }
                }
            }
        } ?: throw Exception("Where are files, Carl?")
    }
}