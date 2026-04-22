package com.example.zquiz.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.zquiz.R
import com.google.android.material.button.MaterialButton
import kotlin.math.roundToInt

/**
 * ResultActivity displays the final quiz results to the user.
 * It shows the score, percentage, a grade title, and personalized feedback
 * based on how well the user performed. It also provides options to
 * restart the quiz or return to the home screen.
 */
class ResultActivity : AppCompatActivity() {

    // TextView to display the raw score (e.g. "Score: 7 / 10")
    private lateinit var txtScore: TextView

    // TextView to display personalized feedback based on performance
    private lateinit var txtFeedback: TextView

    // TextView to display the grade title (e.g. "Master Level 🏆")
    private lateinit var txtTitle: TextView

    // TextView to display the percentage score (e.g. "Percentage: 70%")
    private lateinit var txtPercentage: TextView

    // Button to restart the quiz and go back to MainActivity
    private lateinit var btnRestart: MaterialButton

    // Button to navigate back to the home screen (MainActivity)
    private lateinit var btnHome: MaterialButton

    /**
     * Called when the activity is first created.
     * Retrieves the score and total from the Intent, calculates the percentage,
     * determines the grade and feedback, and sets up button click listeners.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        // Bind UI views from the layout
        txtScore = findViewById(R.id.txtScore)
        txtFeedback = findViewById(R.id.txtFeedback)
        txtTitle = findViewById(R.id.txtTitle)
        txtPercentage = findViewById(R.id.txtPercentage)

        btnRestart = findViewById(R.id.btnRestart)
        btnHome = findViewById(R.id.btnHome)

        // Retrieve the score and total questions passed from QuizActivity
        val score = intent.getIntExtra("score", 0)
        val total = intent.getIntExtra("total", 10)

        // Calculate percentage, guarding against division by zero
        val percentage = if (total > 0) {
            (score.toDouble() / total.toDouble()) * 100
        } else 0.0

        // Round the percentage to the nearest whole number for display
        val percentRounded = percentage.roundToInt()

        // Display the raw score and rounded percentage on screen
        txtScore.text = "Score: $score / $total"
        txtPercentage.text = "Percentage: $percentRounded%"

        // Determine the grade title and feedback message using a smart grading system.
        // Each range maps to a descriptive title and an encouraging feedback message.
        val (title, feedback) = when {
            percentage >= 85 -> "Master Level 🏆" to "Outstanding performance! You have excellent mastery."
            percentage >= 70 -> "Excellent 👏" to "Great job! Strong understanding of the topic."
            percentage >= 50 -> "Good 👍" to "Fair performance. Keep practicing to improve."
            percentage >= 30 -> "Needs Improvement ⚠️" to "You're getting there. Focus on weak areas."
            else            -> "Try Again 💪" to "Don't give up. Practice will improve your score."
        }

        // Display the grade title and feedback on screen
        txtTitle.text = title
        txtFeedback.text = feedback

        // Restart button: navigates back to MainActivity and clears the activity stack
        // FLAG_ACTIVITY_CLEAR_TOP ensures no duplicate instances of MainActivity exist
        btnRestart.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish() // Close ResultActivity
        }

        // Home button: navigates back to MainActivity without clearing the stack
        btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Close ResultActivity
        }
    }
}