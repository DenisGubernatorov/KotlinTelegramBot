package org.example

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

const val HOST_ADDRESS = "https://api.telegram.org"

fun main(args: Array<String>) {
    val botToken = args[0]

    val urlGetMe = "$HOST_ADDRESS/bot$botToken/getMe"
    val urlGetUpdates = "$HOST_ADDRESS/bot$botToken/getUpdates"

    val client: HttpClient = HttpClient.newBuilder().build()
    val requestBuilder: HttpRequest.Builder = HttpRequest.newBuilder()
    val request: HttpRequest = requestBuilder.uri(URI.create(urlGetMe)).build()
    val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
    println(response.body())

    val updates: HttpRequest = requestBuilder.uri(URI.create(urlGetUpdates)).build()
    println(updates)
}
