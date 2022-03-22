package com.example.pdoh

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView

class OutputType (_button: Button, _textView: TextView) {

    private val button = _button
    private val textView = _textView

    fun disable () {
        button.isClickable = true
        button.setBackgroundColor(Color.BLUE)
        textView.visibility = View.INVISIBLE
    }

    fun enable () {
        button.isClickable = false
        button.setBackgroundColor(Color.GRAY)
        textView.visibility = View.VISIBLE
    }

    fun write(newOutputLine: String) {
        val newOutput = "${textView.text}\n$newOutputLine"
        textView.text = newOutput
    }
}

class Output (errorButton: Button,
              debugButton: Button,
              debugTextView: TextView,
              errorTextView: TextView) {
    private val errorOutput = OutputType(errorButton, errorTextView)
    private val debugOutput = OutputType(debugButton, debugTextView)

    init {
        errorOutput.enable()
        debugOutput.disable()

        errorButton.setOnClickListener {
            errorOutput.enable()
            debugOutput.disable()

        }
        debugButton.setOnClickListener {
            debugOutput.enable()
            errorOutput.disable()
        }
    }

    fun writeError (newOutputLine: String) {
        errorOutput.write(newOutputLine)
    }
    fun writeDebug (newOutputLine: String) {
        debugOutput.write(newOutputLine)
    }
}