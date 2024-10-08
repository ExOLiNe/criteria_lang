package com.exoline.lang

import kotlinx.serialization.json.*
import me.alllex.parsus.parser.*
import me.alllex.parsus.token.LiteralToken
import me.alllex.parsus.token.literalToken
import me.alllex.parsus.token.regexToken

typealias VarType = JsonObject
typealias Type = (VarType) -> Boolean
typealias Arguments = List<Any>

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
    private val number by regexToken("[0-9]+").map {
        it.text.toInt()
    }
    private val andToken by literalToken("&&")
    private val orToken by literalToken("||")
    private val comma by literalToken(",")
    private val inToken by (literalToken("in") or literalToken("!in"))
        .map { it.text == "in" }
    private val plusToken by literalToken("+")
    private val minusToken by literalToken("-")
    private val mulToken by literalToken("*")
    private val divToken by literalToken("/")

    private val trueParser by literalToken("true") mapToLambda(true)
    private val falseParser by literalToken("false") mapToLambda(false)

    // function tokens
    private val sizeFunction by literalToken("size")
    // end of function tokens

    private fun functionCall(
        funcToken: LiteralToken,
        vararg argumentsParser: Parser<(VarType) -> Any>,
        body: (Arguments) -> Any
    ): Parser<(VarType) -> Any> = parser {
        val func = funcToken.map {
            { args: Arguments ->
                body(args)
            }
        }()
        braceL()
        val argumentsResolvers = argumentsParser.map {
            it()
        }
        braceR()
        val function = { it: VarType ->
            val arguments = argumentsResolvers.map { arg ->
                arg(it)
            }
            func(arguments)
        }
        function
    }

    private val varAccessParser: Parser<(VarType) -> Any> by parser {
        varName()
        sqBrL()
        val field = string()
        sqBrR()
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

    private val stringParser: Parser<(VarType) -> Any> by string.mapToLambda()

    private val numberParser: Parser<(VarType) -> Any> by number.mapToLambda()

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
            string or number,
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

    private val compareBoolExpr: Parser<(VarType) -> Any> by parser {
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

    private val sizeCallParser = functionCall(
        sizeFunction,
        arrayExpr.mapToLambda()
    ) { args ->
        (args.first() as Set<*>).size
    }

    private val strLengthCallParser = functionCall(
        sizeFunction,
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

    override val root: Parser<Type> by parser {
        val result = expr()
        val function: Type = { it ->
            val value = result(it)
            if (value !is Boolean) {
                throw RuntimeException("OMG!")
            }
            value
        }
        function
    }
}