package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PdfDao {
    @Query("SELECT * FROM pdf_documents ORDER BY uploadDate DESC")
    fun getAllPdfs(): Flow<List<PdfDocument>>

    @Query("SELECT * FROM pdf_documents WHERE department = :dept AND semester = :sem ORDER BY title ASC")
    fun getPdfsByFilter(dept: String, sem: Int): Flow<List<PdfDocument>>

    @Query("SELECT * FROM pdf_documents WHERE isDownloaded = 1 ORDER BY title ASC")
    fun getDownloadedPdfs(): Flow<List<PdfDocument>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPdf(pdf: PdfDocument)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pdfs: List<PdfDocument>)

    @Update
    suspend fun updatePdf(pdf: PdfDocument)

    @Delete
    suspend fun deletePdf(pdf: PdfDocument)

    @Query("DELETE FROM pdf_documents WHERE id = :id")
    suspend fun deletePdfById(id: Int)

    @Query("SELECT * FROM class_representatives ORDER BY name ASC")
    fun getAllCRs(): Flow<List<ClassRepresentative>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCR(cr: ClassRepresentative)

    @Delete
    suspend fun deleteCR(cr: ClassRepresentative)

    @Query("SELECT * FROM class_representatives WHERE passcode = :passcode LIMIT 1")
    suspend fun getCRByPasscode(passcode: String): ClassRepresentative?

    // User Authentication Methods
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE idNo = :idNo LIMIT 1")
    suspend fun getUserByIdNo(idNo: String): User?
}
