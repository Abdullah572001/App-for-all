package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PdfRepository(private val pdfDao: PdfDao) {
    val allPdfs: Flow<List<PdfDocument>> = pdfDao.getAllPdfs()
    val downloadedPdfs: Flow<List<PdfDocument>> = pdfDao.getDownloadedPdfs()

    fun getPdfsForFilter(dept: String, sem: Int): Flow<List<PdfDocument>> {
        return pdfDao.getPdfsByFilter(dept, sem)
    }

    suspend fun insertPdf(pdf: PdfDocument) = withContext(Dispatchers.IO) {
        pdfDao.insertPdf(pdf)
    }

    suspend fun updatePdf(pdf: PdfDocument) = withContext(Dispatchers.IO) {
        pdfDao.updatePdf(pdf)
    }

    suspend fun deletePdf(pdf: PdfDocument) = withContext(Dispatchers.IO) {
        pdfDao.deletePdf(pdf)
    }

    suspend fun deletePdfById(id: Int) = withContext(Dispatchers.IO) {
        pdfDao.deletePdfById(id)
    }

    val allCRs: Flow<List<ClassRepresentative>> = pdfDao.getAllCRs()

    suspend fun insertCR(cr: ClassRepresentative) = withContext(Dispatchers.IO) {
        pdfDao.insertCR(cr)
    }

    suspend fun deleteCR(cr: ClassRepresentative) = withContext(Dispatchers.IO) {
        pdfDao.deleteCR(cr)
    }

    suspend fun getCRByPasscode(passcode: String): ClassRepresentative? = withContext(Dispatchers.IO) {
        pdfDao.getCRByPasscode(passcode)
    }

    suspend fun insertUser(user: User) = withContext(Dispatchers.IO) {
        pdfDao.insertUser(user)
    }

    suspend fun getUserByIdNo(idNo: String): User? = withContext(Dispatchers.IO) {
        pdfDao.getUserByIdNo(idNo)
    }

    suspend fun seedDatabaseIfEmpty() = withContext(Dispatchers.IO) {
        val currentList = pdfDao.getAllPdfs().first()
        if (currentList.isEmpty()) {
            val defaultPdfs = listOf(
                // CSE - Semester 1
                PdfDocument(
                    title = "Structured Programming Intro Slides",
                    department = "CSE",
                    semester = 1,
                    subjectCode = "CSE-1101",
                    subjectName = "Structured Programming",
                    fileType = "Slide",
                    fileSize = "3.2 MB"
                ),
                PdfDocument(
                    title = "Ansi C programming - Ritchie & Kernighan",
                    department = "CSE",
                    semester = 1,
                    subjectCode = "CSE-1101",
                    subjectName = "Structured Programming",
                    fileType = "Book",
                    fileSize = "12.4 MB"
                ),
                PdfDocument(
                    title = "Structured Programming Mid Exam 2023",
                    department = "CSE",
                    semester = 1,
                    subjectCode = "CSE-1101",
                    subjectName = "Structured Programming",
                    fileType = "Question",
                    fileSize = "1.1 MB"
                ),
                PdfDocument(
                    title = "Structured Programming Semester Final 2022",
                    department = "CSE",
                    semester = 1,
                    subjectCode = "CSE-1101",
                    subjectName = "Structured Programming",
                    fileType = "Question",
                    fileSize = "1.5 MB"
                ),
                PdfDocument(
                    title = "Single Variable Calculus Notes",
                    department = "CSE",
                    semester = 1,
                    subjectCode = "MATH-1101",
                    subjectName = "Mathematics I",
                    fileType = "Note",
                    fileSize = "2.8 MB"
                ),

                // CSE - Semester 2
                PdfDocument(
                    title = "Data Structures Complete Handout",
                    department = "CSE",
                    semester = 2,
                    subjectCode = "CSE-1201",
                    subjectName = "Data Structures",
                    fileType = "Note",
                    fileSize = "4.5 MB"
                ),
                PdfDocument(
                    title = "Data Structures final Exam Autumn 2023",
                    department = "CSE",
                    semester = 2,
                    subjectCode = "CSE-1201",
                    subjectName = "Data Structures",
                    fileType = "Question",
                    fileSize = "1.2 MB"
                ),
                PdfDocument(
                    title = "Stack and Queue Practice Slides",
                    department = "CSE",
                    semester = 2,
                    subjectCode = "CSE-1201",
                    subjectName = "Data Structures",
                    fileType = "Slide",
                    fileSize = "1.9 MB"
                ),

                // CSE - Semester 3
                PdfDocument(
                    title = "OOP Concepts in Java Lecture Material",
                    department = "CSE",
                    semester = 3,
                    subjectCode = "CSE-2301",
                    subjectName = "Object Oriented Programming",
                    fileType = "Slide",
                    fileSize = "5.1 MB"
                ),
                PdfDocument(
                    title = "OOP Java Mid Term Board Question 2024",
                    department = "CSE",
                    semester = 3,
                    subjectCode = "CSE-2301",
                    subjectName = "Object Oriented Programming",
                    fileType = "Question",
                    fileSize = "1.3 MB"
                ),
                PdfDocument(
                    title = "Digital Logic and Computer Design - Morris Mano",
                    department = "CSE",
                    semester = 3,
                    subjectCode = "CSE-2303",
                    subjectName = "Digital Logic Design",
                    fileType = "Book",
                    fileSize = "18.2 MB"
                ),

                // CSE - Semester 4
                PdfDocument(
                    title = "CLRS - Introduction to Algorithms 3rd Ed",
                    department = "CSE",
                    semester = 4,
                    subjectCode = "CSE-2401",
                    subjectName = "Algorithms",
                    fileType = "Book",
                    fileSize = "22.5 MB"
                ),
                PdfDocument(
                    title = "Dynamic Programming Short Notes",
                    department = "CSE",
                    semester = 4,
                    subjectCode = "CSE-2401",
                    subjectName = "Algorithms",
                    fileType = "Note",
                    fileSize = "3.1 MB"
                ),
                PdfDocument(
                    title = "Algorithms Final Board Paper 2023",
                    department = "CSE",
                    semester = 4,
                    subjectCode = "CSE-2401",
                    subjectName = "Algorithms",
                    fileType = "Question",
                    fileSize = "1.4 MB"
                ),
                PdfDocument(
                    title = "Database Systems Concepts - Silberschatz",
                    department = "CSE",
                    semester = 4,
                    subjectCode = "CSE-2403",
                    subjectName = "Database Management Systems",
                    fileType = "Book",
                    fileSize = "14.8 MB"
                ),

                // CSE - Semester 5
                PdfDocument(
                    title = "Computer Networks slide (Physical & Data link layer)",
                    department = "CSE",
                    semester = 5,
                    subjectCode = "CSE-3505",
                    subjectName = "Computer Networks",
                    fileType = "Slide",
                    fileSize = "4.2 MB"
                ),
                PdfDocument(
                    title = "Computer Networks Mid Term Quiz 2024",
                    department = "CSE",
                    semester = 5,
                    subjectCode = "CSE-3505",
                    subjectName = "Computer Networks",
                    fileType = "Question",
                    fileSize = "1.0 MB"
                ),

                // CSE - Semester 6
                PdfDocument(
                    title = "Operating System Concepts - Abraham Silberschatz",
                    department = "CSE",
                    semester = 6,
                    subjectCode = "CSE-3601",
                    subjectName = "Operating Systems",
                    fileType = "Book",
                    fileSize = "15.9 MB"
                ),
                PdfDocument(
                    title = "OS Process Management & Semaphores",
                    department = "CSE",
                    semester = 6,
                    subjectCode = "CSE-3601",
                    subjectName = "Operating Systems",
                    fileType = "Note",
                    fileSize = "2.9 MB"
                ),

                // CSE - Semester 7
                PdfDocument(
                    title = "Artificial Intelligence - Modern Approach (Russell & Norvig)",
                    department = "CSE",
                    semester = 7,
                    subjectCode = "CSE-4701",
                    subjectName = "Artificial Intelligence",
                    fileType = "Book",
                    fileSize = "25.2 MB"
                ),
                PdfDocument(
                    title = "AI BFS DFS A* Search Algorithms Slides",
                    department = "CSE",
                    semester = 7,
                    subjectCode = "CSE-4701",
                    subjectName = "Artificial Intelligence",
                    fileType = "Slide",
                    fileSize = "3.8 MB"
                ),

                // CSE - Semester 8
                PdfDocument(
                    title = "Information Security principles and cryptography key slides",
                    department = "CSE",
                    semester = 8,
                    subjectCode = "CSE-4805",
                    subjectName = "Cyber Security",
                    fileType = "Slide",
                    fileSize = "5.5 MB"
                ),
                PdfDocument(
                    title = "Cyber Security Board Final Paper 2024",
                    department = "CSE",
                    semester = 8,
                    subjectCode = "CSE-4805",
                    subjectName = "Cyber Security",
                    fileType = "Question",
                    fileSize = "1.3 MB"
                ),

                // EEE - Semester 1
                PdfDocument(
                    title = "Basic Electric Circuits - Boylestad",
                    department = "EEE",
                    semester = 1,
                    subjectCode = "EEE-1101",
                    subjectName = "Electrical Circuits I",
                    fileType = "Book",
                    fileSize = "11.2 MB"
                ),
                PdfDocument(
                    title = "EEE-1101 Circuit Analysis Autumn 2023 Board Paper",
                    department = "EEE",
                    semester = 1,
                    subjectCode = "EEE-1101",
                    subjectName = "Electrical Circuits I",
                    fileType = "Question",
                    fileSize = "1.5 MB"
                ),

                // EEE - Semester 2
                PdfDocument(
                    title = "Electronic Devices and Circuit Theory - Floyd",
                    department = "EEE",
                    semester = 2,
                    subjectCode = "EEE-1201",
                    subjectName = "Electronics I",
                    fileType = "Book",
                    fileSize = "13.6 MB"
                ),
                PdfDocument(
                    title = "Diode Applications Slide Set",
                    department = "EEE",
                    semester = 2,
                    subjectCode = "EEE-1201",
                    subjectName = "Electronics I",
                    fileType = "Slide",
                    fileSize = "3.4 MB"
                ),

                // Pharmacy - Semester 1
                PdfDocument(
                    title = "Anatomy and Physiology Guide Notes",
                    department = "PHARM",
                    semester = 1,
                    subjectCode = "PHR-1101",
                    subjectName = "Human Anatomy & Physiology I",
                    fileType = "Note",
                    fileSize = "6.1 MB"
                ),
                PdfDocument(
                    title = "PHR-1101 Semester Final board exam 2023",
                    department = "PHARM",
                    semester = 1,
                    subjectCode = "PHR-1101",
                    subjectName = "Human Anatomy & Physiology I",
                    fileType = "Question",
                    fileSize = "1.2 MB"
                )
            )
            pdfDao.insertAll(defaultPdfs)
        }
    }
}
