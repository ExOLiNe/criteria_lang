package com.exoline.lang

import me.alllex.parsus.parser.Parser
import me.alllex.parsus.parser.and
import me.alllex.parsus.parser.map
import me.alllex.parsus.parser.unaryMinus
import me.alllex.parsus.token.Token

inline infix fun <reified T, reified P> Parser<T>.mapToF(value: P): Parser<(VarType) -> P> = map {
    value.toLambda()
}
inline fun <reified T> Parser<T>.mapToF(): Parser<(VarType) -> T> = map {
    it.toLambda()
}
inline fun <reified T>T.toLambda(): (VarType) -> T = { _: VarType ->
    this
}

fun <T>List<Parser<T>>.join(
    separator: Token
): Parser<List<T>> = drop(1).fold(this[0].map { listOf(it) }) { acc, p ->
    (acc and -separator and p).map { (list, item) ->
        list + item
    }
}