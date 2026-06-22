package com.docvault.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.docvault.app.ui.DocumentViewModel
import com.docvault.app.ui.HomeScreen
import com.docvault.app.ui.theme.DocVaultTheme

class MainActivity : ComponentActivity() {

    private val viewModel: DocumentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val incomingUris = getIncomingUris(intent)

        setContent {
            DocVaultTheme {
                HomeScreen(viewModel = viewModel, incomingUris = incomingUris)
            }
        }
    }

    private fun getIncomingUris(intent: Intent?): List<Uri> {
        if (intent == null) return emptyList()
        return when (intent.action) {
            Intent.ACTION_SEND -> {
                val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                listOfNotNull(uri)
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM) ?: emptyList()
            }
            else -> emptyList()
        }
    }
}
