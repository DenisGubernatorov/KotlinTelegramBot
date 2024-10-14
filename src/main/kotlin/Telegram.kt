package org.example

const val HOST_ADDRESS = "https://api.telegram.org"
const val COMMAND_START = "/start"

fun main(args: Array<String>) {
    val botToken: String = args[0]
    val botService = TelegramBotService()
    var updateId = 0

    val updateQuery = "\"update_id\":(\\d+),".toRegex()
    val textValQuery = "\"text\":\"(.+?)\"".toRegex()
    val chatIdQuery = "\"chat\":\\{\"id\":(\\d+)".toRegex()

    val trainer = LearnWordTrainer()

    while (true) {
        Thread.sleep(2000)
        val updates: String = botService.getUpdates(botToken, updateId)
        println(updates)

        var matchResult: MatchResult? = updateQuery.find(updates)
        val groups: MatchGroupCollection? = matchResult?.groups
        val idStrValue: String = groups?.get(1)?.value ?: continue
        updateId = idStrValue.toInt().plus(1)

        matchResult = textValQuery.find(updates)
        val messageText: String =
            matchResult
                ?.groups
                ?.get(1)
                ?.value
                ?.let { getCorrectedStrVal(it) } ?: continue

        matchResult = chatIdQuery.find(updates)
        val chatId = matchResult?.groups?.get(1)?.value ?: ""

        if (COMMAND_START == messageText.lowercase() && chatId.isNotBlank()) {
            botService.sendMenu(botToken, chatId)
        }
    }
}

/**
 * Функция используется для корректировки подаваемой строки, если в ней есть строковое представление символов в unicode, а не сами символы
 * unicode. Например, может понадобиться если кириллический текст в ответе бота представлен в описанном виде.
 * @param messageText - строка
 * @return - преобразованная строка
 */
private fun getCorrectedStrVal(messageText: String): String =
    messageText.replace(Regex("\\\\u([0-9a-fA-F]{4})")) {
        val codePoint = it.groupValues[1].toInt(16)
        codePoint.toChar().toString()
    }
