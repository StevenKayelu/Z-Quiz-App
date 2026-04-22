package com.example.zquiz.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import com.example.zquiz.R
import com.google.android.material.button.MaterialButton

/**
 * MainActivity serves as the quiz setup screen.
 * It allows the user to select quiz preferences such as category,
 * difficulty, number of questions, and time per question before starting the quiz.
 */
class MainActivity : AppCompatActivity() {

    // Dropdown (spinner) for selecting quiz category
    private lateinit var spinnerCategory: AutoCompleteTextView

    // Dropdown for selecting quiz difficulty level
    private lateinit var spinnerDifficulty: AutoCompleteTextView

    // Dropdown for selecting the number of questions
    private lateinit var spinnerQuestions: AutoCompleteTextView

    // Dropdown for selecting time allowed per question (in seconds)
    private lateinit var spinnerTime: AutoCompleteTextView

    // Button to start the quiz with the selected settings
    private lateinit var btnStart: MaterialButton

    /**
     * Called when the activity is first created.
     * Initializes the UI components, populates dropdowns with data,
     * sets default selections, and handles the Start button click.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bind UI views from the layout using their resource IDs
        spinnerCategory = findViewById(R.id.spinnerCategory)
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty)
        spinnerQuestions = findViewById(R.id.spinnerQuestions)
        spinnerTime = findViewById(R.id.spinnerTime)
        btnStart = findViewById(R.id.btnStart)

        // Define the available options for each quiz setting
        val categories = listOf("Spiritual", "Sports", "Music & Entertainment", "Finance & Money Skills", "Business/Entrepreneurship", "IQ & Brain Teasers")
        val difficulties = listOf("easy", "medium", "hard")
        val questionCounts = listOf(5, 10, 15)
        val timePerQuestion = listOf(10, 15, 20, 30) // values in seconds

        // Set up and attach an ArrayAdapter to each dropdown spinner
        spinnerCategory.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, categories)
        )

        spinnerDifficulty.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, difficulties)
        )

        spinnerQuestions.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, questionCounts)
        )

        spinnerTime.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, timePerQuestion)
        )

        // Set default values for each dropdown (first item in each list)
        // 'false' prevents the dropdown from filtering based on the set text
        spinnerCategory.setText(categories[0], false)
        spinnerDifficulty.setText(difficulties[0], false)
        spinnerQuestions.setText(questionCounts[0].toString(), false)
        spinnerTime.setText(timePerQuestion[0].toString(), false)

        // Handle Start button click to launch the QuizActivity
        btnStart.setOnClickListener {

            // Animate the button with a brief scale-down effect for visual feedback
            it.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100)
                .withEndAction {

                    // Reset button scale back to original after animation completes
                    it.scaleX = 1f
                    it.scaleY = 1f

                    // Read selected values from each dropdown, falling back to defaults if empty
                    val selectedCategory = spinnerCategory.text.toString().ifEmpty { categories[0] }
                    val selectedDifficulty = spinnerDifficulty.text.toString().ifEmpty { difficulties[0] }

                    // Parse number of questions as Int, defaulting to first option if invalid
                    val selectedQuestionCount = spinnerQuestions.text.toString()
                        .toIntOrNull() ?: questionCounts[0]

                    // Parse time per question as Int, defaulting to first option if invalid
                    val selectedTime = spinnerTime.text.toString()
                        .toIntOrNull() ?: timePerQuestion[0]

                    // Create an Intent to start QuizActivity and pass selected settings as extras
                    val intent = Intent(this, QuizActivity::class.java).apply {
                        putExtra("category", selectedCategory)
                        putExtra("difficulty", selectedDifficulty)
                        putExtra("question_count", selectedQuestionCount)
                        putExtra("time_per_question", selectedTime)
                    }

                    // Launch the QuizActivity
                    startActivity(intent)
                }
        }
    }
}