package com.testernest.sample

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.testernest.android.Testernest

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val inputPublicKey = findViewById<EditText>(R.id.inputPublicKey)
        val inputBaseUrl = findViewById<EditText>(R.id.inputBaseUrl)
        val inputEventName = findViewById<EditText>(R.id.inputEventName)
        val inputConnectCode = findViewById<EditText>(R.id.inputConnectCode)
        val output = findViewById<TextView>(R.id.textOutput)

        findViewById<Button>(R.id.buttonInit).setOnClickListener {
            val publicKey = inputPublicKey.text.toString().trim()
            val baseUrl = inputBaseUrl.text.toString().trim()
            Testernest.init(this, publicKey, baseUrl = baseUrl, enableLogs = true)
            output.text = "Initialized"
            Log.i("TesternestSample", "Init tapped")
        }

        findViewById<Button>(R.id.buttonLogEvent).setOnClickListener {
            val eventName = inputEventName.text.toString().trim().ifBlank { "sample_event" }
            Testernest.logEvent(eventName, mapOf("sample" to true))
            output.text = "Logged event: $eventName"
            Log.i("TesternestSample", "Log Event tapped")
        }

        findViewById<Button>(R.id.buttonConnectCode).setOnClickListener {
            val code = inputConnectCode.text.toString().trim()
            Testernest.connectTester(code)
            output.text = "Connect code requested"
            Log.i("TesternestSample", "Connect tapped")
        }

        findViewById<Button>(R.id.buttonFlush).setOnClickListener {
            Testernest.flushNow()
            output.text = "Flush requested"
            Log.i("TesternestSample", "Flush tapped")
        }

        findViewById<Button>(R.id.buttonDebug).setOnClickListener {
            val snapshot = Testernest.getDebugSnapshot()
            output.text = snapshot.entries.joinToString("\n") { "${it.key}: ${it.value}" }
        }
    }

    override fun onResume() {
        super.onResume()
        Testernest.attachAutoConnectPrompt(this)
    }
}
