package com.example.zquiz.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.zquiz.data.model.Question
import com.example.zquiz.data.repository.QuizRepository

class QuizViewModel : ViewModel() {

    private val repository = QuizRepository()

    private val _questions = MutableLiveData<List<Question>>()
    val questions: LiveData<List<Question>> = _questions

    private val _currentIndex = MutableLiveData(0)

    private val _score = MutableLiveData(0)
    val score: LiveData<Int> = _score

    private val _currentOptions = MutableLiveData<List<String>>()
    val currentOptions: LiveData<List<String>> = _currentOptions

    private val _lastCorrectAnswer = MutableLiveData<String>()
    val lastCorrectAnswer: LiveData<String> = _lastCorrectAnswer

    private val _lastExplanation = MutableLiveData<String>()
    val lastExplanation: LiveData<String> = _lastExplanation

    // NEW: store config
    private var maxQuestions: Int = 10
    private var timePerQuestion: Int = 15

    fun loadQuestionsByCategoryAndDifficulty(
        context: Context,
        category: String,
        difficulty: String,
        limit: Int,
        timePerQuestion: Int
    ) {
        val all = repository.loadQuestions(context)

        val filtered = all.filter {
            it.category.equals(category, true) &&
                    it.difficulty.equals(difficulty, true)
        }
            .shuffled()
            .take(limit)   // T0 ENFORCES QUESTION COUNT

        _questions.value = filtered
        _currentIndex.value = 0

        updateUI()
    }

    private fun updateUI() {
        val question = getCurrentQuestion()
        _currentOptions.value = question?.getShuffledOptions() ?: emptyList()
    }

    fun getCurrentQuestion(): Question? {
        val index = _currentIndex.value ?: 0
        return _questions.value?.getOrNull(index)
    }

    fun answerQuestion(selected: String) {
        val question = getCurrentQuestion() ?: return

        val correct = question.getCorrectAnswerText()

        _lastCorrectAnswer.value = correct
        _lastExplanation.value = question.explanation

        if (selected == correct) {
            _score.value = (_score.value ?: 0) + question.getScoreWeight()
        }
    }

    fun moveToNext() {
        _currentIndex.value = (_currentIndex.value ?: 0) + 1
        updateUI()

    }
    fun getCurrentIndex(): Int {
        return _currentIndex.value ?: 0
    }

    fun getTotalQuestions(): Int {
        return _questions.value?.size ?: 0
    }

    fun isFinished(): Boolean {
        val index = _currentIndex.value ?: 0
        val size = _questions.value?.size ?: 0
        return index >= size
    }

    fun getTimePerQuestion(): Int {
        return timePerQuestion
    }
}