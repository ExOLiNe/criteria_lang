package com.exoline.lang

import kotlinx.serialization.json.*
import me.alllex.parsus.parser.*
import me.alllex.parsus.token.literalToken
import me.alllex.parsus.token.regexToken

typealias VarType = JsonObject
typealias F = (VarType) -> Any
typealias BoolF = (VarType) -> Boolean
typealias PF = Parser<F>
typealias Arguments = List<Any>

class Interpreter : Grammar<BoolF>(
    ignoreCase = false,
    debugMode = true
) {
    init {
        regexToken("\\s+", ignored = true)
    }

    private val varName by literalToken("object")
    private val sqBrL by literalToken("[")
    private val sqBrR by literalToken("]")
    private val braceL by literalToken("(")
    private val braceR by literalToken(")")
    private val areEqual by literalToken("==").map {
        { l: Any, r: Any -> l == r }
    }
    private val areNotEqual by literalToken("!=").map {
        { l: Any, r: Any -> l != r }
    }
    private val greaterThan by literalToken(">").map {
        { l: Any, r: Any -> (l as Int) > (r as Int) }
    }
    private val greaterThanEquals by literalToken(">=").map {
        { l: Any, r: Any -> (l as Int) >= (r as Int) }
    }
    private val lessThan by literalToken("<").map {
        { l: Any, r: Any -> (l as Int) < (r as Int) }
    }
    private val lessThanEquals by literalToken("<=").map {
        { l: Any, r: Any -> (l as Int) <= (r as Int) }
    }
    private val string by regexToken("\'[a-zA-Z0-9]+\'").map {
        it.text.trim('\'')
    }
    private val digitsToken by regexToken("[0-9]+").map {
        it.text.toInt()
    }
    private val andToken by literalToken("&&")
    private val orToken by literalToken("||")
    private val comma by literalToken(",")
    private val dot by literalToken(".").map { it.text }
    private val inToken by (literalToken("in") or literalToken("!in"))
        .map { it.text == "in" }
    private val plusToken by literalToken("+")
    private val minusToken by literalToken("-")
    private val mulToken by literalToken("*")
    private val divToken by literalToken("/")

    private val trueParser by literalToken("true") mapToF(true)
    private val falseParser by literalToken("false") mapToF(false)

    private fun Grammar<*>.functionCall(
        funcToken: String,
        vararg argumentsParser: PF,
        body: (Arguments) -> Any
    ): PF = -literalToken(funcToken) and
            (-braceL and
                    argumentsParser.toList().join(comma)
                    and -braceR
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
        by -varName and (-sqBrL and string and -sqBrR).map { field ->
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

    private val stringParser: PF by string.mapToF()

    private val doubleParser by
        (digitsToken and dot and digitsToken).map {
            "${it.t1}.${it.t3}".toDouble()
        }

    private val numberParser by doubleParser or digitsToken

    private val mulOrDivExpr: PF by leftAssociative(
        (varAccessParser or numberParser.mapToF()),
        (mulToken or divToken)
    ) { l, op, r ->
        val operation: (Number, Number) -> Number = if (op.token == mulToken) {
            Number::times
        } else {
            Number::div
        }
        val function = { it: VarType ->
             operation(
                 l(it) as Number,
                 r(it) as Number
             )
        }
        function
    }

    private val addOrSubExpr by leftAssociative(
        mulOrDivExpr,
        plusToken or minusToken
    ) { l, op, r ->
        val operation: (Number, Number) -> Number = if (op.token == plusToken) {
            Number::plus
        } else {
            Number::minus
        }
        val function = { it: VarType ->
            operation(
                l(it) as Number,
                r(it) as Number
            )
        }
        function
    }

    private val arithmeticExpr by addOrSubExpr

    private val term by stringParser or arithmeticExpr or trueParser or falseParser

    private val arrayExpr by parser {
        val value = split(
            string or numberParser,
            comma,
            allowEmpty = true,
            trailingSeparator = true
        ).toSet()
        value
    }.between(sqBrL, sqBrR)

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

    private val compareBoolExpr: PF by parser {
        val l = term()
        val compare = (
                greaterThanEquals or lessThanEquals or
                        lessThan or greaterThan or areEqual or areNotEqual
                )()
        val r = term()
        val function = { it: VarType ->
            val lValue = l(it)
            val rValue = r(it)
            compare(lValue, rValue)
        }
        function
    }

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

    private val boolExpr by compareBoolExpr or inArrayBoolExpr or varAccessParser

    private val andChain by leftAssociative(boolExpr, andToken) { l, r ->
        { it: VarType ->
            (l(it) as Boolean) && (r(it) as Boolean)
        }
    }
    private val orChain by leftAssociative(andChain, orToken) { l, r ->
        { it: VarType ->
            (l(it) as Boolean) || (r(it) as Boolean)
        }
    }

    private val expr by orChain

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