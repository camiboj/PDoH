package com.example.pdoh

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private lateinit var debugOutput: Output
    private lateinit var errorOutput: Output

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val errorButton = findViewById<Button>(R.id.error_button)
        val debugButton = findViewById<Button>(R.id.debug_button)
        val debugTextView = findViewById<TextView>(R.id.debug_output)
        val errorTextView = findViewById<TextView>(R.id.error_output)

        debugOutput = Output (errorButton, debugButton, debugTextView, errorTextView)

        debugOutput.writeDebug("[MainActivity.onCreate] app created")
        debugOutput.writeDebug("[MainActivity.onCreate] other log")
    }

}