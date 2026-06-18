package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.PdfDocument
import com.example.data.PdfRepository
import com.example.data.ClassRepresentative
import com.example.data.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SignUpResult {
    SUCCESS,
    ALREADY_EXISTS,
    INVALID_EMAIL,
    EMPTY_FIELDS
}

class PdfViewModel(private val repository: PdfRepository) : ViewModel() {

    // General User Authentication State
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    suspend fun signUpUser(email: String, idNo: String, pass: String): SignUpResult {
        val trimmedEmail = email.trim()
        val trimmedId = idNo.trim()
        val trimmedPass = pass.trim()

        if (trimmedEmail.isEmpty() || trimmedId.isEmpty() || trimmedPass.isEmpty()) {
            return SignUpResult.EMPTY_FIELDS
        }

        // Validate IIUC institutional Email (e.g. q251064@ugrad.iiuc.ac.bd or q251064@iiuc.ac.bd)
        val lowerEmail = trimmedEmail.lowercase()
        if (!lowerEmail.endsWith("@ugrad.iiuc.ac.bd") && !lowerEmail.endsWith("@iiuc.ac.bd")) {
            return SignUpResult.INVALID_EMAIL
        }

        // Check if user already exists
        val existing = repository.getUserByIdNo(trimmedId)
        if (existing != null) {
            return SignUpResult.ALREADY_EXISTS
        }

        // Save User in the Room database
        val newUser = User(idNo = trimmedId, email = trimmedEmail, password = trimmedPass)
        repository.insertUser(newUser)
        _currentUser.value = newUser

        // Authorize Role during Signup
        if (trimmedId.equals("q251064", ignoreCase = true)) {
            _isAdminMode.value = true
            _activeCR.value = null
        } else {
            val crMatch = allCRs.value.firstOrNull { it.passcode.equals(trimmedId, ignoreCase = true) }
            if (crMatch != null) {
                _activeCR.value = crMatch
                _isAdminMode.value = false
            } else {
                _activeCR.value = null
                _isAdminMode.value = false
            }
        }

        return SignUpResult.SUCCESS
    }

    suspend fun signInUser(idNo: String, pass: String): Boolean {
        val trimmedId = idNo.trim()
        val trimmedPass = pass.trim()

        if (trimmedId.isEmpty() || trimmedPass.isEmpty()) {
            return false
        }

        // 1. Default Admin check: default admin id and password is q251064
        if (trimmedId.equals("q251064", ignoreCase = true) && trimmedPass == "q251064") {
            var adminUser = repository.getUserByIdNo("q251064")
            if (adminUser == null) {
                 adminUser = User("q251064", "q251064@ugrad.iiuc.ac.bd", "q251064")
                 repository.insertUser(adminUser)
            }
            _currentUser.value = adminUser
            _isAdminMode.value = true
            _activeCR.value = null
            return true
        }

        // 2. Database user check
        val user = repository.getUserByIdNo(trimmedId)
        if (user != null && user.password == trimmedPass) {
            _currentUser.value = user

            // check if the ID matches as CR
            if (trimmedId.equals("q251064", ignoreCase = true)) {
                _isAdminMode.value = true
                _activeCR.value = null
            } else {
                val crMatch = allCRs.value.firstOrNull { it.passcode.equals(trimmedId, ignoreCase = true) }
                if (crMatch != null) {
                    _activeCR.value = crMatch
                    _isAdminMode.value = false
                } else {
                    _activeCR.value = null
                    _isAdminMode.value = false
                }
            }
            return true
        }
        return false
    }

    fun signOutUser() {
        _currentUser.value = null
        // Also logout of Admin / CR states on general sign out
        _isAdminMode.value = false
        _activeCR.value = null
    }

    // Selections
    private val _currentDept = MutableStateFlow("CSE")
    val currentDept = _currentDept.asStateFlow()

    private val _currentSemester = MutableStateFlow(1)
    val currentSemester = _currentSemester.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedTypeFilter = MutableStateFlow("All") // "All", "Slide", "Book", "Note", "Question"
    val selectedTypeFilter = _selectedTypeFilter.asStateFlow()

    // Mode
    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode = _isAdminMode.asStateFlow()

    // Active Class Representative logged in
    private val _activeCR = MutableStateFlow<ClassRepresentative?>(null)
    val activeCR = _activeCR.asStateFlow()

    // All registered CRs
    val allCRs: StateFlow<List<ClassRepresentative>> = repository.allCRs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Download state map: document id -> progress percentage (0 to 100)
    private val _downloadProgress = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val downloadProgress = _downloadProgress.asStateFlow()

    // All PDFs from DB
    val allPdfs: StateFlow<List<PdfDocument>> = repository.allPdfs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Dynamic departments list
    private val _customDepartments = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val departments: StateFlow<List<Pair<String, String>>> = combine(
        allPdfs,
        _customDepartments
    ) { pdfs, custom ->
        val defaultList = listOf(
            "CSE" to "Computer Science",
            "EEE" to "Electrical Eng.",
            "PHARM" to "Pharmacy Dept"
        )
        val existingCodes = (defaultList.map { it.first } + custom.map { it.first }).toSet()
        val extraPdfsDepts = pdfs.map { it.department }.distinct()
            .filter { it.isNotEmpty() && !existingCodes.contains(it.uppercase()) }
            .map { it.uppercase() to it }
        
        defaultList + custom + extraPdfsDepts
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf(
            "CSE" to "Computer Science",
            "EEE" to "Electrical Eng.",
            "PHARM" to "Pharmacy Dept"
        )
    )

