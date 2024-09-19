package org.example

import java.io.File

fun main() {
    val dictionary = mutableSetOf<Word>()

    val wordsFile = File("words.txt")
    wordsFile.createNewFile()

    val lines = wordsFile.readLines()
    lines.forEach {
        val values = it.split("|")
        val correctAnswersCount = getCorrectAnswersCountValue(values)

        dictionary.add(Word(values[0], values[1], correctAnswersCount.toInt()))
    }

    dictionary.forEach {
        println(it.toString())
    }
}

private fun getCorrectAnswersCountValue(values: List<String>): String {
    val tmpResult = values.getOrNull(2) ?: "0"
    return if ((tmpResult).isBlank()) "0" else tmpResult
}
