package com.docvault.app.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class DocumentRepository(private val context: Context) {

    private val dao = AppDatabase.getInstance(context).documentDao()

    private val storageDir: File by lazy {
        File(context.filesDir, "docvault_files").apply { if (!exists()) mkdirs() }
    }

    fun getAll(): Flow<List<Document>> = dao.getAll()

    fun search(query: String): Flow<List<Document>> =
        if (query.isBlank()) dao.getAll() else dao.search(query)

    suspend fun addDocument(
        uri: Uri,
        originalFileName: String,
        customName: String,
        tag: String,
        fileType: String
    ): Long {
        val extension = if (fileType == "pdf") "pdf" else "jpg"
        val savedFile = File(storageDir, "${UUID.randomUUID()}.$extension")

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(savedFile).use { output ->
                input.copyTo(output)
            }
        }

        val document = Document(
            customName = customName,
            originalFileName = originalFileName,
            filePath = savedFile.absolutePath,
            fileType = fileType,
            tag = tag,
            dateAdded = System.currentTimeMillis()
        )
        return dao.insert(document)
    }

    suspend fun renameDocument(document: Document, newName: String, newTag: String) {
        dao.update(document.copy(customName = newName, tag = newTag))
    }

    suspend fun deleteDocument(document: Document) {
        File(document.filePath).delete()
        dao.delete(document)
    }
}
