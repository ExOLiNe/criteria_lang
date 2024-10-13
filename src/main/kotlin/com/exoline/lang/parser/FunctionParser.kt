package com.exoline.lang.parser

import com.exoline.lang.*
import me.alllex.parsus.parser.*
import me.alllex.parsus.token.literalToken

class FunctionParser(
    term: PF
) : AbstractGrammar<F>() {
    fun functionCall(
        funcToken: String,
        vararg argumentsParser: PF,
        body: (Arguments) -> Any?
    ): PF = -literalToken(funcToken) and
            (-parL and argumentsParser.toList().join(comma) and -parR).map { argResolvers ->
                    val function = { it: VarType ->
                        val arguments = argResolvers.map { arg ->
                            arg(it)
                        }
                        body(arguments)
                    }
                    function
                }

    fun infixFunctionCall(
        funcToken: String,
        argumentL: PF,
        argumentR: PF,
        body: (Any?, Any?) -> Any?
    ): PF = (argumentL and -literalToken(funcToken) and argumentR).map { (argLResolver, argRResolver) ->
        val function = { it: VarType ->
            val argL = argLResolver(it)
            val argR = argRResolver(it)
            body(argL, argR)
        }
        function
    }

    val sizeCallParser by functionCall(
        "size",
        term
    ) { args ->
        val arg = args.first()
        when (arg) {
            is Collection<*> -> arg.size
            is String -> arg.length
            else -> throw Exception("Unknown type")
        }
    }

    val dummy by functionCall(
        "dummy",
        term
    ) { args ->
        args.first()
    }

    override val root: Parser<F> by sizeCallParser or dummy
}