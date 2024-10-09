package org.example

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

const val HOST_ADDRESS = "https://api.telegram.org"

fun main(args: Array<String>) {
    val botToken: String = args[0]

    var updateId = 0
    while (true) {
        Thread.sleep(2000)
        val updates: String = getUpdates(botToken, updateId)
        println(updates)

        var matchResult: MatchResult? = "\"update_id\":(\\d+),".toRegex().find(updates)
        val groups: MatchGroupCollection? = matchResult?.groups
        val idStrValue: String = groups?.get(1)?.value ?: continue
        updateId = idStrValue.toInt().plus(1)

        matchResult = "\"text\":\"(.+?)\"".toRegex().find(updates)
        val messageText: String = matchResult?.groups?.get(1)?.value ?: continue
        println(messageText)
    }
}

private fun getUpdates(
    botToken: String,
    updateId: Int,
): String {
    val urlGetUpdates = "$HOST_ADDRESS/bot$botToken/getUpdates?offset=$updateId"
    val client: HttpClient = HttpClient.newBuilder().build()
    val updatesRequest: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
    val send: HttpResponse<String> = client.send(updatesRequest, HttpResponse.BodyHandlers.ofString())
    return send.body()
}
