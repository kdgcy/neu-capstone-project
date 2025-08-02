package com.fak.classmate.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fak.classmate.AppUtil
import com.fak.classmate.model.ValidationState
import com.fak.classmate.viewmodel.AuthViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Profile(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    // State variables for user data
    var firstname by remember { mutableStateOf("") }
    var lastname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    // Separate state for editing (temporary values)
    var editFirstname by remember { mutableStateOf("") }
    var editLastname by remember { mutableStateOf("") }

    var isEditing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // Validation states
    var firstnameValidation by remember { mutableStateOf(ValidationState(true)) }
    var lastnameValidation by remember { mutableStateOf(ValidationState(true)) }

    val context = LocalContext.current
    val currentUser = Firebase.auth.currentUser
    val firestore = Firebase.firestore

    // Load user data when screen opens
    LaunchedEffect(Unit) {
        currentUser?.let { user ->
            isLoading = true
            email = user.email ?: ""

            firestore.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        firstname = document.getString("firstname") ?: ""
                        lastname = document.getString("lastname") ?: ""

                        // Initialize edit values with current values
                        editFirstname = firstname
                        editLastname = lastname

                        // Initialize validation states as valid for existing data
                        firstnameValidation = ValidationState(true)
                        lastnameValidation = ValidationState(true)
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                    AppUtil.showToast(context, "Failed to load profile data")
                }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigateUp()
                    }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Back",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                actions = {
                    if (!isEditing) {
                        IconButton(onClick = {
                            isEditing = true
                            // Set edit values to current values when starting edit
                            editFirstname = firstname
                            editLastname = lastname
                        }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture Section (without Card container)
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (firstname.isNotEmpty() && lastname.isNotEmpty()) {
                        "$firstname $lastname"
                    } else "ClassMate User",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Profile Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Profile Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // First Name Field
                    OutlinedTextField(
                        value = editFirstname,
                        onValueChange = {
                            if (isEditing) {
                                editFirstname = it
                                firstnameValidation = authViewModel.validateFirstname(it)
                            }
                        },
                        label = { Text("First Name") },
                        enabled = isEditing,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = !firstnameValidation.isValid && editFirstname.isNotEmpty(),
                        supportingText = {
                            if (!firstnameValidation.isValid && editFirstname.isNotEmpty()) {
                                Text(
                                    text = firstnameValidation.errorMessage,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Last Name Field
                    OutlinedTextField(
                        value = editLastname,
                        onValueChange = {
                            if (isEditing) {
                                editLastname = it
                                lastnameValidation = authViewModel.validateLastname(it)
                            }
                        },
                        label = { Text("Last Name") },
                        enabled = isEditing,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = !lastnameValidation.isValid && editLastname.isNotEmpty(),
                        supportingText = {
                            if (!lastnameValidation.isValid && editLastname.isNotEmpty()) {
                                Text(
                                    text = lastnameValidation.errorMessage,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email Field (Read-only)
                    OutlinedTextField(
                        value = email,
                        onValueChange = { }, // Email cannot be changed
                        label = { Text("Email") },
                        enabled = false,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            Text(
                                text = "Email cannot be changed",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            if (isEditing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = {
                            // Reset edit values to original and exit edit mode
                            isEditing = false
                            editFirstname = firstname
                            editLastname = lastname
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = {
                            // Validate before saving using edit values
                            firstnameValidation = authViewModel.validateFirstname(editFirstname)
                            lastnameValidation = authViewModel.validateLastname(editLastname)

                            if (firstnameValidation.isValid && lastnameValidation.isValid) {
                                isSaving = true
                                currentUser?.let { user ->
                                    val updates = hashMapOf<String, Any>(
                                        "firstname" to editFirstname,
                                        "lastname" to editLastname
                                    )

                                    firestore.collection("users").document(user.uid)
                                        .update(updates)
                                        .addOnSuccessListener {
                                            // Update the main state variables only on successful save
                                            firstname = editFirstname
                                            lastname = editLastname

                                            isSaving = false
                                            isEditing = false
                                            AppUtil.showToast(context, "Profile updated successfully")
                                        }
                                        .addOnFailureListener {
                                            isSaving = false
                                            AppUtil.showToast(context, "Failed to update profile")
                                        }
                                }
                            } else {
                                AppUtil.showToast(context, "Please fix the errors above")
                            }
                        },
                        enabled = !isSaving && firstnameValidation.isValid && lastnameValidation.isValid,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isSaving) "Saving..." else "Save")
                    }
                }
            } else {
                // Logout Button
                OutlinedButton(
                    onClick = {
                        Firebase.auth.signOut()
                        navController.navigate("auth") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Logout",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}