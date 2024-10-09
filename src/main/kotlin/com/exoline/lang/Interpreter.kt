package com.exoline.lang

import com.exoline.lang.parser.AbstractGrammar
import com.exoline.lang.parser.ArithmeticParser
import com.exoline.lang.parser.BooleanParser
import kotlinx.serialization.json.*
import me.alllex.parsus.parser.*
import me.alllex.parsus.token.literalToken

typealias VarType = JsonObject
typealias F = (VarType) -> Any
typealias BoolF = (VarType) -> Boolean
typealias PF = Parser<F>
typealias Arguments = List<Any>

class Interpreter : AbstractGrammar<BoolF>(
    debugMode = true
) {
    private fun functionCall(
        funcToken: String,
        vararg argumentsParser: PF,
        body: (Arguments) -> Any
    ): PF = -literalToken(funcToken) and
            (-parL and
                    argumentsParser.toList().join(comma)
                    and -parR
                    ).map { argResolvers ->
        val function = { it: VarType ->
            val arguments = argResolvers.map { arg ->
                arg(it)
            }
            body(arguments)
        }
        function
    }

    private val varAccessParser: PF
        by -varName and (-sqBrL and stringLiteral and -sqBrR).map { field ->
        val function = { it: VarType ->
            val value = it[field]!!.jsonPrimitive
            when {
                value.intOrNull != null -> value.jsonPrimitive.int
                value.isString -> value.content
                value.booleanOrNull != null -> value.boolean
                else -> throw RuntimeException("Unknown type")
            }
        }
        function
    }

    private val stringParser: PF by stringLiteral.mapToF()

    private val arithmeticExpr = ArithmeticParser(varAccessParser).root

    private val arrayExpr by parser {
        val value = split(
            stringLiteral or numberParser,
            comma,
            allowEmpty = true,
            trailingSeparator = true
        ).toSet()
        value
    }.between(sqBrL, sqBrR)

    private val sizeCallParser by functionCall(
        "size",
        arrayExpr.mapToF()
    ) { args ->
        (args.first() as Set<*>).size
    }

    private val strLengthCallParser by functionCall(
        "size",
        stringParser
    ) { args ->
        (args.first() as String).length
    }

    private val term by stringParser or arithmeticExpr or trueParser or falseParser

    private val inArrayBoolExpr by parser {
        val leftResolver = term()
        val isIn = inToken()
        val right = arrayExpr()
        val function = { it: VarType ->
            val left = leftResolver(it)
            (left in right) xor !isIn
        }
        function
    }

    private val expr by BooleanParser(term, listOf(inArrayBoolExpr, varAccessParser)).root

    override val root: Parser<BoolF> by parser {
        val result = expr()
        val function: BoolF = { it ->
            val value = result(it)
            if (value !is Boolean) {
                throw RuntimeException("OMG!")
            }
            value
        }
        function
    }
}