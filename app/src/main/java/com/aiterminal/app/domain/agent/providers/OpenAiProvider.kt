package com.aiterminal.app.domain.agent.providers

import com.aiterminal.app.core.common.AppResult
import com.aiterminal.app.domain.model.ChatMessage
import com.aiterminal.app.domain.model.MessageRole
import com.aiterminal.app.domain.model.ProviderConfig
import com.aiterminal.app.domain.model.ToolCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class OpenAiProvider(
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build(),
    private val json: Json = Json { ignoreUnknownKeys = true }
) : ILlmProvider {

    override val providerId: String = "openai"

    override suspend fun generateResponse(
        messages: List<ChatMessage>,
        toolsJson: String,
        apiKey: String,
        config: ProviderConfig
    ): AppResult<LlmResponse> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext AppResult.Error("OpenAI API Key is missing. Please add your key in Settings.")
        }

        try {
            val endpoint = "${config.baseUrl.removeSuffix("/")}/chat/completions"

            // Construct messages array
            val messagesArray = buildJsonArray {
                for (msg in messages) {
                    add(buildJsonObject {
                        val roleStr = when (msg.role) {
                            MessageRole.SYSTEM -> "system"
                            MessageRole.USER -> "user"
                            MessageRole.ASSISTANT -> "assistant"
                            MessageRole.TOOL -> "tool"
                        }
                        put("role", roleStr)
                        put("content", msg.content)

                        if (msg.role == MessageRole.TOOL && msg.toolCallId != null) {
                            put("tool_call_id", msg.toolCallId)
                        }

                        if (msg.role == MessageRole.ASSISTANT && msg.toolCalls.isNotEmpty()) {
                            put("tool_calls", buildJsonArray {
                                for (tc in msg.toolCalls) {
                                    add(buildJsonObject {
                                        put("id", tc.id)
                                        put("type", "function")
                                        put("function", buildJsonObject {
                                            put("name", tc.name)
                                            put("arguments", tc.argumentsJson)
                                        })
                                    })
                                }
                            })
                        }
                    })
                }
            }

            // Construct payload
            val rootPayload = buildJsonObject {
                put("model", config.model)
                put("messages", messagesArray)
                put("max_tokens", config.maxTokens)

                if (config.supportsTools && toolsJson.isNotBlank()) {
                    try {
                        val toolsParsed = json.parseToJsonElement(toolsJson).jsonArray
                        put("tools", toolsParsed)
                        put("tool_choice", "auto")
                    } catch (_: Exception) {}
                }
            }

            val requestBody = rootPayload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext AppResult.Error(
                    "OpenAI API Error (${response.code}): $responseBody"
                )
            }

            // Parse response
            val rootElement = json.parseToJsonElement(responseBody).jsonObject
            val choices = rootElement["choices"]?.jsonArray
                ?: return@withContext AppResult.Error("Invalid response format: missing 'choices'")

            if (choices.isEmpty()) {
                return@withContext AppResult.Error("No completion choices returned by model")
            }

            val firstChoice = choices[0].jsonObject
            val messageObj = firstChoice["message"]?.jsonObject
                ?: return@withContext AppResult.Error("Missing 'message' in choice")

            val textContent = messageObj["content"]?.let {
                try { it.jsonPrimitive.content } catch (_: Exception) { null }
            }
            val finishReason = firstChoice["finish_reason"]?.let {
                try { it.jsonPrimitive.content } catch (_: Exception) { null }
            }

            val parsedToolCalls = mutableListOf<ToolCall>()
            val toolCallsArray = messageObj["tool_calls"]?.let {
                try { it.jsonArray } catch (_: Exception) { null }
            }

            toolCallsArray?.forEach { tcElem ->
                try {
                    val tcObj = tcElem.jsonObject
                    val callId = tcObj["id"]?.jsonPrimitive?.content ?: java.util.UUID.randomUUID().toString()
                    val funcObj = tcObj["function"]?.jsonObject
                    val funcName = funcObj?.get("name")?.jsonPrimitive?.content ?: ""
                    val argsString = funcObj?.get("arguments")?.jsonPrimitive?.content ?: "{}"

                    if (funcName.isNotBlank()) {
                        parsedToolCalls.add(
                            ToolCall(
                                id = callId,
                                name = funcName,
                                argumentsJson = argsString
                            )
                        )
                    }
                } catch (_: Exception) {}
            }

            AppResult.Success(
                LlmResponse(
                    content = textContent,
                    toolCalls = parsedToolCalls,
                    finishReason = finishReason
                )
            )
        } catch (e: Exception) {
            AppResult.Error("Failed to call OpenAI API: ${e.message}", e)
        }
    }
}
