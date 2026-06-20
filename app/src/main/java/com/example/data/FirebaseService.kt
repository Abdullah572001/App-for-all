package com.example.data

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.AuthResult
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Custom await extension to safely resolve Task without introducing extra dependencies
suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            continuation.resume(task.result)
        } else {
            continuation.resumeWithException(task.exception ?: Exception("Firebase Task failed"))
        }
    }
}

object FirebaseService {
    private const val TAG = "FirebaseService"
    
    var isFirebaseOnline: Boolean = false
        private set

    private var auth: FirebaseAuth? = null
    private var db: FirebaseFirestore? = null

    fun initialize(context: Context) {
        try {
            val apiKey = BuildConfig.FIREBASE_API_KEY
            val appId = BuildConfig.FIREBASE_APPLICATION_ID
            val projectId = BuildConfig.FIREBASE_PROJECT_ID

            val isPlaceholder = apiKey.contains("FakeKey") || appId.contains("fake_app_id") || projectId.contains("fallback")

            if (isPlaceholder) {
                Log.w(TAG, "Firebase credentials are placeholders. Falling back to local database mode.")
                isFirebaseOnline = false
                return
            }

            val options = FirebaseOptions.Builder()
                .setApiKey(apiKey)
                .setApplicationId(appId)
                .setProjectId(projectId)
                .build()

            val app = if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context, options)
            } else {
                FirebaseApp.getInstance()
            }

