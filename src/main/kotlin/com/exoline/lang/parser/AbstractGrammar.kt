package com.exoline.lang.parser

import com.exoline.lang.compareTo
import com.exoline.lang.mapToF
import me.alllex.parsus.parser.*
import me.alllex.parsus.token.literalToken
import me.alllex.parsus.token.regexToken

abstract class AbstractGrammar<T>(
    debugMode: Boolean = true
) : Grammar<T>(debugMode = debugMode) {
    init { regexToken("\\s+", ignored = true) }

    protected val varName by literalToken("object")
    protected val sqBrL by literalToken("[")
    protected val sqBrR by literalToken("]")
    protected val parL by literalToken("(")
    protected val parR by literalToken(")")
    protected val areEqual by literalToken("==").map {
        { l: Any, r: Any -> l == r }
    }
    protected val areNotEqual by literalToken("!=").map {
        { l: Any, r: Any -> l != r }
    }
    protected val greaterThan by literalToken(">").map {
        { l: Any, r: Any -> (l as Number) > (r as Number) }
    }
    protected val greaterThanEquals by literalToken(">=").map {
        { l: Any, r: Any -> (l as Number) >= (r as Number) }
    }
    protected val lessThan by literalToken("<").map {
        { l: Any, r: Any -> (l as Number) < (r as Number) }
    }
    protected val lessThanEquals by literalToken("<=").map {
        { l: Any, r: Any -> (l as Number) <= (r as Number) }
    }
    protected val quote by literalToken("'")
    protected val doubleQuote by literalToken("\"")
    protected val string by regexToken("[a-zA-Z0-9]+")
    protected val stringLiteral by (
            (-quote and string and -quote)
                    or (-doubleQuote and string and -doubleQuote)
            ).map {
            it.text
        }
    protected val digitsToken by regexToken("[0-9]+").map {
        it.text.toInt()
    }
    protected val andToken by literalToken("&&")
    protected val orToken by literalToken("||")
    protected val comma by literalToken(",")
    protected val dot by literalToken(".").map { it.text }
    protected val inToken by (literalToken("in") or literalToken("!in"))
        .map { it.text == "in" }
    protected val plusToken by literalToken("+")
    protected val minusToken by literalToken("-")
    protected val mulToken by literalToken("*")
    protected val divToken by literalToken("/")

    protected val trueParser by literalToken("true") mapToF(true)
    protected val falseParser by literalToken("false") mapToF(false)

    protected val doubleParser by (digitsToken and dot and digitsToken).map {
        "${it.t1}.${it.t3}".toDouble()
    }

    protected val numberParser: Parser<Number> by doubleParser or digitsToken
}