    fun addDepartment(code: String, name: String) {
        val uppercaseCode = code.trim().uppercase()
        if (uppercaseCode.isNotEmpty()) {
            viewModelScope.launch {
                val currentDepts = departments.value
                if (!currentDepts.any { it.first == uppercaseCode }) {
                    _customDepartments.value = _customDepartments.value + (uppercaseCode to name.trim().ifEmpty { uppercaseCode })
                }
            }
        }
    }

    // Filtered lists
    val filteredPdfs: StateFlow<List<PdfDocument>> = combine(
        allPdfs,
        _currentDept,
        _currentSemester,
        _selectedTypeFilter,
        _searchQuery
    ) { pdfs, dept, sem, type, query ->
        pdfs.filter { pdf ->
            // Filter by Dept
            val matchesDept = pdf.department.equals(dept, ignoreCase = true)
            // Filter by Semester
            val matchesSem = pdf.semester == sem
            // Filter by Type
            val matchesType = type == "All" || pdf.fileType.equals(type, ignoreCase = true)
            // Filter by Query
            val matchesQuery = query.isEmpty() ||
                    pdf.title.contains(query, ignoreCase = true) ||
                    pdf.subjectCode.contains(query, ignoreCase = true) ||
                    pdf.subjectName.contains(query, ignoreCase = true)

            matchesDept && matchesSem && matchesType && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Downloaded PDFs for Offline list overview
    val downloadedPdfs: StateFlow<List<PdfDocument>> = repository.downloadedPdfs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Run database seeding on start
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }
    }

    fun setDepartment(dept: String) {
        _currentDept.value = dept
        // Reset type when department shifts to keep user experience consistent
        _selectedTypeFilter.value = "All"
    }

    fun setSemester(semester: Int) {
        _currentSemester.value = semester
        _selectedTypeFilter.value = "All"
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setTypeFilter(type: String) {
        _selectedTypeFilter.value = type
    }

    fun toggleAdminMode() {
        _isAdminMode.value = !_isAdminMode.value
        if (!_isAdminMode.value) {
            _activeCR.value = null
        }
    }

    // Login checking admin seed + CR database entries
    fun tryLoginWithPasscode(passcode: String): String {
        val trimmed = passcode.trim()
        if (trimmed == "iiuc123") {
            _isAdminMode.value = true
            _activeCR.value = null
            return "ADMIN"
        }
        val match = allCRs.value.firstOrNull { it.passcode.equals(trimmed, ignoreCase = false) }
        if (match != null) {
            _activeCR.value = match
            _isAdminMode.value = false
            return "CR"
        }
        return "NONE"
    }

    fun logout() {
        _isAdminMode.value = false
        _activeCR.value = null
    }

    fun addCR(name: String, passcode: String, dept: String) {
        viewModelScope.launch {
            repository.insertCR(
                ClassRepresentative(
                    name = name.trim(),
                    passcode = passcode.trim(),
                    department = dept.trim().uppercase()
                )
            )
        }
    }

    fun deleteCR(cr: ClassRepresentative) {
        viewModelScope.launch {
            repository.deleteCR(cr)
        }
    }

    // Admin commands
    fun addPdf(title: String, dept: String, sem: Int, subCode: String, subName: String, type: String, size: String) {
        viewModelScope.launch {
            val newPdf = PdfDocument(
                title = title,
                department = dept,
                semester = sem,
                subjectCode = subCode,
                subjectName = subName,
                fileType = type,
                fileSize = size,
                isUserUploaded = true
            )
            repository.insertPdf(newPdf)
        }
    }

    fun deletePdf(pdf: PdfDocument) {
        viewModelScope.launch {
            repository.deletePdf(pdf)
        }
    }

    // Download process simulation
    fun startDownload(pdf: PdfDocument) {
        if (pdf.isDownloaded) return
        val docId = pdf.id

        viewModelScope.launch {
            // Simulated downloading loop
            for (progress in 0..100 step 20) {
                _downloadProgress.value = _downloadProgress.value + (docId to progress)
                delay(300) // 300ms intervals
            }
            // Update physical record in database to isDownloaded = true
            val updatedPdf = pdf.copy(
                isDownloaded = true,
                downloadCount = pdf.downloadCount + 1
            )
            repository.updatePdf(updatedPdf)

            // Remove from progress tracker
            _downloadProgress.value = _downloadProgress.value - docId
        }
    }

    // Delete download cache
    fun removeDownload(pdf: PdfDocument) {
        viewModelScope.launch {
            val updatedPdf = pdf.copy(isDownloaded = false)
            repository.updatePdf(updatedPdf)
        }
    }
}

class PdfViewModelFactory(private val repository: PdfRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PdfViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PdfViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
