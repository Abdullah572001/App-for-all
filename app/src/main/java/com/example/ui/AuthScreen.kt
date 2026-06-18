package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AuthScreen(viewModel: PdfViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Switch between Sign In (false) and Sign Up (true)
    var isSignUp by remember { mutableStateOf(false) }

    // Constants for M3 Theme Colors
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    // Input fields state
    var email by remember { mutableStateOf("") }
    var idNo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Validation/Error States
    var emailError by remember { mutableStateOf<String?>(null) }
    var idError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    
    var showPassword by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .widthIn(max = 480.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header Logo Area
            Spacer(modifier = Modifier.height(30.dp))
            
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(primaryColor, tertiaryColor)
                        )
                    )
                    .border(2.dp, Color.White, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = "IIUC PDF Library",
                    tint = Color.White,
                    modifier = Modifier.size(46.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "IIUC PDF Organiser",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = primaryColor,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Academic File Repository & Class Manager",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Main Auth Form Card
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFCAC4D0).copy(alpha = 0.5f), RoundedCornerShape(28.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Sign In / Sign Up Mode Title
                    Text(
                        text = if (isSignUp) "নতুন অ্যাকাউন্ট তৈরি করুন" else "লগইন করুন",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        text = if (isSignUp) 
                            "IIUC ইনস্টিটিউট ইমেইল দিয়ে রেজিস্ট্রার করুন" 
                        else 
                            "আপনার শিক্ষার্থীর আইডি ও পাসওয়ার্ড দিয়ে প্রবেশ করুন",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
                        textAlign = TextAlign.Center
                    )

                    // FIELDS ANIMATION
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Email Field (Only Visible on Sign Up)
                        AnimatedVisibility(
                            visible = isSignUp,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            OutlinedTextField(
                                value = email,
                                onValueChange = {
                                    email = it
                                    emailError = null
                                },
                                label = { Text("ইনস্টিটিউট ইমেইল (Email)") },
                                placeholder = { Text("যেমন: q251064@ugrad.iiuc.ac.bd") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = "Email Icon",
                                        tint = primaryColor
                                    )
                                },
                                isError = emailError != null,
                                supportingText = {
                                    if (emailError != null) {
                                        Text(text = emailError ?: "", color = MaterialTheme.colorScheme.error)
                                    } else {
                                        Text(text = "অবশ্যই @ugrad.iiuc.ac.bd বা @iiuc.ac.bd হতে হবে", fontSize = 10.sp)
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("signup_email_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // ID No Field (Always Visible)
                        OutlinedTextField(
                            value = idNo,
                            onValueChange = {
                                idNo = it
                                idError = null
                            },
                            label = { Text("আইডি নং (ID No.)") },
                            placeholder = { Text("যেমন: c201045 বা q251064") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = "ID Badge Icon",
                                    tint = primaryColor
                                )
                            },
                            isError = idError != null,
                            supportingText = {
                                if (idError != null) {
                                    Text(text = idError ?: "", color = MaterialTheme.colorScheme.error)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(if (isSignUp) "signup_id_input" else "signin_id_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Password Field (Always Visible)
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                passwordError = null
                            },
                            label = { Text("পাসওয়ার্ড (Password)") },
                            placeholder = { Text("গোপন পাসওয়ার্ড") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Lock Icon",
                                    tint = primaryColor
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (showPassword) "Hide password" else "Show password"
                                    )
                                }
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            isError = passwordError != null,
                            supportingText = {
                                if (passwordError != null) {
                                    Text(text = passwordError ?: "", color = MaterialTheme.colorScheme.error)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(if (isSignUp) "signup_password_input" else "signin_password_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Submit Action Button
                    Button(
                        onClick = {
                            var hasError = false
                            
                            // Simple frontend validation
                            if (idNo.trim().isEmpty()) {
                                idError = "আইডি নম্বর খালি হতে পারে না"
                                hasError = true
                            }
                            if (password.trim().isEmpty()) {
                                passwordError = "পাসওয়ার্ড প্রদান করুন"
                                hasError = true
                            }
                            
                            if (isSignUp) {
                                val emailTrimmed = email.trim().lowercase()
                                if (email.trim().isEmpty()) {
                                    emailError = "ইনস্টিটিউট ইমেইল খালি হতে পারে না"
                                    hasError = true
                                } else if (!emailTrimmed.endsWith("@ugrad.iiuc.ac.bd") && !emailTrimmed.endsWith("@iiuc.ac.bd")) {
                                    emailError = "ভুল ইমেইল! IIUC ইনস্টিটিউট ইমেইল প্রদান করুন"
                                    hasError = true
                                }
                            }

                            if (!hasError) {
                                isSubmitting = true
                                scope.launch {
                                    if (isSignUp) {
                                        val res = viewModel.signUpUser(email, idNo, password)
                                        isSubmitting = false
                                        when (res) {
                                            SignUpResult.SUCCESS -> {
                                                Toast.makeText(context, "অ্যাকাউন্ট তৈরি ও সফল লগইন সম্পন্ন হয়েছে!", Toast.LENGTH_LONG).show()
                                            }
                                            SignUpResult.ALREADY_EXISTS -> {
                                                idError = "এই আইডি দিয়ে ইতিমধ্যে অ্যাকাউন্ট নিবন্ধিত রয়েছে"
                                            }
                                            SignUpResult.INVALID_EMAIL -> {
                                                emailError = "ভুল ইমেইল! অনুগ্রহ করে সঠিক ইনস্টিটিউট ইমেইল দিন (যেমন: ugrad.iiuc.ac.bd)"
                                            }
                                            SignUpResult.EMPTY_FIELDS -> {
                                                Toast.makeText(context, "সবগুলো বিবরণ সঠিকভাবে পূরণ করুন", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } else {
                                        val loginSuccess = viewModel.signInUser(idNo, password)
                                        isSubmitting = false
                                        if (loginSuccess) {
                                            Toast.makeText(context, "সফলভাবে লগইন হয়েছে!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "ভুল আইডি নম্বর অথবা পাসওয়ার্ড!", Toast.LENGTH_LONG).show()
                                            idError = "তথ্য যাচাই করুন"
                                            passwordError = "তথ্য যাচাই করুন"
                                        }
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag(if (isSignUp) "signup_button" else "signin_button"),
                        enabled = !isSubmitting
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (isSignUp) Icons.Default.PersonAdd else Icons.Default.Login,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isSignUp) "রেজিস্ট্রেশন সম্পন্ন করুন" else "নিরাপদে লগইন করুন",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Switch Mode Text Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSignUp) "ইতিমধ্যে অ্যাকাউন্ট আছে?" else "নতুন শিক্ষার্থী এখানে?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        TextButton(
                            onClick = {
                                isSignUp = !isSignUp
                                // Clear error messages when toggling
                                emailError = null
                                idError = null
                                passwordError = null
                            },
                            modifier = Modifier.testTag("auth_toggle_button")
                        ) {
                            Text(
                                text = if (isSignUp) "লগইন করুন" else "নতুন অ্যাকাউন্ট খুলুন",
                                fontWeight = FontWeight.Bold,
                                color = primaryColor,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Footer credits Info
            Text(
                text = "International Islamic University Chittagong",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Text(
                text = "© 2026 Department of CSE, IIUC",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
