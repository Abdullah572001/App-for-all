package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.PdfDocument
import com.example.data.PdfRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PdfViewModel(private val repository: PdfRepository) : ViewModel() {

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
