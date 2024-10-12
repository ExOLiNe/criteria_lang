package com.exoline.lang.parser

import com.exoline.lang.*
import me.alllex.parsus.parser.*
import me.alllex.parsus.token.literalToken

class FunctionParser(
    term: PF
) : AbstractGrammar<F>() {
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

    val strLengthCallParser by functionCall(
        "size",
        term
    ) { args ->
        (args.first() as String).length
    }

    val dummy by functionCall(
        "dummy",
        term
    ) { args ->
        args.first()
    }

    override val root: Parser<F> by strLengthCallParser or dummy
}