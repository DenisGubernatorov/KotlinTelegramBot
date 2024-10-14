package org.example

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

class TelegramBotService {
    fun getUpdates(
        botToken: String,
        updateId: Int,
    ): String {
        val urlGetUpdates = "$HOST_ADDRESS/bot$botToken/getUpdates?offset=$updateId"
        val client: HttpClient = HttpClient.newBuilder().build()
        val updatesRequest: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val send: HttpResponse<String> = client.send(updatesRequest, HttpResponse.BodyHandlers.ofString())
        return send.body()
    }

    fun sendMessage(
        botToken: String,
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

    fun sendMenu(
        botToken: String,
        chatId: String,
    ): String {
        val sendMenuBody =
            """
            {
            	"chat_id": ${chatId.toInt()},
            	"text": "Основное меню",
            	"reply_markup": {
            		"inline_keyboard": [
            			[
            				{
            					"text": "Учить слова",
            					"callback_data": "learn_word_button"
            				},
            				{
            					"text": "Статистика",
            					"callback_data": "satistics_button"
            				}
            			]
            		]
            	}
            }
            """.trimIndent()
        val urlSendMessage = "$HOST_ADDRESS/bot$botToken/sendMessage"
        val client: HttpClient = HttpClient.newBuilder().build()
        val sendRequest: HttpRequest =
            HttpRequest
                .newBuilder()
                .uri(URI.create(urlSendMessage))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
                .build()
        return client.send(sendRequest, HttpResponse.BodyHandlers.ofString()).body()
    }
}
