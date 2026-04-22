package com.example.zquiz.data.repository

import android.content.Context
import com.example.zquiz.data.model.Question
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class QuizRepository {

    fun loadQuestions(context: Context): List<Question> {

        val inputStream = context.assets.open("questions.json")
        val json = inputStream.bufferedReader().use { it.readText() }

        val type = object : TypeToken<List<Question>>() {}.type

        return Gson().fromJson(json, type)
    }
}