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
}

class MyGrammar : Grammar<Type>() {
    init {
        regexToken("\\s+", ignored = true)
    }

    val varName by literalToken("object")
    val sqBrL by literalToken("[")
    val sqBrR by literalToken("]")
    val areEqual by literalToken("==")
    val areNotEqual by literalToken("!=")
    val string by regexToken("\'[a-zA-Z0-9]+\'")
    val number by regexToken("[0-9]+")
    val andToken by literalToken("&&")
    val orToken by literalToken("||")
    val comma by literalToken(",")
    val inToken by literalToken("in")

    val varAccessParser: Parser<(VarType) -> Expr> by parser {
        varName()
        sqBrL()
        val field = string().text.drop(1).dropLast(1)
        sqBrR();
        { it: VarType ->
            val value = it[field]!!.jsonPrimitive
            when {
                value.intOrNull != null -> Expr.Num(value.jsonPrimitive.int)
                value.isString -> Expr.Str(value.content)
                value.booleanOrNull != null -> Expr.Bool(value.boolean)
                else -> throw RuntimeException("Unknown type")
            }
        }
    }

    val stringParser: Parser<(VarType) -> Expr> by string.map {
        { _: VarType ->
            Expr.Str(it.text.trim('\''))
        }
    }

    val numberParser: Parser<(VarType) -> Expr> by number.map {
        { _: VarType ->
            Expr.Num(it.text.toInt())
        }
    }

    val term by varAccessParser or stringParser or numberParser

    val inArrayBoolExpr by parser {
        val valueResolver = term()
        inToken()
        sqBrL()
        val set = split(string, comma, true, true).map {
            it.text.trim('\'')
        }.toSet()
        sqBrR()
        val function = { it: VarType ->
            val value = valueResolver(it)
            Expr.Bool(set.contains(value.getValue()))
        }
        function
    }

    val compareBoolExpr: Parser<(VarType) -> Expr> by parser {
        val l = term()
        val compare = (areEqual or areNotEqual).map {
            when(it.token.name) {
                areEqual.name -> { l: Any, r: Any -> l == r}
                areNotEqual.name -> { l: Any, r: Any -> l != r}
                else -> throw IllegalStateException()
            }
        }()
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

    val boolExpr by compareBoolExpr or inArrayBoolExpr

    /*val andExpr by parser {
        val l = boolExpr()
        orToken()
        val r = boolExpr()
        val function = { it: VarType ->
            val lValue = (l(it) as Expr.Bool).value
            val rValue = (r(it) as Expr.Bool).value
            Expr.Bool(lValue && rValue)
        }
        function
    }*/
    val andChain by leftAssociative(boolExpr, andToken) { l, r ->
        { it: VarType ->
            l(it) and r(it)
        }
    }
    val orChain by leftAssociative(andChain, orToken) { l, r ->
        { it: VarType ->
            l(it) or r(it)
        }
    }

    val expr by orChain or boolExpr

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