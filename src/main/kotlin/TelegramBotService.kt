package org.example

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

const val LEARN_WORD_BUTTON = "learn_word_button"
const val STATISTICS_BUTTON = "statistics_button"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
const val COMMAND_SEND_MESSAGE = "sendMessage"
const val COMMAND_ANSWER_CALL_BACK_QUERY = "answerCallbackQuery"

class TelegramBotService(
    private val botToken: String,
) {
    private val client: HttpClient = HttpClient.newBuilder().build()

    fun getUpdates(updateId: Int): String {
        val urlGetUpdates = "$HOST_ADDRESS/bot$botToken/getUpdates?offset=$updateId"
        val updatesRequest: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val send: HttpResponse<String> = client.send(updatesRequest, HttpResponse.BodyHandlers.ofString())
        return send.body()
    }

    fun sendMessage(
        chatId: String,
        messageText: String,
    ): String {
        println(messageText)
        val urlSendMessage = "$HOST_ADDRESS/bot$botToken/sendMessage?chat_id=$chatId&text=${
            URLEncoder.encode(
                messageText,
                StandardCharsets.UTF_8,
            )
        }"
        val client: HttpClient = HttpClient.newBuilder().build()
        val sendRequest: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        return client.send(sendRequest, HttpResponse.BodyHandlers.ofString()).body()
    }

    fun sendMenu(chatId: String): String {
        val sendMenuBody =
            """
            {
            	"chat_id": $chatId,
            	"text": "Основное меню",
            	"reply_markup": {
            		"inline_keyboard": [
            			[
            				{
            					"text": "Учить слова",
            					"callback_data": "$LEARN_WORD_BUTTON"
            				},
            				{
            					"text": "Статистика",
            					"callback_data": "$STATISTICS_BUTTON"
            				}
            			]
            		]
            	}
            }
            """.trimIndent()
        return client
            .send(getHttpRequest(COMMAND_SEND_MESSAGE, sendMenuBody), HttpResponse.BodyHandlers.ofString())
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
        chatId: String,
        question: Question,
    ): String {
        val buttons =
            question.variants
                .mapIndexed { index, word ->
                    """
                    [{
                        "text": "${word.translate}",
                        "callback_data": "${CALLBACK_DATA_ANSWER_PREFIX + index}"
                    }]
                    """.trimIndent()
                }.joinToString(",\n")
        val sendMenuBody =
            """
            {
            	"chat_id": $chatId,
            	"text": "Выберите перевод: ${question.correctAnswer.original}",
            	"reply_markup": {
            		"inline_keyboard":[
                                        $buttons            
                                      ]            
            	}
            }
            """.trimIndent()
        val httpRequest = getHttpRequest(COMMAND_SEND_MESSAGE, sendMenuBody)
        return client.send(httpRequest, HttpResponse.BodyHandlers.ofString()).body()
    }

    fun answerCallbackQuery(callbackQueryId: String): String {
        val body =
            """
            {                         
                "callback_query_id": "$callbackQueryId",                
            }
            """.trimIndent()

        return client
            .send(getHttpRequest(COMMAND_ANSWER_CALL_BACK_QUERY, body), HttpResponse.BodyHandlers.ofString())
            .body()
    }
}
