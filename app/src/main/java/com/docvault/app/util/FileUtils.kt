package com.docvault.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.docvault.app.data.Document
import java.io.File

object FileUtils {

    fun getUriForDocument(context: Context, document: Document): Uri {
        val file = File(document.filePath)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun getMimeType(fileType: String): String =
        if (fileType == "pdf") "application/pdf" else "image/*"

    fun openDocument(context: Context, document: Document) {
        val uri = getUriForDocument(context, document)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, getMimeType(document.fileType))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun shareDocument(context: Context, document: Document) {
        val uri = getUriForDocument(context, document)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = getMimeType(document.fileType)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share via"))
    }
}
