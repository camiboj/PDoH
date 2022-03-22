package com.example.pdoh

import android.widget.TextView

class Output : TextView.Ada {

    init {
        println("[Output.init 1] ")
    }

    private var view: TextView = findViewById<TextView>(R.id.debug_output)

    init {
        println("[Output.init 2]")
    }
    fun write(newOutputLine: String) {
        val newOutput = "${view.text}\n$newOutputLine"
        view.text = newOutput
    }
}