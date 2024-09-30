package org.example

fun Question.asConsoleString(): String =
    this.variants
        .mapIndexed { index: Int, word: Word -> "${index + 1} - ${word.translate}" }
        .joinToString("\n 0 - Выход ")

fun main() {
    val trainer = LearnWordTrainer()

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
                println(
                    "Выучено ${statistics.learned} из ${statistics.total} слов | ${statistics.percent}%",
                )
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
    val questions = trainer.getQuestions()

    if (questions.isEmpty()) {
        println("Вы выучили все слова")
    } else {
        questions.forEach {
            println("Слово: ${it.correctAnswer.original}")
            println("Варианты ответа:")

            println(it.asConsoleString())
            println("0 - Выход")

            print("Введите вариант ответа: ")

            val answer = getCorrectAnswer()

            when {
                answer == 0 -> {
                    return
                }

                trainer.checkAnswer(answer, it) -> {
                    println("Правильно!")
                }

                else -> {
                    println("Неправильно - ${it.correctAnswer.original} [${it.correctAnswer.translate}]")
                }
            }

            println()
        }
    }
}

private fun getCorrectAnswer(): Int {
    val answer = readln()

    if (!answer.all { it.isDigit() } || answer.toInt() !in 0..4) {
        println("Некорректный номер. Повторите ввод")
        return getCorrectAnswer()
    }

    return answer.toInt()
}