            auth = FirebaseAuth.getInstance(app)
            db = FirebaseFirestore.getInstance(app)
            isFirebaseOnline = true
            Log.d(TAG, "Firebase Online Mode successful.")
        } catch (e: Exception) {
            Log.e(TAG, "Firebase initialization failed, running in Offline local database mode: ${e.message}", e)
            isFirebaseOnline = false
        }
    }

    // 1. Firebase Authentication: Sign Up
    suspend fun signUp(email: String, idNo: String, pass: String, role: String, department: String? = null): String? = withContext(Dispatchers.IO) {
        if (!isFirebaseOnline) return@withContext null
        try {
            val mAuth = auth ?: return@withContext null
            val mDb = db ?: return@withContext null

            // Create Firebase Auth User
            val authResult = mAuth.createUserWithEmailAndPassword(email, pass).awaitTask()
            val uid = authResult.user?.uid ?: return@withContext null

            // Create user document in Firestore to persist role based authorization
            val userMap = hashMapOf(
                "uid" to uid,
                "email" to email,
                "idNo" to idNo,
                "role" to role,
                "department" to (department ?: "")
            )
            mDb.collection("users").document(idNo).set(userMap).awaitTask()
            
            return@withContext uid
        } catch (e: Exception) {
            Log.e(TAG, "Firebase SignUp failed: ${e.message}")
            throw e
        }
    }

    // 2. Firebase Authentication: Sign In
    // Returns a UserProfile containing ID, role and department
    suspend fun signIn(email: String, pass: String): Map<String, Any>? = withContext(Dispatchers.IO) {
        if (!isFirebaseOnline) return@withContext null
        try {
            val mAuth = auth ?: return@withContext null
            val mDb = db ?: return@withContext null

            val authResult = mAuth.signInWithEmailAndPassword(email, pass).awaitTask()
            val user = authResult.user ?: return@withContext null
            
            // Search Firestore for the user document containing role details
            // Try matching by email or fetch collection
            val querySnapshot = mDb.collection("users")
                .whereEqualTo("email", email)
                .get()
                .awaitTask()

            if (!querySnapshot.isEmpty) {
                val doc = querySnapshot.documents[0]
                return@withContext doc.data
            }
            
            return@withContext hashMapOf(
                "uid" to user.uid,
                "email" to (user.email ?: ""),
                "idNo" to (user.email?.substringBefore("@") ?: ""),
                "role" to "student",
                "department" to ""
            )
        } catch (e: Exception) {
            Log.e(TAG, "Firebase SignIn failed: ${e.message}")
            throw e
        }
    }

    // 3. Firestore: Sync / Upload PDF Document
    suspend fun uploadPdf(pdf: PdfDocument) = withContext(Dispatchers.IO) {
        if (!isFirebaseOnline) return@withContext
        try {
            val mDb = db ?: return@withContext
            val pdfMap = hashMapOf(
                "id" to pdf.id,
                "title" to pdf.title,
                "department" to pdf.department,
                "semester" to pdf.semester,
                "subjectCode" to pdf.subjectCode,
                "subjectName" to pdf.subjectName,
                "fileType" to pdf.fileType,
                "fileSize" to pdf.fileSize,
                "downloadCount" to pdf.downloadCount,
                "isDownloaded" to pdf.isDownloaded,
                "isUserUploaded" to pdf.isUserUploaded,
                "uploadDate" to pdf.uploadDate
            )
            // Save inside firestore with document ID as pdf title (or safe string)
            val docId = pdf.title.replace("/", "_")
            mDb.collection("pdfs").document(docId).set(pdfMap).awaitTask()
            Log.d(TAG, "PDf uploaded to Firestore successfully: ${pdf.title}")
        } catch (e: Exception) {
            Log.e(TAG, "Firebase pdf upload failed: ${e.message}")
        }
    }

    // 4. Firestore: Delete PDF Document
    suspend fun deletePdf(pdfTitle: String) = withContext(Dispatchers.IO) {
        if (!isFirebaseOnline) return@withContext
        try {
            val mDb = db ?: return@withContext
            val docId = pdfTitle.replace("/", "_")
            mDb.collection("pdfs").document(docId).delete().awaitTask()
            Log.d(TAG, "PDF deleted from Firestore: $pdfTitle")
        } catch (e: Exception) {
            Log.e(TAG, "Firebase pdf deletion failed: ${e.message}")
        }
    }

    // 5. Firestore: Get All PDF Documents
    suspend fun getAllPdfs(): List<PdfDocument> = withContext(Dispatchers.IO) {
        if (!isFirebaseOnline) return@withContext emptyList()
        try {
            val mDb = db ?: return@withContext emptyList()
            val querySnapshot = mDb.collection("pdfs").get().awaitTask()
            val pdfList = mutableListOf<PdfDocument>()
            for (doc in querySnapshot.documents) {
                val idLong = doc.getLong("id") ?: 0L
                val semesterLong = doc.getLong("semester") ?: 1L
                val downloadCountLong = doc.getLong("downloadCount") ?: 0L
                val uploadDateLong = doc.getLong("uploadDate") ?: System.currentTimeMillis()
                val pdf = PdfDocument(
                    id = idLong.toInt(),
                    title = doc.getString("title") ?: "",
                    department = doc.getString("department") ?: "",
                    semester = semesterLong.toInt(),
                    subjectCode = doc.getString("subjectCode") ?: "",
                    subjectName = doc.getString("subjectName") ?: "",
                    fileType = doc.getString("fileType") ?: "",
                    fileSize = doc.getString("fileSize") ?: "0 MB",
                    downloadCount = downloadCountLong.toInt(),
                    isDownloaded = doc.getBoolean("isDownloaded") ?: false,
                    isUserUploaded = doc.getBoolean("isUserUploaded") ?: false,
                    uploadDate = uploadDateLong
                )
                pdfList.add(pdf)
            }
            return@withContext pdfList
        } catch (e: Exception) {
            Log.e(TAG, "Firebase fetch pdfs failed: ${e.message}")
            return@withContext emptyList()
        }
    }

    // 6. Firestore: Sync Class Representatives
    suspend fun uploadCR(cr: ClassRepresentative) = withContext(Dispatchers.IO) {
        if (!isFirebaseOnline) return@withContext
        try {
            val mDb = db ?: return@withContext
            val crMap = hashMapOf(
                "name" to cr.name,
                "passcode" to cr.passcode,
                "department" to cr.department
            )
            mDb.collection("crs").document(cr.passcode).set(crMap).awaitTask()
            Log.d(TAG, "CR added/updated on Firestore: ${cr.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Firebase CR upload failed: ${e.message}")
        }
    }

    // 7. Firestore: Delete Class Representative
    suspend fun deleteCR(passcode: String) = withContext(Dispatchers.IO) {
        if (!isFirebaseOnline) return@withContext
        try {
            val mDb = db ?: return@withContext
            mDb.collection("crs").document(passcode).delete().awaitTask()
            Log.d(TAG, "CR deleted from Firestore: $passcode")
        } catch (e: Exception) {
            Log.e(TAG, "Firebase CR deletgion failed: ${e.message}")
        }
    }

    // 8. Firestore: Get All Class Representatives
    suspend fun getAllCRs(): List<ClassRepresentative> = withContext(Dispatchers.IO) {
        if (!isFirebaseOnline) return@withContext emptyList()
        try {
            val mDb = db ?: return@withContext emptyList()
            val querySnapshot = mDb.collection("crs").get().awaitTask()
            val crsList = mutableListOf<ClassRepresentative>()
            for (doc in querySnapshot.documents) {
                val cr = ClassRepresentative(
                    name = doc.getString("name") ?: "",
                    passcode = doc.getString("passcode") ?: "",
                    department = doc.getString("department") ?: ""
                )
                crsList.add(cr)
            }
            return@withContext crsList
        } catch (e: Exception) {
            Log.e(TAG, "Firebase fetch CRs failed: ${e.message}")
            return@withContext emptyList()
        }
    }
}
