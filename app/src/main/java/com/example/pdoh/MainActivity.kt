package com.example.pdoh

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private lateinit var debugOutput: Output
    private lateinit var errorOutput: Output

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val debugView = findViewById<TextView>(R.id.debug_output)
        debugOutput = Output(debugView)
        val errorView = findViewById<TextView>(R.id.error_output)
        errorOutput = Output(errorView)
        errorOutput.hide()

        val buttonError = findViewById<TextView>(R.id.button_error)

        buttonError.setOnClickListener {
            debugOutput.hide()
            errorOutput.show()
        }

        val buttonDebug = findViewById<TextView>(R.id.button_debug)

        buttonDebug.setOnClickListener {
            errorOutput.hide()
            debugOutput.show()
        }
        debugOutput.write("[MainActivity.onCreate] app created")
        debugOutput.write("[MainActivity.onCreate] other log")
    }

}