package com.exoline.lang

infix fun Expr.and(other: Expr): Expr.Bool = (if (this is Expr.Bool && other is Expr.Bool) {
    value && other.value
} else {
    false
}).let {
    Expr.Bool(it)
}

infix fun Expr.or(other: Expr): Expr.Bool = (if (this is Expr.Bool && other is Expr.Bool) {
    value || other.value
} else {
    false
}).let {
    Expr.Bool(it)
}