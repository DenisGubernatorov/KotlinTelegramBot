package org.example

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

const val HOST_ADDRESS = "https://api.telegram.org"
const val COMMAND_START = "/start"
const val ALL_WORDS_LEARNED_MESSAGE = "Вы выучили все слова в базе"

@Serializable
data class Update(
    @SerialName("update_id")
    val updateId: Long,
    @SerialName("message")
    val message: Message? = null,
    @SerialName("callback_query")
    val callbackQuery: CallbackQuery? = null,
)

@Serializable
data class Response(
    @SerialName("result")
    val result: List<Update>,
)

@Serializable
data class Message(
    @SerialName("text")
    val text: String,
    @SerialName("chat")
    val chat: Chat,
)

@Serializable
data class Chat(
    @SerialName("id")
    val id: Long,
)

@Serializable
data class CallbackQuery(
    @SerialName("data")
    val data: String? = null,
    @SerialName("message")
    val message: Message? = null,
    @SerialName("id")
    val callbackAnswerId: Long,
)

fun main(args: Array<String>) {
    val botService = TelegramBotService(args[0])
    var lastUpdateId = 0L
    val trainer = LearnWordTrainer()

    while (true) {
        Thread.sleep(2000)
        val updatesStrVal: String = botService.getUpdates(lastUpdateId)
        println(updatesStrVal)
        val response: Response = botService.json.decodeFromString(updatesStrVal)
        val updates = response.result
        val firstUpdate = updates.firstOrNull() ?: continue
        val updateId = firstUpdate.updateId
        lastUpdateId = updateId + 1

        val messageText: String? = firstUpdate.message?.text

        val chatId =
            firstUpdate.message?.chat?.id ?: firstUpdate.callbackQuery
                ?.message
                ?.chat
                ?.id

        if (COMMAND_START == messageText?.lowercase()) {
            chatId?.let { botService.sendMenu(it) }
        }

        firstUpdate.callbackQuery?.callbackAnswerId?.let { botService.answerCallbackQuery(it) }

        val callBackData = firstUpdate.callbackQuery?.data

        chatId?.let { workByCommand(callBackData, it, botService, trainer) }
    }
}

private fun workByCommand(
    callBackData: String?,
    chatId: Long,
    botService: TelegramBotService,
    trainer: LearnWordTrainer,
) {
    when (callBackData) {
        LEARN_WORD_BUTTON -> checkNextQuestionAndSend(chatId, botService, trainer)
        STATISTICS_BUTTON -> getStatisticBot(chatId, botService, trainer)
        else -> {
            if (callBackData != null && callBackData.startsWith(CALLBACK_DATA_ANSWER_PREFIX)) {
                val index = callBackData.substringAfter(CALLBACK_DATA_ANSWER_PREFIX)
                checkAnswer(chatId, botService, trainer, index)
                checkNextQuestionAndSend(chatId, botService, trainer)
            }
        }
    }
}

fun checkAnswer(
    chatId: Long,
    botService: TelegramBotService,
    trainer: LearnWordTrainer,
    index: String,
) {
    val isCorrect = trainer.checkAnswer(index.toInt())
    botService.sendMessage(
        chatId,
        if (isCorrect) {
            "Правильно"
        } else {
            "Неправильно - ${trainer.question?.correctAnswer?.original} [${trainer.question?.correctAnswer?.translate}]"
        },
    )
}

private fun getStatisticBot(
    chatId: Long,
    botService: TelegramBotService,
    trainer: LearnWordTrainer,
) {
    botService.sendMessage(chatId, trainer.getStatistics().toString())
}

private fun checkNextQuestionAndSend(
    chatId: Long,
    botService: TelegramBotService,
    trainer: LearnWordTrainer,
) {
    val question = trainer.getNextQuestion()

    if (question == null) {
        botService.sendMessage(chatId, ALL_WORDS_LEARNED_MESSAGE)
    } else {
        botService.sendQuestion(chatId, question)
    }
}
