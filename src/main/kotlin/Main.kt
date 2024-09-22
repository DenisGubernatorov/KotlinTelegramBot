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
        when (val selectedMode = readln()) {
            "1" -> println(selectedMode)

            "2" -> {
                val learnedWords = dictionary.filter { it.correctAnswersCount >= 3 }
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
