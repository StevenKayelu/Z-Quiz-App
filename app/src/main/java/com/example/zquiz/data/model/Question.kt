package com.example.zquiz.data.model

data class Question(
    val id: Int,
    val category: String,
    val difficulty: String,
    val question: String,
    val choices: List<String>,
    val answer: String,
    val explanation: String
) {

    //Shuffles answer options
    fun getShuffledOptions(): List<String> {
        return choices.map { it.substring(3) }.shuffled()
    }

   // Returns correct answer from the Questions.json
    fun getCorrectAnswerText(): String {
        val index = answer[0] - 'A'
        return choices[index].substring(3)
    }

    // Returns Difficulty Weight to be used in categorization
    fun getScoreWeight(): Int {
        return when (difficulty.lowercase()) {
            "easy" -> 1
            "medium" -> 2
            "hard" -> 3
            else -> 1
        }
    }
}