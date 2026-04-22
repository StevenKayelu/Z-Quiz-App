package com.example.zquiz.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.zquiz.R
import com.example.zquiz.viewmodel.QuizViewModel
import com.google.android.material.button.MaterialButton

/**
 * QuizActivity handles the main quiz gameplay screen.
 * It displays one question at a time with multiple choice answer buttons,
 * a countdown timer, a score tracker, and a progress bar.
 * After each answer, an explanation dialog is shown before moving to the next question.
 */
class QuizActivity : AppCompatActivity() {

    // ViewModel that holds and manages quiz data and state
    private val viewModel: QuizViewModel by viewModels()

    // TextView to display the current quiz question
    private lateinit var txtQuestion: TextView

    // TextView to display the countdown timer for each question
    private lateinit var txtTimer: TextView

    // TextView to display the current score
    private lateinit var txtScore: TextView

    // ProgressBar to visually show how many questions have been answered
    private lateinit var progressBar: ProgressBar

    // List of answer option buttons (A, B, C, D)
    private lateinit var buttons: List<MaterialButton>

    // Countdown timer instance for each question
    private var timer: CountDownTimer? = null

    // Time allowed per question in milliseconds (default: 10 seconds)
    private var timePerQuestion = 10000L

    // Total number of questions in this quiz session
    private var totalQuestions = 0

    /**
     * Called when the activity is first created.
     * Initializes views, observers, click listeners, and loads quiz questions
     * based on settings passed from MainActivity via Intent extras.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        initViews()
        setupObservers()
        setupClickListeners()

        // Retrieve quiz settings passed from MainActivity
        val category = intent.getStringExtra("category") ?: ""
        val difficulty = intent.getStringExtra("difficulty") ?: ""

        totalQuestions = intent.getIntExtra("question_count", 10)
        val timeSeconds = intent.getIntExtra("time_per_question", 10)

        // Convert time from seconds to milliseconds for CountDownTimer
        timePerQuestion = (timeSeconds * 1000).toLong()

        // Set progress bar maximum to the total number of questions
        progressBar.max = totalQuestions

        // Tell the ViewModel to load questions matching the selected settings
        viewModel.loadQuestionsByCategoryAndDifficulty(
            this,
            category,
            difficulty,
            totalQuestions,
            timeSeconds
        )
    }

    /**
     * Binds all UI views from the layout to their corresponding properties.
     */
    private fun initViews() {
        txtQuestion = findViewById(R.id.txtQuestion)
        txtTimer = findViewById(R.id.txtTimer)
        txtScore = findViewById(R.id.txtScore)
        progressBar = findViewById(R.id.progressBar)

        // Collect all four answer buttons into a list for easy iteration
        buttons = listOf(
            findViewById(R.id.btnA),
            findViewById(R.id.btnB),
            findViewById(R.id.btnC),
            findViewById(R.id.btnD)
        )
    }

    /**
     * Sets up LiveData observers to reactively update the UI
     * whenever quiz data changes in the ViewModel.
     */
    private fun setupObservers() {

        // Observe the questions list — render the current question when data is loaded
        viewModel.questions.observe(this) {
            renderQuestion()
        }

        // Observe answer options — update button labels whenever options change
        viewModel.currentOptions.observe(this) { options ->
            buttons.forEachIndexed { i, btn ->
                btn.text = options.getOrNull(i) ?: ""

                // Reset button appearance to default for each new question
                resetButton(btn)
            }
        }

        // Observe score changes — refresh the header UI when score updates
        viewModel.score.observe(this) {
            updateHeaderUI()
        }
    }

    /**
     * Attaches click listeners to each answer button.
     * Each button triggers handleAnswer() when clicked.
     */
    private fun setupClickListeners() {
        buttons.forEach { button ->
            button.setOnClickListener { handleAnswer(it as MaterialButton) }
        }
    }

    /**
     * Renders the current question onto the screen.
     * If there are no more questions, the quiz ends.
     */
    private fun renderQuestion() {
        val question = viewModel.getCurrentQuestion()

        // If no question is returned, the quiz is complete
        if (question == null) {
            finishQuiz()
            return
        }

        txtQuestion.text = question.question
        updateHeaderUI()
        startTimer() // Start the countdown for this question
    }

