package org.example

import java.io.File

private val wordsFile = File("words.txt")
private val dictionary = mutableSetOf<Word>()

fun main() {
    wordsFile.createNewFile()

    val lines = wordsFile.readLines()
    lines.forEach {
        val values = it.split("|")
        val correctAnswersCount = values.getOrNull(2)?.toIntOrNull() ?: 0

        dictionary.add(Word(values[0], values[1], correctAnswersCount))
    }

    showMainMenu()

    while (true) {
        val learnedWords = dictionary.filter { it.correctAnswersCount >= 3 }.toSet()

        when (readln()) {
            "1" -> {
                val wordsToLearn = dictionary - learnedWords
                startLearning(wordsToLearn, learnedWords)
                println("Вы вышли из режима обучения. Выберите следующее действие")
                showMainMenu()
            }

            "2" -> {
                println(
                    "Выучено ${learnedWords.size} из ${dictionary.size} слов | ${(learnedWords.size / dictionary.size.toDouble() * 100).toInt()}%",
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

private fun startLearning(
    wordsToLearn: Set<Word>,
    learnedWords: Set<Word>,
) {
    if (wordsToLearn.isEmpty()) {
        println("Вы выучили все слова")
    } else {
        wordsToLearn.forEach {
            println("Слово: ${it.original}")
            println("Варианты ответа:")
            val variants =
                when {
                    wordsToLearn.size > 4 -> wordsToLearn.take(wordsToLearn.size).shuffled()
                    else -> {
                        val shuffled = wordsToLearn.shuffled()
                        val take = learnedWords.shuffled().take(4 - shuffled.size)
                        shuffled + take
                    }
                }
            println("0 - Выход")
            variants.forEachIndexed { index, word ->
                println("${index + 1} - ${word.translate}")
            }
            print("Введите вариант ответа: ")
            val answer = getCorrectAnswer()

            if (answer == 0) {
                return
            } else if (answer - 1 !in wordsToLearn.indices || it != variants.elementAt(answer - 1)) {
                println("Неправильно - ${it.original} [${it.translate}]")
            } else {
                println("Правильно!")
                it.correctAnswersCount++
                saveDictionary()
            }
            println()
        }
    }
}

private fun saveDictionary() {
    val updated = dictionary.joinToString("\n") { "${it.original}|${it.translate}|${it.correctAnswersCount}" }

    wordsFile.writeText(updated)
}

private fun getCorrectAnswer(): Int {
    val answer = readln()

    if (!answer.all { it.isDigit() } || answer.toInt() !in 0..4) {
        println("Некорректный номер. Повторите ввод")
        return getCorrectAnswer()
    }

    return answer.toInt()
}
