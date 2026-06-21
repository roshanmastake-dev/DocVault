package com.docvault.app

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
        setContent {
            DocVaultTheme {
                HomeScreen(viewModel = viewModel)
            }
        }
    }
}
