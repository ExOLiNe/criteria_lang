package com.exoline.lang

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import me.alllex.parsus.parser.Parser

typealias JObject = ObjectNode
typealias JElement = JsonNode
typealias JArray = ArrayNode

typealias VarType = JObject
typealias AppF = (VarType) -> ParseResult.AppResult
typealias F = (VarType) -> Any?
typealias BoolF = (VarType) -> Boolean
typealias PF = Parser<F>
typealias Arguments = List<Any?>
typealias ImportReference = String
typealias ImportCode = String