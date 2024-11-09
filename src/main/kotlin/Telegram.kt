package org.example

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

const val HOST_ADDRESS = "https://api.telegram.org"
const val COMMAND_START = "/start"
const val ALL_WORDS_LEARNED_MESSAGE = "Вы выучили все слова в базе"
const val EXIT_LEARNING_MODE_MESSAGE = "Вы вышли из режима обучения. Для продолжения перезапустите бота"

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
    val text: String = "",
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
    val trainers = HashMap<Long, LearnWordTrainer>()
    var lastUpdateId = 0L

    while (true) {
        Thread.sleep(2000)
        val updatesStrVal: String = botService.getUpdates(lastUpdateId)
        println(updatesStrVal)
        val response: Response = botService.json.decodeFromString(updatesStrVal)
        if (response.result.isEmpty()) continue
        val sortedUpdates = response.result.sortedBy { it.updateId }
        sortedUpdates.forEach { handleUpdate(it, botService, trainers) }
        lastUpdateId = sortedUpdates.last().updateId + 1
    }
}

fun handleUpdate(
    update: Update,
    botService: TelegramBotService,
    trainers: HashMap<Long, LearnWordTrainer>,
) {
    val messageText: String? = update.message?.text

    val chatId =
        update.message?.chat?.id ?: update.callbackQuery
            ?.message
            ?.chat
            ?.id
            ?: return

    val trainer = trainers.getOrPut(chatId) { LearnWordTrainer("${chatId}_dic.txt") }

    if (COMMAND_START == messageText?.lowercase()) {
        botService.sendMenu(chatId)
    }

    update.callbackQuery?.callbackAnswerId?.let { botService.answerCallbackQuery(it) }

    val callBackData = update.callbackQuery?.data

    workByCommand(callBackData, chatId, botService, trainer)
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
        RESET_DATA_BUTTON -> resetProgress(chatId, botService, trainer)
        else -> {
            if (callBackData != null && callBackData.startsWith(CALLBACK_DATA_ANSWER_PREFIX)) {
                val index = callBackData.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()

                if (index == CALLBACK_DATA_ANSWER_EXIT) {
                    botService.sendMessage(chatId, EXIT_LEARNING_MODE_MESSAGE)
                    return
                }

                checkAnswer(chatId, botService, trainer, index)
                checkNextQuestionAndSend(chatId, botService, trainer)
            }
        }
    }
}

fun resetProgress(
    chatId: Long,
    botService: TelegramBotService,
    trainer: LearnWordTrainer,
) {
    trainer.resetProgress()
    botService.sendMessage(chatId, "Прогресс сброшен")
}

fun checkAnswer(
    chatId: Long,
    botService: TelegramBotService,
    trainer: LearnWordTrainer,
    index: Int,
) {
    val isCorrect = trainer.checkAnswer(index)
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
