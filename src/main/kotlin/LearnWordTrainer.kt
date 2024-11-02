package org.example

import java.io.File

data class Statistic(
    val learned: Int,
    val total: Int,
    val percent: Int,
) {
    override fun toString(): String = "Выучено $learned из $total слов | $percent%"
}

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)

class LearnWordTrainer(
    fileName: String = "words.txt",
    private val learnedAnswerCount: Int = 3,
    private val variantsCount: Int = 4,
) {
    private val wordsFile = File(fileName)
    private val dictionary = loadDictionary()
    private var wordsToLearn = updateWordsToLearn()
    var question: Question? = null

    fun getStatistics(): Statistic {
        val learned = dictionary.filter { it.correctAnswersCount >= learnedAnswerCount }.size
        val total = dictionary.size
        val percent = learned * 100 / total

        return Statistic(
            learned,
            total,
            percent,
        )
    }

    fun getNextQuestion(): Question? {
        wordsToLearn = updateWordsToLearn()
        if (wordsToLearn.isEmpty()) return null
        val variants = getAnswerVariants()
        question =
            Question(
                variants,
                variants.random(),
            )
        return question
    }

    fun checkAnswer(answer: Int?): Boolean =
        question?.let {
            val correctAnswerId = it.variants.indexOf(it.correctAnswer)
            if (correctAnswerId == answer) {
                it.correctAnswer.correctAnswersCount++
                saveDictionary()
                true
            } else {
                false
            }
        } ?: false

    private fun loadDictionary(): MutableSet<Word> {
        val dictionary = mutableSetOf<Word>()

        val lines = wordsFile.readLines()
        lines.forEach {
            val values = it.split("|")
            val correctAnswersCount = values.getOrNull(2)?.toIntOrNull() ?: 0
            dictionary.add(Word(values[0], values[1], correctAnswersCount))
        }

        return dictionary
    }

    private fun saveDictionary() {
        val updated = dictionary.joinToString("\n") { "${it.original}|${it.translate}|${it.correctAnswersCount}" }
        wordsToLearn = updateWordsToLearn()
        wordsFile.writeText(updated)
    }

    private fun getAnswerVariants(): List<Word> {
        val variants =
            when {
                wordsToLearn.size > variantsCount -> wordsToLearn.shuffled().take(variantsCount)
                else -> {
                    val existingVariants = wordsToLearn.shuffled()
                    val additionalVariants =
                        dictionary
                            .filter { it.correctAnswersCount >= learnedAnswerCount }
                            .shuffled()
                            .take(variantsCount - existingVariants.size)

                    (existingVariants + additionalVariants).shuffled()
                }
            }

        return variants
    }

    private fun updateWordsToLearn() = dictionary.filter { it.correctAnswersCount < learnedAnswerCount }
}

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int,
)
