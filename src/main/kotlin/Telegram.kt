package org.example

const val HOST_ADDRESS = "https://api.telegram.org"
const val COMMAND_START = "/start"
const val ALL_WORDS_LEARNED_MESSAGE = "Вы выучили все слова в базе"

fun main(args: Array<String>) {
    val botService = TelegramBotService(args[0])
    var updateId = 0
    val trainer = LearnWordTrainer()

    val updateQuery = "\"update_id\":(\\d+),".toRegex()
    val textValQuery = "\"text\":\"(.+?)\"".toRegex()
    val chatIdQuery = "\"chat\":\\{\"id\":(\\d+)".toRegex()
    val callBackQuery = "callback_query".toRegex()
    val clickedCallback = "\"data\":\"(.+?)\"".toRegex()
    val callbackAnswerId = "\"callback_query\":\\{\"id\":\"(\\d+)\"".toRegex()

    while (true) {
        Thread.sleep(2000)
        val updates: String = botService.getUpdates(updateId)
        println(updates)
        var matchResult: MatchResult? = updateQuery.find(updates)
        val idStrValue: String = getValueFromMatchResult(matchResult) ?: continue
        updateId = idStrValue.toInt().plus(1)

        matchResult = textValQuery.find(updates)
        val messageText: String =
            getValueFromMatchResult(matchResult)
                ?.let { getCorrectedStrVal(it) } ?: continue

        matchResult = chatIdQuery.find(updates)
        val chatId = getValueFromMatchResult(matchResult) ?: ""

        if (COMMAND_START == messageText.lowercase() && chatId.isNotBlank()) {
            botService.sendMenu(chatId)
        }

        val callBackAnswer = getValueFromMatchResult(callbackAnswerId.find(updates))
        if (callBackAnswer != null) {
            botService.answerCallbackQuery(callBackAnswer)
        }

        matchResult = callBackQuery.find(updates)
        if (matchResult != null) {
            matchResult = clickedCallback.find(updates)
            val callBackData = getValueFromMatchResult(matchResult)

            workByCommand(callBackData, chatId, botService, trainer)
        }
    }
}

private fun getValueFromMatchResult(matchResult: MatchResult?) = matchResult?.groups?.get(1)?.value

private fun workByCommand(
    callBackData: String?,
    chatId: String,
    botService: TelegramBotService,
    trainer: LearnWordTrainer,
) {
    when (callBackData) {
        LEARN_WORD_BUTTON -> workWithLearningCommand(chatId, botService, trainer)
        STATISTICS_BUTTON -> workWithStatisticsButton(chatId, botService, trainer)
    }
}

private fun workWithStatisticsButton(
    chatId: String,
    botService: TelegramBotService,
    trainer: LearnWordTrainer,
) {
    botService.sendMessage(chatId, trainer.getStatistics().toString())
}

private fun workWithLearningCommand(
    chatId: String,
    botService: TelegramBotService,
    trainer: LearnWordTrainer,
) {
    val question = trainer.getQuestion()

    if (question == null) {
        botService.sendMessage(chatId, ALL_WORDS_LEARNED_MESSAGE)
    } else {
        botService.sendQuestion(chatId, question)
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
