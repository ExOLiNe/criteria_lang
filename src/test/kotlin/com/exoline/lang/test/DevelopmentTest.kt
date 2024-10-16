package com.exoline.lang.test

import com.exoline.lang.VarType
import com.exoline.lang.parser.FunctionParser
import com.exoline.lang.Interpreter
import com.exoline.lang.JObject
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import me.alllex.parsus.parser.Grammar
import me.alllex.parsus.parser.Parser
import me.alllex.parsus.parser.map
import me.alllex.parsus.token.regexToken
import org.junit.jupiter.api.Test

class TestGrammar : Grammar<Any>() {
    val term by regexToken("\"[a-zA-Z0-9]+\"").map { match ->
        { it: VarType ->
            match.text
        }
    }
    override val root: Parser<Any> by FunctionParser(term).root
}

// `main function` of this library - to make new syntax and immediately test it
class DevelopmentTest {
    @Test
    fun main() {
        val mapper = ObjectMapper().registerKotlinModule()
        val interpreter = Interpreter()
        val app = interpreter.parseOrThrow(
            getFile("development/code.txt").readText()
        ).app
        val map = mapper.convertValue<JObject>(mapOf("some" to 10))
        println(app(map).result)
    }
}