package com.exoline.lang.parser

import com.exoline.lang.*
import me.alllex.parsus.parser.*
import me.alllex.parsus.token.literalToken
import java.time.LocalDateTime
import java.time.ZoneId

class FunctionParser(
    private val term: PF
) : AbstractGrammar<F>() {
    fun functionCall(
        funcToken: String,
        totalArgs: Int,
        vararg defaults: Any,
        body: (Arguments) -> Any?
    ): PF {
        if (totalArgs < defaults.size) {
            throw IllegalArgumentException("Total arguments must be more or equals than total defaults")
        }
        val defaultsStartFromIndex = totalArgs - defaults.size
        val args = parser {
            split(term, comma, allowEmpty = true, trailingSeparator = true)
        }

        return -literalToken(funcToken) and
                (-parL and args and -parR).map { argResolvers ->
                    if (argResolvers.size < defaultsStartFromIndex) {
                        throw IllegalArgumentException("Function $funcToken called with " +
                                "insufficient parameters. Expected: ${defaultsStartFromIndex}," +
                                "but got: ${argResolvers.size}")
                    }
                    val function = { it: VarType ->
                        val arguments = argResolvers.map { arg ->
                            arg.invoke(it)
                        }
                        val defaultsTotalNeed = totalArgs - arguments.size
                        body(arguments + defaults.takeLast(defaultsTotalNeed))
                    }
                    function
                }
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
        1
    ) { args ->
        val arg = args.first()
        when (arg) {
            is Collection<*> -> arg.size
            is String -> arg.length
            else -> throw Exception("Unknown type")
        }
    }

    val dateParser by functionCall(
        "date",
        6,
        1, 1, 0, 0, 0
    ) { args ->
        args as List<Int>
        LocalDateTime.of(args[0], args[1], args[2], args[3], args[4], args[5])
            .atZone(ZoneId.systemDefault()).toInstant()
    }

    val dummy by functionCall(
        "dummy",
        2,
        "defaultStr"
    ) { args ->
        (args[0] as String) + (args[1] as String)
    }

    override val root: Parser<F> by dateParser or sizeCallParser  or dummy
}