package com.exoline.lang

import com.exoline.lang.exception.RecursiveImportException
import com.exoline.lang.parser.AbstractGrammar
import com.exoline.lang.parser.ArithmeticParser
import com.exoline.lang.parser.BooleanParser
import com.exoline.lang.parser.FunctionParser
import me.alllex.parsus.parser.*

data class ParseResult(
    val fields: Set<String>,
    val app: AppF
) {
    data class AppResult(
        val result: Boolean,
        val identifiersValues: Map<String, Any?>
    )
}

class Interpreter(
    private val importResolver: ((ImportReference) -> ImportCode?)? = null
) : AbstractGrammar<ParseResult>(
    debugMode = true,
) {
    private val fields = mutableSetOf<String>()
    private val identifiers = mutableMapOf<String, F>()
    private val identifiersValues = mutableMapOf<String, Any?>()

    private val imports = mutableSetOf<String>()

    private fun resetState() {
        fields.clear()
        identifiers.clear()
        identifiersValues.clear()
        imports.clear()
    }

    private val fieldParser: Parser<String> by separated(string, divToken).map {
        it.joinToString(divToken.string)
    }.quotedParser()

    private val objectAccessParser: PF
        by -varName and (-sqBrL and fieldParser and -sqBrR).map { field ->
            fields += field
            val function = { it: VarType ->
                it.getRecursively(field)
            }
            function
        }

    private val identifierDefinition
        by (-buck and string and -assign and ref(::term)).map { (identifier, value) ->
            if (identifiers.contains(identifier)) {
                throw Exception("Redefinition of $identifier")
            }
            identifiers[identifier] = value
    }

    private val identifierAccess: Parser<F> by -buck and string.map { identifier ->
        (identifiers[identifier])?.let { identifierExpression ->
            // modified lambda(to save value of identifier's underlying expression
            val function: F = { it: VarType ->
                identifierExpression(it).apply {
                    identifiersValues[identifier] = this
                }
            }
            function
        } ?: throw Exception("Unknown identifier $identifier")
    }

    private val stringParser: PF by stringLiteral.mapToF()

    val functionParser = FunctionParser(ref(::term))

    val arithmeticExpr: Parser<F> = ArithmeticParser(listOf(
        objectAccessParser,
        identifierAccess,
        functionParser.root
    )).root

    val term: Parser<F> by
        stringParser or arithmeticExpr or nullToken.mapToF(null as Any?)

    private val inArrayBoolExpr by functionParser.infixFunctionCall("in", term, arrayExpr.mapToF()) { l, r ->
        l in (r as Set<Any>)
    } or functionParser.infixFunctionCall("!in", term, arrayExpr.mapToF()) { l, r ->
        l !in (r as Set<Any>)
    }

    private val likeExpr by functionParser.infixFunctionCall("like", term, startsWithLiteral.mapToF()) { l, r ->
        (l as? String?)?.startsWith(r as String) ?: false
    } or functionParser.infixFunctionCall("!like", term, startsWithLiteral.mapToF()) { l, r ->
        ((l as? String?)?.startsWith(r as String) ?: false).not()
    } or functionParser.infixFunctionCall("like", term, endsWithLiteral.mapToF()) { l, r ->
        (l as? String?)?.endsWith(r as String) ?: false
    } or functionParser.infixFunctionCall("!like", term, endsWithLiteral.mapToF()) { l, r ->
        ((l as? String?)?.endsWith(r as String) ?: false).not()
    }

    private val containsExpr by functionParser.infixFunctionCall("contains", term, term) { l, r ->
        (l as? Collection<*>?)?.contains(r) ?: false
    } or functionParser.infixFunctionCall("!contains", term, term) { l, r ->
        ((l as? Collection<*>?)?.contains(r) ?: false).not()
    }

    private val importStatement: Parser<String> by -importToken and string.map { importRef ->
        if (importRef in imports) {
            throw RecursiveImportException("Recursive imports not allowed")
        }
        imports += importRef
        importResolver?.invoke(importRef)?.let { codeText ->
            parseOrThrow(statement, codeText)
            importRef
        } ?: throw Exception("Import $importRef can't be resolved")
    }

    private val statement by
        zeroOrMore(ref(::importStatement) and -semicolon) and
        zeroOrMore(identifierDefinition and -semicolon)

    val boolExpr by BooleanParser(
        term,
        listOf(likeExpr, containsExpr, inArrayBoolExpr, objectAccessParser, identifierAccess)).root

    override val root: Parser<ParseResult> by parser {
        resetState()
        statement()
        val result = boolExpr()
        val function: AppF = { it ->
            val value = result(it)
            if (value !is Boolean) {
                throw RuntimeException("OMG!")
            }
            ParseResult.AppResult(value, identifiersValues)
        }
        ParseResult(
            fields = fields,
            app = function
        )
    }
}