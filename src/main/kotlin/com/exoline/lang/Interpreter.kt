package com.exoline.lang

import kotlinx.serialization.json.*
import me.alllex.parsus.parser.*
import me.alllex.parsus.token.literalToken
import me.alllex.parsus.token.regexToken

typealias VarType = JsonObject
typealias Type = (VarType) -> Boolean

sealed class Expr {
    abstract fun getValue(): Any
    data class Str(val value: String) : Expr() {
        override fun getValue(): Any = value
    }
    data class Num(val value: Number) : Expr() {
        override fun getValue(): Any = value
    }
    data class Bool(val value: Boolean): Expr() {
        override fun getValue(): Any = value
    }
    data class Array(val value: Set<Any>): Expr() {
        override fun getValue(): Any = value
    }
}

class Interpreter : Grammar<Type>() {
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
    private val string by regexToken("\'[a-zA-Z0-9]+\'").map {
        it.text.trim('\'')
    }
    private val number by regexToken("[0-9]+").map {
        it.text.toInt()
    }
    private val andToken by literalToken("&&")
    private val orToken by literalToken("||")
    private val comma by literalToken(",")
    private val inToken by (literalToken("in") or literalToken("!in")).map {
        it.text == "in"
    }
    private val plusToken by literalToken("+")
    private val minusToken by literalToken("-")
    private val mulToken by literalToken("*")
    private val divToken by literalToken("/")
    private val trueParser by literalToken("true").map {
        { it: VarType ->
            Expr.Bool(true)
        }
    }
    private val falseParser by literalToken("false").map {
        { it: VarType ->
            Expr.Bool(false)
        }
    }

    // function tokens
    private val sizeFunction: Parser<(List<Set<Any>>) -> (VarType) -> Expr> by literalToken("size").map {
        { it: List<Set<Any>> ->
            { _: VarType ->
                Expr.Num(it.first().size)
            }
        }
    }
    // end of function tokens

    private fun functionCall(
        funcToken: Parser<(List<Any>) -> (VarType) -> Expr>,
        argumentsParser: Parser<List<(VarType) ->Expr>>
    ): Parser<(VarType) -> Expr> = parser {
        val func = funcToken()
        braceL()
        val argumentsResolvers = argumentsParser()
        braceR()
        val function = { it: VarType ->
            val arguments = argumentsResolvers.map { arg ->
                arg(it).getValue()
            }
            func(arguments)(it)
        }
        function
    }

    private val varAccessParser: Parser<(VarType) -> Expr> by parser {
        varName()
        sqBrL()
        val field = string()
        sqBrR()
        val function = { it: VarType ->
            val value = it[field]!!.jsonPrimitive
            when {
                value.intOrNull != null -> Expr.Num(value.jsonPrimitive.int)
                value.isString -> Expr.Str(value.content)
                value.booleanOrNull != null -> Expr.Bool(value.boolean)
                else -> throw RuntimeException("Unknown type")
            }
        }
        function
    }

    private val stringParser: Parser<(VarType) -> Expr> by string.map {
        { _: VarType ->
            Expr.Str(it)
        }
    }

    private val numberParser: Parser<(VarType) -> Expr> by number.map {
        { _: VarType ->
            Expr.Num(it)
        }
    }

    private val mulOrDivExpr by leftAssociative(
        (varAccessParser or numberParser),
        (mulToken or divToken)
    ) { l, op, r ->
        val operation: (Number, Number) -> Number = if (op.token == mulToken) {
            Number::times
        } else {
            Number::div
        }
        val function = { it: VarType ->
             Expr.Num(operation(
                 l(it).getValue() as Number,
                 r(it).getValue() as Number)
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
            Expr.Num(operation(
                l(it).getValue() as Number,
                r(it).getValue() as Number)
            )
        }
        function
    }

    private val arithmeticExpr by addOrSubExpr

    private val term by stringParser or arithmeticExpr or trueParser or falseParser

    private val arrayExpr by parser {
        sqBrL()
        val value = split(
            string or number,
            comma,
            allowEmpty = true,
            trailingSeparator = true
        ).toSet()
        sqBrR()
        value
    }

    private val inArrayBoolExpr by parser {
        val valueResolver = term()
        val isIn = inToken()
        val set = arrayExpr()
        val function = { it: VarType ->
            val value = valueResolver(it)
            Expr.Bool(set.contains(value.getValue()).xor(!isIn))
        }
        function
    }

    private val compareBoolExpr: Parser<(VarType) -> Expr> by parser {
        val l = term()
        val compare = (areEqual or areNotEqual)()
        val r = term()
        val function = { it: VarType ->
            val lValue = l(it)
            val rValue = r(it)
            val value = if (lValue is Expr.Str && rValue is Expr.Str) {
                compare(lValue.value, rValue.value)
            } else if (lValue is Expr.Num && rValue is Expr.Num) {
                compare(lValue.value, rValue.value)
            } else if(lValue is Expr.Bool && rValue is Expr.Bool) {
                compare(lValue.value, rValue.value)
            } else {
                false
            }
            Expr.Bool(value)
        }
        function
    }

    private val sizeCallParser = functionCall(sizeFunction as Parser<(List<Any>) -> (VarType) -> Expr>, arrayExpr.map { array ->
        listOf { it: VarType ->
            Expr.Array(array)
        }
    })

    private val boolExpr by compareBoolExpr or inArrayBoolExpr or varAccessParser

    private val andChain by leftAssociative(boolExpr, andToken) { l, r ->
        { it: VarType ->
            l(it) and r(it)
        }
    }
    private val orChain by leftAssociative(andChain, orToken) { l, r ->
        { it: VarType ->
            l(it) or r(it)
        }
    }

    private val expr by orChain

    override val root: Parser<Type> by parser {
        val result = expr()
        val function = { it: VarType ->
            val value = result(it)
            if (value !is Expr.Bool) {
                throw RuntimeException("OMG!")
            }
            value.value
        }
        function
    }
}