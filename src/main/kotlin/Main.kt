package org.example

import java.io.File

fun main() {
    val wordsFile = File("words.txt")
    wordsFile.createNewFile()

    val lines = wordsFile.readLines()
    lines.forEach { println(it) }
}
