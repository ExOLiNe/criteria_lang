package com.exoline.lang

import com.exoline.lang.parser.AbstractGrammar
import com.exoline.lang.parser.ArithmeticParser
import com.exoline.lang.parser.BooleanParser
import com.exoline.lang.parser.FunctionParser
import kotlinx.serialization.json.JsonObject
import me.alllex.parsus.parser.*

typealias VarType = JsonObject
typealias F = (VarType) -> Any
typealias BoolF = (VarType) -> Boolean
typealias PF = Parser<F>
typealias Arguments = List<Any>

data class AppResult(
    val fields: Set<String>,
    val app: BoolF
)

class Interpreter : AbstractGrammar<AppResult>(
    debugMode = true
) {
    private val fields = mutableSetOf<String>()

    private fun resetState() {
        fields.clear()
    }

    private val fieldParser: Parser<String> by separated(string, divToken).map {
        it.joinToString(divToken.string)
    }.quotedParser()

    private val varAccessParser: PF
        by -varName and (-sqBrL and fieldParser and -sqBrR).map { field ->
            fields += field
            val function = { it: VarType ->
                it.getRecursively(field)
            }
            function
        }

    private val stringParser: PF by stringLiteral.mapToF()

    val functionParser = FunctionParser(ref(::term))

    val arithmeticExpr: Parser<(VarType) -> Any> = ArithmeticParser(listOf(
        varAccessParser,
        functionParser.root
    )).root

    val term: Parser<(VarType) -> Any> by
        stringParser or arithmeticExpr

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

    val boolExpr by BooleanParser(term, listOf(inArrayBoolExpr, varAccessParser)).root

    override val root: Parser<AppResult> by parser {
        resetState()
        val result = boolExpr()
        val function: BoolF = { it ->
            val value = result(it)
            if (value !is Boolean) {
                throw RuntimeException("OMG!")
            }
            value
        }
        AppResult(
            fields = fields,
            app = function
        )
    }
}