package com.example.pdoh

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView

class OutputText (textView: TextView) {

    private val view = textView

    fun write(newOutputLine: String) {
        val newOutput = "${view.text}\n$newOutputLine"
        view.text = newOutput
    }

    fun show() {
        view.visibility = View.VISIBLE
    }

    fun hide() {
        view.visibility = View.INVISIBLE
    }
}

class Output (_errorButton: Button,
              _debugButton: Button,
              debugTextView: TextView,
              errorTextView: TextView) {
    private val errorButton = _errorButton
    private val debugButton = _debugButton
    private val debugOutputText = OutputText(debugTextView)
    private val errorOutputText = OutputText(errorTextView)

    init {
        errorButton.setOnClickListener {
            debugOutputText.hide()
            errorOutputText.show()
        }
        debugButton.setOnClickListener {
            errorOutputText.hide()
            debugOutputText.show()
        }
    }

    fun writeError (newOutputLine: String) {
        errorOutputText.write(newOutputLine)
    }
    fun writeDebug (newOutputLine: String) {
        debugOutputText.write(newOutputLine)
    }
}