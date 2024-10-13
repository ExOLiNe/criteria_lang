package com.exoline.lang.parser

import com.exoline.lang.F
import com.exoline.lang.PF
import com.exoline.lang.VarType
import me.alllex.parsus.parser.*

class BooleanParser(
    term: Parser<F>,
    additionalParsers: List<Parser<F>>
) : AbstractGrammar<F>() {
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
    private val boolExpr by listOf((-parL and ref(::orChain) and -parR), trueParser, falseParser)
        .plus(additionalParsers)
        .fold(compareBoolExpr) { acc, parser ->
            acc or parser
        }

    private val andChain: Parser<F> by leftAssociative(boolExpr, andToken) { l, r ->
        { it: VarType ->
            (l(it) as Boolean) && (r(it) as Boolean)
        }
    }
    private val orChain: Parser<F> by leftAssociative(andChain, orToken) { l, r ->
        { it: VarType ->
            (l(it) as Boolean) || (r(it) as Boolean)
        }
    }

    private val expr by orChain

    override val root: Parser<F> by expr
}