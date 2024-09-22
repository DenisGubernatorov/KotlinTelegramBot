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

    dictionary.forEach {
        println(it.toString())
    }
}