    /**
     * Updates the progress bar and score display.
     * Acts as the single source of truth for the header UI state.
     */
    private fun updateHeaderUI() {
        val index = viewModel.getCurrentIndex() + 1
        val score = viewModel.score.value ?: 0

        // Ensure progress does not exceed the total number of questions
        progressBar.progress = index.coerceAtMost(totalQuestions)
        txtScore.text = "Score: $score / $totalQuestions"
    }

    /**
     * Starts a countdown timer for the current question.
     * Cancels any previously running timer before starting a new one.
     * Automatically moves to the next question when time runs out.
     */
    private fun startTimer() {
        timer?.cancel() // Cancel any existing timer before starting fresh

        timer = object : CountDownTimer(timePerQuestion, 1000) {

            // Update the timer display every second
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                txtTimer.text = "Time: ${millisUntilFinished / 1000}s"
            }

            // Time is up — move to the next question automatically
            override fun onFinish() {
                goNext()
            }
        }.start()
    }

    /**
     * Handles the user's answer selection when an answer button is tapped.
     * Cancels the timer, evaluates correctness, updates button colors,
     * and shows the explanation dialog.
     *
     * @param button The answer button that was clicked.
     */
    private fun handleAnswer(button: MaterialButton) {
        timer?.cancel() // Stop the timer as soon as an answer is selected

        val selected = button.text.toString()
        viewModel.answerQuestion(selected) // Notify ViewModel of the selected answer

        val correct = viewModel.lastCorrectAnswer.value ?: ""

        // Disable all buttons to prevent multiple answers
        disableButtons()

        // Highlight the selected button as correct or wrong
        if (selected == correct) {
            setCorrect(button)
        } else {
            setWrong(button)
            highlightCorrect(correct) // Also highlight the correct answer in green
        }

        updateHeaderUI()
        showExplanationDialog() // Show explanation before proceeding
    }

    /**
     * Displays an AlertDialog with an explanation for the current question's answer.
     * The user must tap "Next" to proceed to the next question.
     */
    private fun showExplanationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Explanation")
            .setMessage(viewModel.lastExplanation.value ?: "")
            .setCancelable(false) // Prevent dismissing without tapping Next
            .setPositiveButton("Next") { _, _ -> goNext() }
            .show()
    }

    /**
     * Advances to the next question or ends the quiz if all questions are done.
     */
    private fun goNext() {
        if (viewModel.isFinished()) {
            finishQuiz()
        } else {
            viewModel.moveToNext()
            renderQuestion()
        }
    }

    /**
     * Ends the quiz and navigates to ResultActivity,
     * passing the final score and total question count.
     */
    private fun finishQuiz() {
        startActivity(
            Intent(this, ResultActivity::class.java)
                .putExtra("score", viewModel.score.value ?: 0)
                .putExtra("total", totalQuestions)
        )
        finish() // Close QuizActivity so the user can't navigate back to it
    }

    // ================= UI HELPERS =================

    /**
     * Resets a button to its default appearance (enabled, gray background, black text).
     * Called when setting up buttons for a new question.
     *
     * @param btn The button to reset.
     */
    private fun resetButton(btn: MaterialButton) {
        btn.isEnabled = true
        btn.setBackgroundColor(getColor(android.R.color.darker_gray))
        btn.setTextColor(getColor(android.R.color.black))
    }

    /**
     * Disables all answer buttons to prevent the user from selecting
     * multiple answers for the same question.
     */
    private fun disableButtons() {
        buttons.forEach { it.isEnabled = false }
    }

    /**
     * Highlights a button green to indicate a correct answer.
     *
     * @param btn The button to mark as correct.
     */
    private fun setCorrect(btn: MaterialButton) {
        btn.setBackgroundColor(getColor(android.R.color.holo_green_dark))
        btn.setTextColor(getColor(android.R.color.white))
    }

    /**
     * Highlights a button red to indicate a wrong answer.
     *
     * @param btn The button to mark as wrong.
     */
    private fun setWrong(btn: MaterialButton) {
        btn.setBackgroundColor(getColor(android.R.color.holo_red_dark))
        btn.setTextColor(getColor(android.R.color.white))
    }

    /**
     * Finds the button displaying the correct answer text and highlights it green.
     * Used when the user selects a wrong answer, to show the correct one.
     *
     * @param answer The correct answer text to find and highlight.
     */
    private fun highlightCorrect(answer: String) {
        buttons.find { it.text == answer }?.let {
            setCorrect(it)
        }
    }

    /**
     * Called when the activity is destroyed.
     * Cancels the timer to prevent memory leaks or callbacks on a dead activity.
     */
    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }
}