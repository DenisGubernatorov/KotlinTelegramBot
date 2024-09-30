package org.example

import java.io.File

data class Statistic(
    val learned: Int,
    val total: Int,
    val percent: Int,
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
    val isEmptyQuestion: Boolean = false,
)

class LearnWordTrainer {
    private val wordsFile = File("words.txt")
    private val dictionary = loadDictionary()
    private var wordsToLearn = updateWordsToLearn()

    fun getStatistics(): Statistic {
        val learned = dictionary.filter { it.correctAnswersCount >= 3 }.size
        val total = dictionary.size
        val percent = learned * 100 / total
        return Statistic(
            learned,
            total,
            percent,
        )
    }

    fun getQuestions(): List<Question> {
        val questions =
            wordsToLearn
                .map {
                    Question(getAnswerVariants(), it)
                }.toList()

        return questions
    }

    fun checkAnswer(
        answer: Int,
        question: Question,
    ): Boolean =
        if (answer - 1 == question.variants.indexOf(question.correctAnswer)) {
            question.correctAnswer.correctAnswersCount++
            saveDictionary()
            true
        } else {
            false
        }

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
                wordsToLearn.size > 4 -> wordsToLearn.take(wordsToLearn.size).shuffled()
                else -> {
                    val existingVariants = wordsToLearn.shuffled()
                    val additionalVariants =
                        dictionary.filter { it.correctAnswersCount >= 3 }.shuffled().take(4 - existingVariants.size)

                    existingVariants + additionalVariants
                }
            }

        return variants
    }

    private fun updateWordsToLearn() = dictionary.filter { it.correctAnswersCount < 3 }
}
