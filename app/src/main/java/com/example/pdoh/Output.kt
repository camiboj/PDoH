package com.example.pdoh

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView

class Output (textView: TextView) {

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