package com.exoline.lang.parser

import com.exoline.lang.*
import me.alllex.parsus.parser.*

class ArithmeticParser(
    varAccessParser: Parser<F>
) : AbstractGrammar<F>() {
    private val term by varAccessParser or numberParser.mapToF() or (-parL and ref(::addOrSubExpr) and -parR)

    private val mulOrDivExpr: PF by leftAssociative(
        term,
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

    override val root: Parser<F> by addOrSubExpr
}