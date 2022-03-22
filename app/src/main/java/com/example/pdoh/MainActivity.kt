package com.example.pdoh

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private lateinit var debugOutput: Output
    init {
        println("[MainActivity.init 1]")

        val textView = findViewById<TextView>(R.id.debug_output)
        debugOutput = Output()
        println("[MainActivity.init 2] ${textView.text}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        debugOutput.write("[MainActivity.onCreate] app created")
        debugOutput.write("[MainActivity.onCreate] other log")
    }

}