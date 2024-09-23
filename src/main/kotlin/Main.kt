package org.example

import java.io.File

fun main() {
    val dictionary = mutableSetOf<Word>()

    val wordsFile = File("words.txt")
    wordsFile.createNewFile()

    val lines = wordsFile.readLines()
    lines.forEach {
        val values = it.split("|")
        val correctAnswersCount = values.getOrNull(2)?.toIntOrNull() ?: 0

        dictionary.add(Word(values[0], values[1], correctAnswersCount))
    }

    println(
        """
        Меню:
        1 - Учить слова
        2 - Статистика
        3 - Выход
        """.trimIndent(),
    )

    while (true) {
        val learnedWords = dictionary.filter { it.correctAnswersCount >= 3 }.toSet()

        when (readln()) {
            "1" -> {
                val wordsToLearn = dictionary - learnedWords
                if (studyStageIn(wordsToLearn)) break
            }

            "2" -> {
                println(
                    "Выучено ${learnedWords.size} из ${dictionary.size} слов | ${(learnedWords.size / dictionary.size.toDouble() * 100).toInt()}%",
                )
            }

            "3" -> {
                println("Выход")
                break
            }

            else -> println("Неизвестный режим. Повторите ввод")
        }
    }
}

private fun studyStageIn(wordsToLearn: Set<Word>): Boolean {
    var isFinished = false
    if (wordsToLearn.isEmpty()) {
        println("Вы выучили все слова")
        isFinished = true
    } else {
        wordsToLearn.forEach {
            println("Слово: ${it.original}")
            println("Варианты ответа:")
            val variants = wordsToLearn.take(wordsToLearn.size).shuffled()
            variants.forEachIndexed { index, word -> println("${index + 1} -  ${word.translate} ") }
        }
    }
    return isFinished
}
