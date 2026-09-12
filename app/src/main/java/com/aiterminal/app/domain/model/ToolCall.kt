package com.aiterminal.app.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class ToolCall(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val argumentsJson: String
) {
    fun parseArguments(): Map<String, String> {
        return try {
            val jsonElement = Json.parseToJsonElement(argumentsJson)
            val jsonObject = jsonElement.jsonObject
            jsonObject.mapValues { (_, value) ->
                try {
                    value.jsonPrimitive.content
                } catch (e: Exception) {
                    value.toString()
                }
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
