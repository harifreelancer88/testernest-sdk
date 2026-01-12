package com.testernest.android

import android.app.Dialog
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity

data class TesternestConnectPromptConfig(
    val title: String = "Connect Tester",
    val subtitle: String = "Enter your 6-digit code to connect.",
    val appName: String? = null,
    val showSkipButton: Boolean = true,
    val skipText: String = "Not now",
    val connectText: String = "Connect",
    val primaryColor: Int? = null,
    val backgroundColor: Int? = null,
    val titleColor: Int? = null,
    val subtitleColor: Int? = null,
    val inputTextColor: Int? = null,
    val buttonTextColor: Int? = null,
    val connectTimeoutMs: Long = 8000L
)

class TesternestConnectPrompt(
    private val activity: ComponentActivity,
    private val config: TesternestConnectPromptConfig = TesternestConnectPromptConfig(),
    private val onDismiss: (() -> Unit)? = null,
    private val onConnected: (() -> Unit)? = null
) {
    private val handler = Handler(Looper.getMainLooper())
    private var dialog: Dialog? = null
    private var waitingForConnect = false
    private var pollRunnable: Runnable? = null

    private lateinit var titleView: TextView
    private lateinit var subtitleView: TextView
    private lateinit var appNameView: TextView
    private lateinit var codeInput: EditText
    private lateinit var errorView: TextView
    private lateinit var skipButton: Button
    private lateinit var connectButton: Button

    fun show() {
        if (dialog != null) return
        if (activity.isFinishing || activity.isDestroyed) return
        if (Testernest.isTesterConnected()) return

        val dialog = Dialog(activity)
        dialog.setContentView(R.layout.testernest_connect_prompt)
        dialog.setCancelable(true)
        dialog.setOnDismissListener {
            clearPending()
            onDismiss?.invoke()
        }

        bindViews(dialog)
        applyConfig(dialog)

        dialog.show()
        this.dialog = dialog
    }

    fun dismiss() {
        dialog?.dismiss()
    }

    private fun bindViews(dialog: Dialog) {
        titleView = dialog.findViewById(R.id.testernest_connect_title)
        subtitleView = dialog.findViewById(R.id.testernest_connect_subtitle)
        appNameView = dialog.findViewById(R.id.testernest_connect_app_name)
        codeInput = dialog.findViewById(R.id.testernest_connect_code_input)
        errorView = dialog.findViewById(R.id.testernest_connect_error)
        skipButton = dialog.findViewById(R.id.testernest_connect_skip)
        connectButton = dialog.findViewById(R.id.testernest_connect_submit)

        codeInput.filters = arrayOf(InputFilter.LengthFilter(6))
        codeInput.inputType = InputType.TYPE_CLASS_NUMBER

        skipButton.setOnClickListener { dismiss() }
        connectButton.setOnClickListener { onConnectClicked() }
    }

    private fun applyConfig(dialog: Dialog) {
        titleView.text = config.title
        subtitleView.text = config.subtitle
        connectButton.text = config.connectText
        skipButton.text = config.skipText

        if (config.subtitle.isBlank()) {
            subtitleView.visibility = View.GONE
        }

        if (config.appName.isNullOrBlank()) {
            appNameView.visibility = View.GONE
        } else {
            appNameView.text = config.appName
            appNameView.visibility = View.VISIBLE
        }

        if (!config.showSkipButton) {
            skipButton.visibility = View.GONE
        }

        config.titleColor?.let { titleView.setTextColor(it) }
        config.subtitleColor?.let { subtitleView.setTextColor(it) }
        config.inputTextColor?.let { codeInput.setTextColor(it) }
        config.buttonTextColor?.let { connectButton.setTextColor(it) }

        config.primaryColor?.let { color ->
            connectButton.setBackgroundColor(color)
            if (config.buttonTextColor == null) {
                connectButton.setTextColor(Color.WHITE)
            }
            if (config.showSkipButton && config.buttonTextColor == null) {
                skipButton.setTextColor(color)
            }
        }

        config.backgroundColor?.let { color ->
            dialog.findViewById<View>(R.id.testernest_connect_prompt_root)?.setBackgroundColor(color)
        }
    }

    private fun onConnectClicked() {
        if (waitingForConnect) return
        val code = codeInput.text.toString().trim()
        if (!Regex("^\\d{6}$").matches(code)) {
            showError("Enter a 6-digit code.")
            return
        }
        showError(null)
        setWaiting(true)
        Testernest.connectTester(code)
        startConnectPolling()
    }

    private fun startConnectPolling() {
        val startedAt = System.currentTimeMillis()
        val runnable = object : Runnable {
            override fun run() {
                if (Testernest.isTesterConnected()) {
                    onConnected?.invoke()
                    dismiss()
                    return
                }
                if (System.currentTimeMillis() - startedAt >= config.connectTimeoutMs) {
                    showError("Unable to connect. Check the code and try again.")
                    setWaiting(false)
                    return
                }
                handler.postDelayed(this, 400)
            }
        }
        pollRunnable = runnable
        handler.postDelayed(runnable, 400)
    }

    private fun setWaiting(waiting: Boolean) {
        waitingForConnect = waiting
        connectButton.isEnabled = !waiting
        codeInput.isEnabled = !waiting
        connectButton.text = if (waiting) "Connecting..." else config.connectText
    }

    private fun showError(message: String?) {
        if (message.isNullOrBlank()) {
            errorView.visibility = View.GONE
            return
        }
        errorView.text = message
        errorView.visibility = View.VISIBLE
    }

    private fun clearPending() {
        pollRunnable?.let { handler.removeCallbacks(it) }
        pollRunnable = null
        waitingForConnect = false
    }
}
