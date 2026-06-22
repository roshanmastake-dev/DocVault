package com.docvault.app.ui

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.docvault.app.data.Document

data class PendingFile(val uri: Uri, val fileName: String, val fileType: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: DocumentViewModel, incomingUris: List<Uri> = emptyList()) {
    val context = LocalContext.current
    val documents by viewModel.documents.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var pendingFiles by remember { mutableStateOf<List<PendingFile>>(emptyList()) }
    var currentPendingIndex by remember { mutableStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var documentToEdit by remember { mutableStateOf<Document?>(null) }

    // Gallery se share karke aaye files handle karo
    LaunchedEffect(incomingUris) {
        if (incomingUris.isNotEmpty()) {
            val files = incomingUris.mapNotNull { uri ->
                val mimeType = context.contentResolver.getType(uri) ?: ""
                val fileType = if (mimeType == "application/pdf") "pdf" else "image"
                val fileName = queryFileName(context, uri) ?: "document"
                PendingFile(uri, fileName, fileType)
            }
            if (files.isNotEmpty()) {
                pendingFiles = files
                currentPendingIndex = 0
                showAddDialog = true
            }
        }
    }

    // Gallery + files dono dikhte hain, multiple select bhi hoga
    val multipleFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val files = uris.mapNotNull { uri ->
                val mimeType = context.contentResolver.getType(uri) ?: ""
                val fileType = if (mimeType == "application/pdf") "pdf" else "image"
                val fileName = queryFileName(context, uri) ?: "document"
                PendingFile(uri, fileName, fileType)
            }
            if (files.isNotEmpty()) {
                pendingFiles = files
                currentPendingIndex = 0
                showAddDialog = true
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { multipleFilePicker.launch("*/*") },
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Upload document")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("My Documents", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text("Search by name or tag") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(28.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (documents.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No documents yet.\nTap + to add your first file.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    items(documents, key = { it.id }) { doc ->
                        DocumentCard(
                            document = doc,
                            onEdit = { documentToEdit = doc },
                            onDelete = { viewModel.deleteDocument(doc) }
                        )
                    }
                }
            }
        }
    }

    // Ek ek karke dialog dikhao jab multiple files hain
    if (showAddDialog && currentPendingIndex < pendingFiles.size) {
        val current = pendingFiles[currentPendingIndex]
        val total = pendingFiles.size
        val dialogTitle = if (total > 1) "${currentPendingIndex + 1} of $total" else ""

        AddDocumentDialog(
            suggestedName = current.fileName,
            dialogSubtitle = dialogTitle,
            onConfirm = { name, tag ->
                viewModel.addDocument(
                    uri = current.uri,
                    originalFileName = current.fileName,
                    customName = name,
                    tag = tag,
                    fileType = current.fileType
                )
                if (currentPendingIndex + 1 < pendingFiles.size) {
                    currentPendingIndex++
                } else {
                    showAddDialog = false
                    pendingFiles = emptyList()
                }
            },
            onDismiss = {
                if (currentPendingIndex + 1 < pendingFiles.size) {
                    currentPendingIndex++
                } else {
                    showAddDialog = false
                    pendingFiles = emptyList()
                }
            }
        )
    }

    documentToEdit?.let { doc ->
        AddDocumentDialog(
            suggestedName = doc.customName,
            suggestedTag = doc.tag,
            isEditing = true,
            onConfirm = { name, tag ->
                viewModel.renameDocument(doc, name, tag)
                documentToEdit = null
            },
            onDismiss = { documentToEdit = null }
        )
    }
}

private fun queryFileName(context: android.content.Context, uri: Uri): String? {
    var name: String? = null
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (it.moveToFirst() && nameIndex >= 0) {
            name = it.getString(nameIndex)
        }
    }
    return name
}
