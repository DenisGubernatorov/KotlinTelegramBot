package org.example

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

const val LEARN_WORD_BUTTON = "learn_word_button"
const val STATISTICS_BUTTON = "statistics_button"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
const val CALLBACK_DATA_ANSWER_EXIT = 4
const val COMMAND_SEND_MESSAGE = "sendMessage"
const val COMMAND_ANSWER_CALL_BACK_QUERY = "answerCallbackQuery"

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long,
    @SerialName("text")
    val text: String,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup? = null,
)

@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: List<List<InlineKeyboard>>,
)

@Serializable
data class InlineKeyboard(
    @SerialName("callback_data")
    val callBackData: String,
    @SerialName("text")
    val text: String,
)

@Serializable
data class AnswerCallbackQueryRequest(
    @SerialName("callback_query_id")
    val callbackQueryId: Long,
)

class TelegramBotService(
    private val botToken: String,
) {
    private val client: HttpClient = HttpClient.newBuilder().build()
    val json =
        Json {
            ignoreUnknownKeys = true
        }

    fun getUpdates(updateId: Long): String {
        val urlGetUpdates = "$HOST_ADDRESS/bot$botToken/getUpdates?offset=$updateId"
        val updatesRequest: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val send: HttpResponse<String> = client.send(updatesRequest, HttpResponse.BodyHandlers.ofString())
        return send.body()
    }

    fun sendMessage(
        chatId: Long,
        messageText: String,
    ): String {
        val requestBody =
            SendMessageRequest(
                chatId = chatId,
                text = messageText,
            )
        val requestBodyStr = Json.encodeToString(requestBody)

        return client
            .send(getHttpRequest(COMMAND_SEND_MESSAGE, requestBodyStr), HttpResponse.BodyHandlers.ofString())
            .body()
    }

    fun sendMenu(chatId: Long): String {
        val requestBody =
            SendMessageRequest(
                chatId = chatId,
                text = "Основное меню",
                replyMarkup =
                    ReplyMarkup(
                        listOf(
                            listOf(
                                InlineKeyboard(text = "Учить слова", callBackData = LEARN_WORD_BUTTON),
                                InlineKeyboard(text = "Статистика", callBackData = STATISTICS_BUTTON),
                            ),
                        ),
                    ),
            )
        val requestBodyStr = json.encodeToString(requestBody)
        return client
            .send(getHttpRequest(COMMAND_SEND_MESSAGE, requestBodyStr), HttpResponse.BodyHandlers.ofString())
            .body()
    }

    private fun getHttpRequest(
        botCommand: String,
        sendMenuBody: String,
    ): HttpRequest {
        val urlSendMessage = "$HOST_ADDRESS/bot$botToken/$botCommand"
        val sendRequest: HttpRequest =
            HttpRequest
                .newBuilder()
                .uri(URI.create(urlSendMessage))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody, StandardCharsets.UTF_8))
                .build()
        return sendRequest
    }

    fun sendQuestion(
        chatId: Long,
        question: Question,
    ): String {
        val variants =
            question.variants
                .mapIndexed { index, word ->
                    listOf(
                        InlineKeyboard(text = word.translate, callBackData = "$CALLBACK_DATA_ANSWER_PREFIX$index"),
                    )
                }.plusElement(
                    listOf(
                        InlineKeyboard(
                            text = "выход",
                            callBackData = "$CALLBACK_DATA_ANSWER_PREFIX$CALLBACK_DATA_ANSWER_EXIT",
                        ),
                    ),
                )
        val requestBody =
            SendMessageRequest(
                chatId = chatId,
                text = "Выберите перевод: ${question.correctAnswer.original}",
                replyMarkup =
                    ReplyMarkup(variants),
            )
        val requestBodyStr = json.encodeToString(requestBody)
        val httpRequest = getHttpRequest(COMMAND_SEND_MESSAGE, requestBodyStr)
        return client.send(httpRequest, HttpResponse.BodyHandlers.ofString()).body()
    }

    fun answerCallbackQuery(callbackQueryId: Long): String {
        val requestBody =
            AnswerCallbackQueryRequest(
                callbackQueryId = callbackQueryId,
            )

        val requestBodyStr = json.encodeToString(requestBody)
        return client
            .send(getHttpRequest(COMMAND_ANSWER_CALL_BACK_QUERY, requestBodyStr), HttpResponse.BodyHandlers.ofString())
            .body()
    }
}
