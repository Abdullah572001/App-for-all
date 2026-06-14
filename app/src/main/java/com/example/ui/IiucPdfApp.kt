package com.example.ui

import android.widget.Toast
import com.example.ui.theme.*
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.PdfDocument
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IiucPdfApp(viewModel: PdfViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ViewModel States
    val selectedDept by viewModel.currentDept.collectAsStateWithLifecycle()
    val selectedSemester by viewModel.currentSemester.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedTypeFilter by viewModel.selectedTypeFilter.collectAsStateWithLifecycle()
    val isAdminMode by viewModel.isAdminMode.collectAsStateWithLifecycle()
    val filteredPdfs by viewModel.filteredPdfs.collectAsStateWithLifecycle()
    val downloadedPdfs by viewModel.downloadedPdfs.collectAsStateWithLifecycle()
    val downloadProgress by viewModel.downloadProgress.collectAsStateWithLifecycle()

    // Local controller states
    var showAdminDialog by remember { mutableStateOf(false) }
    var showUploadDialog by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf(0) } // 0: All Archive, 1: Saved Offline

    val departments = listOf(
        "CSE" to "Computer Science",
        "EEE" to "Electrical Eng.",
        "PHARM" to "Pharmacy Dept"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "IIUC PDF Organiser",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = "Academic File Repository • Library Management",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                },
                actions = {
                    // Profile Badge matching the HTML mockup
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Account",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { showAdminDialog = true },
                        modifier = Modifier.testTag("admin_access_button")
                    ) {
                        Icon(
                            imageVector = if (isAdminMode) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = "Admin Toggle",
                            tint = if (isAdminMode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFF3EDF7),
                tonalElevation = 0.dp,
                modifier = Modifier.border(width = 0.8.dp, color = Color(0xFFCAC4D0).copy(alpha = 0.5f))
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(Icons.Default.Cloud, contentDescription = "Archive") },
                    label = { Text("মূল আর্কাইভ", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_archive")
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (downloadedPdfs.isNotEmpty()) {
                                    Badge { Text(downloadedPdfs.size.toString()) }
                                }
                            }
                        ) {
                            Icon(Icons.Default.DownloadDone, contentDescription = "Saved")
                        }
                    },
                    label = { Text("গচ্ছিত ফাইলসমূহ", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_offline")
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings Icon") },
                    label = { Text("অ্যাডমিন", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_admin")
                )
            }
        },
        floatingActionButton = {
            if (isAdminMode && activeTab == 0) {
                ExtendedFloatingActionButton(
                    text = { Text("নতুন ফাইল যোগ করুন", color = Color.White) },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Upload", tint = Color.White) },
                    onClick = { showUploadDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("admin_fab_upload")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Main Archive View
            if (activeTab == 0) {
                // Search Bar - fully rounded responsive pill
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("বিষয় কোড বা ফাইলের নাম খুঁজুন...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon", tint = Color(0xFF49454F)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear Search")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("search_field"),
                    singleLine = true,
                    shape = CircleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFECE6F0),
                        unfocusedContainerColor = Color(0xFFECE6F0),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedPlaceholderColor = Color(0xFF49454F),
                        unfocusedPlaceholderColor = Color(0xFF49454F),
                        focusedTextColor = Color(0xFF1C1B1F),
                        unfocusedTextColor = Color(0xFF1C1B1F)
                    )
                )

                // Dept Switcher Row - Rounded full pills matching the HTML Quick Filters
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    departments.forEach { (deptCode, deptLabel) ->
                        val isSelected = selectedDept == deptCode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else Color(0xFFF3EDF7)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFCAC4D0),
                                    shape = CircleShape
                                )
                                .clickable { viewModel.setDepartment(deptCode) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = when (deptCode) {
                                        "CSE" -> Icons.Default.Code
                                        "EEE" -> Icons.Default.Bolt
                                        else -> Icons.Default.MedicalServices
                                    },
                                    contentDescription = deptCode,
                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = deptCode,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color.White else Color(0xFF49454F)
                                )
                            }
                        }
                    }
                }

                // Semester Selector Horizontal Scroll
                Text(
                    text = "সেমেস্টার নির্বাচন করুন:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(start = 18.dp, top = 12.dp, bottom = 4.dp)
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(8) { index ->
                        val semesterNum = index + 1
                        val isSelected = selectedSemester == semesterNum
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setSemester(semesterNum) },
                            label = { Text("Semester $semesterNum", fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            ),
                            modifier = Modifier.testTag("semester_chip_$semesterNum")
                        )
                    }
                }

                // Categorized Folders Grid
                // Mandated Requirement: "প্রতি সেমিস্টারের প্রশ্নগুলো আলাদাভাবে একটি ফোল্ডারে যেন সাজানো থাকে।"
                // Automatically groups questions separately in a gold styled folder, alongside other lecture documents.
                Text(
                    text = "ডকুমেন্ট ক্যাটাগরি ফোল্ডারসমূহ:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(start = 18.dp, top = 12.dp, bottom = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Question Folder (Special Gold Highlights)
                    FolderCard(
                        title = "প্রশ্ন ব্যাংক",
                        englishLabel = "Exam Questions",
                        count = filteredPdfs.count { it.fileType == "Question" },
                        isSelected = selectedTypeFilter == "Question",
                        isSpecial = true,
                        icon = Icons.Default.Article,
                        onClick = {
                            if (selectedTypeFilter == "Question") viewModel.setTypeFilter("All")
                            else viewModel.setTypeFilter("Question")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("folder_questions")
                    )

                    // Lecture Slides Folder
                    FolderCard(
                        title = "লেকচার স্লাইড",
                        englishLabel = "Syllabus Slides",
                        count = filteredPdfs.count { it.fileType == "Slide" },
                        isSelected = selectedTypeFilter == "Slide",
                        isSpecial = false,
                        icon = Icons.Default.PlayArrow,
                        onClick = {
                            if (selectedTypeFilter == "Slide") viewModel.setTypeFilter("All")
                            else viewModel.setTypeFilter("Slide")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("folder_slides")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Textbooks Folder
                    FolderCard(
                        title = "শ্রেণী পাঠ্যবই",
                        englishLabel = "Text Books",
                        count = filteredPdfs.count { it.fileType == "Book" },
                        isSelected = selectedTypeFilter == "Book",
                        isSpecial = false,
                        icon = Icons.Default.Book,
                        onClick = {
                            if (selectedTypeFilter == "Book") viewModel.setTypeFilter("All")
                            else viewModel.setTypeFilter("Book")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("folder_books")
                    )

                    // Lecture Notes Folder
                    FolderCard(
                        title = "আজকের নোটস",
                        englishLabel = "Handwritten Notes",
                        count = filteredPdfs.count { it.fileType == "Note" },
                        isSelected = selectedTypeFilter == "Note",
                        isSpecial = false,
                        icon = Icons.Default.Description,
                        onClick = {
                            if (selectedTypeFilter == "Note") viewModel.setTypeFilter("All")
                            else viewModel.setTypeFilter("Note")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("folder_notes")
                    )
                }

                // Current Filter Header Status info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedTypeFilter == "All") "সকল ডকুমেন্টস (${filteredPdfs.size})"
                        else "ফোল্ডার ফিল্টার: ${selectedTypeFilter} (${filteredPdfs.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    if (selectedTypeFilter != "All") {
                        TextButton(
                            onClick = { viewModel.setTypeFilter("All") },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("ফিল্টার মুছুন", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                        }
                    }
                }

                // PDF Document lists
                if (filteredPdfs.isEmpty()) {
                    EmptyStatesView(
                        message = "এই সেকশনে এখনও কোনো পিডিএফ আপলোড করা হয়নি। " +
                                if (isAdminMode) "নতুন ও অটোমেটিক ফোল্ডারে যোগ করতে নিচের বাটনে চাপ দিন।"
                                else "আপনি এডমিন প্যানেল থেকে ফাইল আপলোড করে সাজাতে পারেন।"
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filteredPdfs, key = { it.id }) { pdf ->
                            PdfDocItemCard(
                                pdf = pdf,
                                isDownloading = downloadProgress.containsKey(pdf.id),
                                progress = downloadProgress[pdf.id] ?: 0,
                                isAdmin = isAdminMode,
                                onDownload = { viewModel.startDownload(pdf) },
                                onDelete = { viewModel.deletePdf(pdf) }
                            )
                        }
                    }
                }
            } else if (activeTab == 1) {
                // "Offline Downloads" Mode Tab View
                Text(
                    text = "আপনার ডাউনলোডের তালিকা (${downloadedPdfs.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(start = 18.dp, top = 16.dp, bottom = 8.dp)
                )

                if (downloadedPdfs.isEmpty()) {
                    EmptyStatesView(
                        message = "আপনার অফলাইনে পড়ার জন্য কোনো ফাইল ডাউনলোড করা নেই। প্রধান আর্কাইভ থেকে ফাইল ডাউনলোড করুন।"
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        items(downloadedPdfs, key = { it.id }) { pdf ->
                            PdfDocItemCard(
                                pdf = pdf,
                                isDownloading = false,
                                progress = 0,
                                isAdmin = false,
                                onDownload = {},
                                onDeleteDownload = { viewModel.removeDownload(pdf) }
                            )
                        }
                    }
                }
            } else {
                // "Admin Panel" Mode Tab View
                AdminDashboardView(
                    viewModel = viewModel,
                    isAdminMode = isAdminMode
                )
            }
        }
    }

    // Passcode Verification dialog for Admin Access Panel
    if (showAdminDialog) {
        var passcode by remember { mutableStateOf("") }
        var isError by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showAdminDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "IIUC এডমিন অ্যাক্সেস",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "শিক্ষার্থীদের জন্য নতুন কুইজ প্রশ্ন ও লেকচার স্লাইড ক্যাটাগরাইজ ও আপলোড করতে পাসকোড দিন",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = passcode,
                        onValueChange = {
                            passcode = it
                            isError = false
                        },
                        label = { Text("এডমিন সিক্রেট কোড") },
                        placeholder = { Text("যেমন: iiuc123") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = isError,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_passcode_field"),
                        singleLine = true
                    )

                    if (isError) {
                        Text(
                            text = "ভুল পাসকোড! অনুগ্রহ করে 'iiuc123' টাইপ করুন।",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = { showAdminDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("বাতিল")
                        }

                        Button(
                            onClick = {
                                if (passcode == "iiuc123") {
                                    viewModel.toggleAdminMode()
                                    showAdminDialog = false
                                    Toast.makeText(context, "এডমিন মুড সফলভাবে সক্রিয় হয়েছে!", Toast.LENGTH_SHORT).show()
                                } else {
                                    isError = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("admin_verify_button")
                        ) {
                            Text("যাচাই করুন", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Dialog for Admin Uploading / Automated Category Selection
    if (showUploadDialog) {
        var uploadTitle by remember { mutableStateOf("") }
        var uploadDept by remember { mutableStateOf(selectedDept) }
        var uploadSemester by remember { mutableStateOf(selectedSemester.toString()) }
        var uploadSubCode by remember { mutableStateOf("") }
        var uploadSubName by remember { mutableStateOf("") }
        var uploadType by remember { mutableStateOf("Slide") } // "Slide", "Book", "Note", "Question"
        var uploadSize by remember { mutableStateOf("2.5 MB") }

        var isTitleError by remember { mutableStateOf(false) }
        var isCodeError by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showUploadDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "নতুন ফাইল স্বয়ংক্রিয় আপলোড",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "এখানে আপলোড করা ফাইলগুলো স্বয়ংক্রিয়ভাবে সংশ্লিষ্ট সাবজেক্ট ও সেমেস্টারের ক্যাটাগরিতে সর্ট হয়ে যাবে।",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            lineHeight = 14.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    item {
                        OutlinedTextField(
                            value = uploadTitle,
                            onValueChange = {
                                uploadTitle = it
                                isTitleError = false
                            },
                            label = { Text("ফাইলের নাম / শিরোনাম") },
                            placeholder = { Text("যেমন: Midterm Solution 2024") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("upload_title_field"),
                            isError = isTitleError,
                            singleLine = true
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = uploadSubCode,
                            onValueChange = {
                                uploadSubCode = it
                                isCodeError = false
                            },
                            label = { Text("বিষয় কোড (Subject Code)") },
                            placeholder = { Text("যেমন: CSE-3505") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("upload_code_field"),
                            isError = isCodeError,
                            singleLine = true
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = uploadSubName,
                            onValueChange = { uploadSubName = it },
                            label = { Text("বিষয়ের নাম (Subject Name)") },
                            placeholder = { Text("যেমন: Computer Networks") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    // Select Dept & Semester
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Dept Selection Switch
                            Column(modifier = Modifier.weight(1f)) {
                                Text("ডিপার্টমেন্ট", style = MaterialTheme.typography.labelSmall)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(uploadDept, fontWeight = FontWeight.SemiBold)
                                    // Simulated dropdown
                                    Row {
                                        listOf("CSE", "EEE", "PHARM").forEach { itemDept ->
                                            if (itemDept != uploadDept) {
                                                Text(
                                                    text = itemDept,
                                                    fontSize = 10.sp,
                                                    modifier = Modifier
                                                        .clickable { uploadDept = itemDept }
                                                        .background(Color.LightGray.copy(alpha = 0.3f), CircleShape)
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            // Sem Selection
                            Column(modifier = Modifier.weight(1f)) {
                                Text("সেমেস্টার (১-৮)", style = MaterialTheme.typography.labelSmall)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = uploadSemester,
                                    onValueChange = {
                                        val v = it.toIntOrNull()
                                        if (v == null || v in 1..8) {
                                            uploadSemester = it
                                        }
                                    },
                                    placeholder = { Text("1-8") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        }
                    }

                    // Document Type and Dummy Size File
                    item {
                        Text("ডকুমেন্টের প্রকারভেদ (Type)", style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "Slide" to "লেকচার স্লাইড",
                                "Book" to "পাঠ্যবই",
                                "Note" to "খাতার নোট",
                                "Question" to "প্রশ্ন ব্যাংক" // Mandated separate folder sorting
                            ).forEach { (typeVal, typeBangla) ->
                                val isTypeSel = uploadType == typeVal
                                FilterChip(
                                    selected = isTypeSel,
                                    onClick = { uploadType = typeVal },
                                    label = { Text(typeBangla, fontSize = 10.sp) },
                                    modifier = Modifier.testTag("upload_type_$typeVal")
                                )
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = uploadSize,
                            onValueChange = { uploadSize = it },
                            label = { Text("ফাইলের সাইজ প্রাক্কলন") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = { showUploadDialog = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("বাতিল")
                            }

                            Button(
                                onClick = {
                                    if (uploadTitle.trim().isEmpty()) {
                                        isTitleError = true
                                    }
                                    if (uploadSubCode.trim().isEmpty()) {
                                        isCodeError = true
                                    }

                                    if (uploadTitle.trim().isNotEmpty() && uploadSubCode.trim().isNotEmpty()) {
                                        val semInt = uploadSemester.toIntOrNull() ?: 1
                                        val subNamelocal = uploadSubName.ifEmpty { "Academic Subject" }
                                        viewModel.addPdf(
                                            title = uploadTitle,
                                            dept = uploadDept,
                                            sem = semInt,
                                            subCode = uploadSubCode.uppercase(),
                                            subName = subNamelocal,
                                            type = uploadType,
                                            size = uploadSize
                                        )
                                        showUploadDialog = false
                                        Toast.makeText(context, "ফাইলটি সফলভাবে আপলোড ও সাজানো হয়েছে!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .weight(1.5f)
                                    .testTag("submit_upload_button")
                            ) {
                                Text("সংরক্ষণ করুন", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Subordinate folder card representation widget
@Composable
fun FolderCard(
    title: String,
    englishLabel: String,
    count: Int,
    isSelected: Boolean,
    isSpecial: Boolean, // e.g. Question Bank uses highlighted borders
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else if (isSpecial) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    } else {
        Color(0xFFCAC4D0)
    }

    val containerColor = if (isSelected) {
        Color(0xFFEADDFF)
    } else if (isSpecial) {
        Color(0xFFEADDFF).copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier
            .border(
                width = if (isSelected || isSpecial) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSpecial) Color(0xFF6750A4)
                        else Color(0xFFD0BCFF)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isSpecial) Color.White else Color(0xFF21005D),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isSpecial && !isSelected) Color(0xFF21005D) else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = englishLabel,
                    fontSize = 11.sp,
                    color = if (isSpecial && !isSelected) Color(0xFF49454F) else Color.Gray,
                    maxLines = 1
                )
            }

            // Document Count Badge
            Box(
                modifier = Modifier
                    .background(
                        if (isSelected || isSpecial) Color(0xFF6750A4)
                        else Color(0xFFECE6F0),
                        CircleShape
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = count.toString(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected || isSpecial) Color.White else Color(0xFF49454F)
                )
            }
        }
    }
}

// Visual layout representive of items
@Composable
fun PdfDocItemCard(
    pdf: PdfDocument,
    isDownloading: Boolean,
    progress: Int,
    isAdmin: Boolean,
    onDownload: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onDeleteDownload: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(16.dp))
            .testTag("pdf_item_${pdf.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Course Code Badge
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = pdf.subjectCode,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // File Type Badge
                val typeColor = when (pdf.fileType) {
                    "Question" -> Color(0xFF6750A4)
                    "Book" -> Color(0xFFE67E22)
                    "Note" -> Color(0xFF2980B9)
                    else -> MaterialTheme.colorScheme.primary
                }

                Box(
                    modifier = Modifier
                        .background(typeColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = when (pdf.fileType) {
                            "Question" -> "Board Question (প্রশ্নসমূহ)"
                            "Book" -> "Text Book (বই)"
                            "Note" -> "Lecture Note"
                            else -> "Lecture Slide"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = typeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Document Title
            Text(
                text = pdf.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF1C1B1F),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Subject label
            Text(
                text = "Course: ${pdf.subjectName}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 3.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            HorizontalDivider(color = Color(0xFFCAC4D0).copy(alpha = 0.4f), thickness = 1.dp)

            Spacer(modifier = Modifier.height(10.dp))

            // Footer info / actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "size details",
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Size: ${pdf.fileSize}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    if (pdf.downloadCount > 0) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = "downloads",
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${pdf.downloadCount} downloads",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Interactive download option
                Box(contentAlignment = Alignment.Center) {
                    if (isDownloading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                progress = { progress / 100f },
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$progress%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else if (pdf.isDownloaded) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                onDeleteDownload?.invoke()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Saved Offline",
                                tint = Color(0xFF27AE60),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (onDeleteDownload != null) "মুছে ফেলুন" else "Saved Offline",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (onDeleteDownload != null) MaterialTheme.colorScheme.error else Color(0xFF27AE60)
                            )
                        }
                    } else {
                        // Action button
                        IconButton(
                            onClick = onDownload,
                            modifier = Modifier
                                .size(34.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    CircleShape
                                )
                                .testTag("btn_download_${pdf.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download Item",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Delete resource button in Admin Dashboard
                if (isAdmin && onDelete != null) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(34.dp)
                            .background(
                                MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                                CircleShape
                            )
                            .testTag("admin_btn_delete_${pdf.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Item",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStatesView(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FolderOpen,
            contentDescription = "Empty State Folder",
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            modifier = Modifier.size(60.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "কোনো ফাইল পাওয়া যায়নি!",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun AdminDashboardView(
    viewModel: PdfViewModel,
    isAdminMode: Boolean
) {
    val context = LocalContext.current
    var passcode by remember { mutableStateOf("") }
    var isPasscodeError by remember { mutableStateOf(false) }

    // Upload Form input values
    var titleInput by remember { mutableStateOf("") }
    var subCodeInput by remember { mutableStateOf("") }
    var subNameInput by remember { mutableStateOf("") }
    var deptInput by remember { mutableStateOf("CSE") }
    var semesterInput by remember { mutableStateOf("1") }
    var typeInput by remember { mutableStateOf("Slide") }
    var sizeInput by remember { mutableStateOf("3.2 MB") }

    var isTitleError by remember { mutableStateOf(false) }
    var isSubCodeError by remember { mutableStateOf(false) }

    val allPdfs by viewModel.allPdfs.collectAsStateWithLifecycle()
    
    // Admin list filtering by department
    var filterDept by remember { mutableStateOf("All") }

    if (!isAdminMode) {
        // RENDER PASSCODE ACCESS GATE
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Secure Portal",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "প্রশাসক প্রবেশদ্বার",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Admin Access Portal • Restricted",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ডকুমেন্ট আপলোড, বিভাগ যুক্তকরণ ও ফাইল ডাটাবেজ পরিচালনার জন্য সঠিক সিক্রেট পাসকোড প্রদান করুন।",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF49454F),
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    OutlinedTextField(
                        value = passcode,
                        onValueChange = {
                            passcode = it
                            isPasscodeError = false
                        },
                        label = { Text("এডমিন সিক্রেট পাসকোড") },
                        placeholder = { Text("যেমন: iiuc123") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = isPasscodeError,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_dashboard_passcode_field"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (isPasscodeError) {
                        Text(
                            text = "ভুল কোড! সঠিক কোডটি টাইপ করুন (iiuc123)।",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(top = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (passcode == "iiuc123") {
                                viewModel.toggleAdminMode()
                                isPasscodeError = false
                                Toast.makeText(context, "এডমিন প্যানেলে স্বাগতম!", Toast.LENGTH_SHORT).show()
                            } else {
                                isPasscodeError = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("admin_dashboard_login_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Unlock", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("প্রবেশ করুন", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    } else {
        // ADMIN AUTHORIZED: RENDER THE BEAUTIFUL CONTROL DASHBOARD
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
        ) {
            // Dashboard Header Banner
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDFF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "অ্যাডমিন ফাইল কন্ট্রোল স্টেশন",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF21005D)
                                )
                                Text(
                                    text = "IIUC Archive Library Management",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF49454F)
                                )
                            }
                            
                            Button(
                                onClick = {
                                    viewModel.toggleAdminMode()
                                    Toast.makeText(context, "এডমিন মুড থেকে প্রস্থান করেছেন!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("লগআউট", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = Color(0xFFCAC4D0).copy(alpha = 0.5f)
                        )

                        // Statistics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("মোট রেজিস্টার্ড ফাইল", fontSize = 10.sp, color = Color(0xFF49454F))
                                Text("${allPdfs.size} টি", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF21005D))
                            }
                            Column {
                                Text("সিএসই বিভাগ (CSE)", fontSize = 10.sp, color = Color(0xFF49454F))
                                Text("${allPdfs.count { it.department == "CSE" }} টি", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF21005D))
                            }
                            Column {
                                Text("অন্যান্য বিভাগ", fontSize = 10.sp, color = Color(0xFF49454F))
                                Text("${allPdfs.count { it.department != "CSE" }} টি", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF21005D))
                            }
                        }
                    }
                }
            }

            // Streamlined Upload Form Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "নতুন ডক আপলোড ও ক্যাটাগরি ম্যাপিং",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // File Title
                        OutlinedTextField(
                            value = titleInput,
                            onValueChange = {
                                titleInput = it
                                isTitleError = false
                            },
                            label = { Text("ফাইলের নাম বা শিরোনাম") },
                            placeholder = { Text("যেমন: Database Systems Lab Sheet") },
                            isError = isTitleError,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_title_field"),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Subject Code
                            OutlinedTextField(
                                value = subCodeInput,
                                onValueChange = {
                                    subCodeInput = it
                                    isSubCodeError = false
                                },
                                label = { Text("বিষয় কোড") },
                                placeholder = { Text("CSE-3512") },
                                isError = isSubCodeError,
                                modifier = Modifier
                                    .weight(1.0f)
                                    .testTag("form_code_field"),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                            // Subject Name
                            OutlinedTextField(
                                value = subNameInput,
                                onValueChange = { subNameInput = it },
                                label = { Text("বিষয়ের নাম") },
                                placeholder = { Text("Database Management") },
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("form_name_field"),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        // Adding Department Selector chips
                        Column {
                            Text("বিভাগ নির্বাচন করুন (Department)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold))
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("CSE", "EEE", "PHARM").forEach { dept ->
                                    val isSel = deptInput == dept
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(CircleShape)
                                            .background(if (isSel) MaterialTheme.colorScheme.primary else Color(0xFFF3EDF7))
                                            .border(1.dp, if (isSel) MaterialTheme.colorScheme.primary else Color(0xFFCAC4D0), CircleShape)
                                            .clickable { deptInput = dept }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dept,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (isSel) Color.White else Color(0xFF49454F)
                                        )
                                    }
                                }
                            }
                        }

                        // Semester Selection (1 to 8)
                        Column {
                            Text("সেমেস্টার নির্বাচন করুন (১-৮)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold))
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(8) { idx ->
                                    val semNumber = (idx + 1).toString()
                                    val isSel = semesterInput == semNumber
                                    Box(
                                        modifier = Modifier
                                            .size(width = 54.dp, height = 36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) MaterialTheme.colorScheme.primary else Color(0xFFF3EDF7))
                                            .border(1.dp, if (isSel) MaterialTheme.colorScheme.primary else Color(0xFFCAC4D0), RoundedCornerShape(8.dp))
                                            .clickable { semesterInput = semNumber },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${semNumber}st",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (isSel) Color.White else Color(0xFF49454F)
                                        )
                                    }
                                }
                            }
                        }

                        // Category Type Selector
                        Column {
                            Text("ফাইলের ধরন (Type Map)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold))
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    "Slide" to "স্লাইড",
                                    "Book" to "বই",
                                    "Note" to "নোট",
                                    "Question" to "প্রশ্ন"
                                ).forEach { (typeVal, label) ->
                                    val isSel = typeInput == typeVal
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) Color(0xFFEADDFF) else Color.Transparent)
                                            .border(1.dp, if (isSel) MaterialTheme.colorScheme.primary else Color(0xFFCAC4D0), RoundedCornerShape(8.dp))
                                            .clickable { typeInput = typeVal }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isSel) Color(0xFF21005D) else Color.Gray
                                        )
                                    }
                                }
                            }
                        }

                        // File size & Save Button
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = sizeInput,
                                onValueChange = { sizeInput = it },
                                label = { Text("সাইজ") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                            Button(
                                onClick = {
                                    if (titleInput.trim().isEmpty()) {
                                        isTitleError = true
                                    }
                                    if (subCodeInput.trim().isEmpty()) {
                                        isSubCodeError = true
                                    }

                                    if (titleInput.trim().isNotEmpty() && subCodeInput.trim().isNotEmpty()) {
                                        val sNum = semesterInput.toIntOrNull() ?: 1
                                        val sName = subNameInput.ifEmpty { "Academic Course" }
                                        viewModel.addPdf(
                                            title = titleInput.trim(),
                                            dept = deptInput,
                                            sem = sNum,
                                            subCode = subCodeInput.trim().uppercase(),
                                            subName = sName.trim(),
                                            type = typeInput,
                                            size = sizeInput.trim()
                                        )
                                        // Reset Form inputs
                                        titleInput = ""
                                        subCodeInput = ""
                                        subNameInput = ""
                                        Toast.makeText(context, "ফাইল সফলভাবে রেজিস্টর করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(52.dp)
                                    .padding(top = 6.dp)
                                    .testTag("form_submit_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("ডাটাবেজে সেভ", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Database Management Directory List Header with Department Fast Selection
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "সংরক্ষিত ফাইল ডিরেক্টরি পরিচালনা",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1C1B1F)
                    )
                    Text(
                        text = "ফাস্ট ফাইন্ডার ডিপার্টমেন্ট দিয়ে তালিকা ফিল্টার করুন:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                    )

                    // Department filtering row in Admin view
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("All" to "সকল বিভাগ", "CSE" to "CSE", "EEE" to "EEE", "PHARM" to "PHARM").forEach { (code, label) ->
                            val isSel = filterDept == code
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .background(if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                    .border(1.dp, if (isSel) MaterialTheme.colorScheme.primary else Color(0xFFCAC4D0), CircleShape)
                                    .clickable { filterDept = code }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            // Filter pdf documents based on the admin selection
            val adminFilteredPdfs = allPdfs.filter { pdf ->
                filterDept == "All" || pdf.department.equals(filterDept, ignoreCase = true)
            }

            if (adminFilteredPdfs.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFCAC4D0).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("এই বিভাগে কোনো ফাইল পাওয়া যায়নি।", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            } else {
                items(adminFilteredPdfs, key = { pdf -> "admin_${pdf.id}" }) { pdf ->
                    var showDeleteDialog by remember { mutableStateOf(false) }

                    // Elegant Admin List Card with active delete trigger button
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFCAC4D0).copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left badge representing type
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when (pdf.fileType) {
                                            "Question" -> Color(0xFFEADDFF)
                                            "Book" -> Color(0xFFFFE0B2)
                                            "Note" -> Color(0xFFE1F5FE)
                                            else -> Color(0xFFE8F5E9)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (pdf.fileType) {
                                        "Question" -> Icons.Default.Article
                                        "Book" -> Icons.Default.Book
                                        "Note" -> Icons.Default.Description
                                        else -> Icons.Default.Info
                                    },
                                    contentDescription = null,
                                    tint = when (pdf.fileType) {
                                        "Question" -> Color(0xFF21005D)
                                        "Book" -> Color(0xFFE65100)
                                        "Note" -> Color(0xFF01579B)
                                        else -> Color(0xFF1B5E20)
                                    },
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Middle Info
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pdf.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = Color(0xFF1C1B1F)
                                )
                                Text(
                                    text = "${pdf.department} • Semester ${pdf.semester} • ${pdf.subjectCode}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }

                            // Right Action button (🗑️ Delete) with confirmation safety
                            IconButton(
                                onClick = { showDeleteDialog = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.08f), CircleShape)
                                    .testTag("admin_dashboard_delete_${pdf.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete from Room",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Secure Dialog confirmation for deletes
                    if (showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            title = { Text("ফাইল মুছে ফেলার সতর্কতা", fontWeight = FontWeight.Bold) },
                            text = { Text("আপনি কি নিশ্চিতভাবে '${pdf.title}' ফাইলটি আর্কাইভ ও ডাটাবেজ থেকে মুছে ফেলতে চান? এটি আর ফিরিয়ে আনা যাবে না।") },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        viewModel.deletePdf(pdf)
                                        showDeleteDialog = false
                                        Toast.makeText(context, "ফাইলটি ডাটাবেজ থেকে মুছে ফেলা হয়েছে!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("মুছে ফেলুন", color = Color.White)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteDialog = false }) {
                                    Text("বাতিল")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
