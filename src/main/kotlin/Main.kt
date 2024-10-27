package org.example

fun Question.asConsoleString(): String {
    val result =
        this.variants
            .mapIndexed { index: Int, word: Word -> "${index + 1} - ${word.translate}" }
            .joinToString("\n")
    return "$result\n0 - Выход"
}

fun main() {
    val trainer =
        try {
            LearnWordTrainer()
        } catch (e: Exception) {
            println("Невозможно загрузить словарь")
            return
        }

    showMainMenu()

    while (true) {
        when (readln()) {
            "1" -> {
                startLearning(trainer)
                println("Вы вышли из режима обучения. Выберите следующее действие")
                showMainMenu()
            }

            "2" -> {
                val statistics = trainer.getStatistics()
                println(statistics.toString())
                println("Выберите режим для продолжения")
                showMainMenu()
            }

            "3" -> {
                println("Выход")
                break
            }

            else -> println("Неизвестный режим. Повторите ввод")
        }
    }
}

private fun showMainMenu() {
    println(
        """
        Меню:
        1 - Учить слова
        2 - Статистика
        3 - Выход
        """.trimIndent(),
    )
}

private fun startLearning(trainer: LearnWordTrainer) {
    val question = trainer.getQuestion()

    if (question == null) {
        println(ALL_WORDS_LEARNED_MESSAGE)
    } else {
        println("Слово: ${question.correctAnswer.original}")
        println("Варианты ответа:")

        println(question.asConsoleString())

        print("Введите вариант ответа: ")

        val answer = getCorrectAnswer()

        when {
            answer == 0 -> {
                return
            }

            trainer.checkAnswer(answer, question) -> {
                println("Правильно!")
            }

            else -> {
                println("Неправильно - ${question.correctAnswer.original} [${question.correctAnswer.translate}]")
            }
        }

        println()
    }
}

private fun getCorrectAnswer(): Int {
    val answer = readln()

    if (!answer.all { it.isDigit() } || answer.length > 1 || answer.toInt() !in 0..4) {
        println("Некорректный номер. Повторите ввод")
        return getCorrectAnswer()
    }

    return answer.toInt()
}
