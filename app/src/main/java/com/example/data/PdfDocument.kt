package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pdf_documents")
data class PdfDocument(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val department: String, // e.g. "CSE", "EEE", "Pharmacy"
    val semester: Int,      // e.g. 1 to 8
    val subjectCode: String, // e.g. "CSE-1101"
    val subjectName: String, // e.g. "Computer Programming I"
    val fileType: String,    // "Slide", "Book", "Note", "Question"
    val fileSize: String,    // e.g. "2.4 MB"
    val downloadCount: Int = 0,
    val isDownloaded: Boolean = false,
    val isUserUploaded: Boolean = false,
    val uploadDate: Long = System.currentTimeMillis()
)
