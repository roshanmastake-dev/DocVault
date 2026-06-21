package com.docvault.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class Document(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customName: String,
    val originalFileName: String,
    val filePath: String,
    val fileType: String, // "pdf" or "image"
    val tag: String,
    val dateAdded: Long
)
