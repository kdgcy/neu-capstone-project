package com.fak.classmate.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fak.classmate.AppUtil
import com.fak.classmate.model.ValidationState
import com.fak.classmate.viewmodel.AuthViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

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

    // Separate state for editing
    var editFirstname by remember { mutableStateOf("") }
    var editLastname by remember { mutableStateOf("") }

    var isEditing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // Change Password states
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmNewPasswordVisible by remember { mutableStateOf(false) }
    var isChangingPassword by remember { mutableStateOf(false) }
    var newPasswordValidation by remember { mutableStateOf(ValidationState(true)) }
    var confirmNewPasswordValidation by remember { mutableStateOf(ValidationState(true)) }

    var memberSince by remember { mutableStateOf("") }

    // Validation states
    var firstnameValidation by remember { mutableStateOf(ValidationState(true)) }
    var lastnameValidation by remember { mutableStateOf(ValidationState(true)) }

    val context = LocalContext.current
    val currentUser = Firebase.auth.currentUser
    val firestore = Firebase.firestore

    // Load user data
    LaunchedEffect(Unit) {
        currentUser?.let { user ->
            isLoading = true
            email = user.email ?: ""

            // Format member since date
            user.metadata?.creationTimestamp?.let { timestamp ->
                val date = java.util.Date(timestamp)
                val formatter = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault())
                memberSince = formatter.format(date)
            }

            firestore.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        firstname = document.getString("firstname") ?: ""
                        lastname = document.getString("lastname") ?: ""
                        editFirstname = firstname
                        editLastname = lastname
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

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Gradient Header with Profile Picture
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    ) {
                        // Gradient Background
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.primaryContainer
                                        )
                                    )
                                )
                        )

                        // Profile Picture and Name
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.fillMaxSize(),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (firstname.isNotEmpty() && lastname.isNotEmpty()) {
                                    "$firstname $lastname"
                                } else "ClassMate User",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Member since $memberSince",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // Top Bar Icons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-224).dp)
                            .padding(horizontal = 8.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier.size(28.dp),
                                tint = Color.White
                            )
                        }

                        if (!isEditing && !isLoading) {
                            IconButton(onClick = {
                                isEditing = true
                                editFirstname = firstname
                                editLastname = lastname
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Profile",
                                    modifier = Modifier.size(24.dp),
                                    tint = Color.White
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(48.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Profile Information Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Personal Information",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

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

                            Spacer(modifier = Modifier.height(12.dp))

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

                            Spacer(modifier = Modifier.height(12.dp))

                            // Email Field (Read-only)
                            OutlinedTextField(
                                value = email,
                                onValueChange = { },
                                label = { Text("Email") },
                                enabled = false,
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Email,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                supportingText = {
                                    Text(
                                        text = "Email cannot be changed",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Change Password Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Change Password",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Current Password Field
                            OutlinedTextField(
                                value = currentPassword,
                                onValueChange = { currentPassword = it },
                                label = { Text("Current Password") },
                                placeholder = { Text("Enter current password") },
                                visualTransformation = if (currentPasswordVisible)
                                    VisualTransformation.None
                                else
                                    PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
                                        Icon(
                                            imageVector = if (currentPasswordVisible)
                                                Icons.Default.Visibility
                                            else
                                                Icons.Default.VisibilityOff,
                                            contentDescription = "Toggle password visibility"
                                        )
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = isEditing
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // New Password Field
                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = {
                                    newPassword = it
                                    newPasswordValidation = authViewModel.validatePassword(it)
                                },
                                label = { Text("New Password") },
                                placeholder = { Text("Enter new password") },
                                visualTransformation = if (newPasswordVisible)
                                    VisualTransformation.None
                                else
                                    PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                        Icon(
                                            imageVector = if (newPasswordVisible)
                                                Icons.Default.Visibility
                                            else
                                                Icons.Default.VisibilityOff,
                                            contentDescription = "Toggle password visibility"
                                        )
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = isEditing,
                                isError = !newPasswordValidation.isValid && newPassword.isNotEmpty(),
                                supportingText = {
                                    if (!newPasswordValidation.isValid && newPassword.isNotEmpty()) {
                                        Text(
                                            text = newPasswordValidation.errorMessage,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    } else {
                                        Text(
                                            text = "Must be 6+ characters with uppercase, lowercase, and number",
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Confirm New Password Field
                            OutlinedTextField(
                                value = confirmNewPassword,
                                onValueChange = {
                                    confirmNewPassword = it
                                    confirmNewPasswordValidation = authViewModel.validateConfirmPassword(newPassword, it)
                                },
                                label = { Text("Confirm New Password") },
                                placeholder = { Text("Re-enter new password") },
                                visualTransformation = if (confirmNewPasswordVisible)
                                    VisualTransformation.None
                                else
                                    PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { confirmNewPasswordVisible = !confirmNewPasswordVisible }) {
                                        Icon(
                                            imageVector = if (confirmNewPasswordVisible)
                                                Icons.Default.Visibility
                                            else
                                                Icons.Default.VisibilityOff,
                                            contentDescription = "Toggle password visibility"
                                        )
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = isEditing,
                                isError = !confirmNewPasswordValidation.isValid && confirmNewPassword.isNotEmpty(),
                                supportingText = {
                                    if (!confirmNewPasswordValidation.isValid && confirmNewPassword.isNotEmpty()) {
                                        Text(
                                            text = confirmNewPasswordValidation.errorMessage,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Change Password Button
                            Button(
                                onClick = {
                                    newPasswordValidation = authViewModel.validatePassword(newPassword)
                                    confirmNewPasswordValidation = authViewModel.validateConfirmPassword(newPassword, confirmNewPassword)

                                    if (currentPassword.isBlank()) {
                                        AppUtil.showToast(context, "Please enter your current password")
                                    } else if (newPasswordValidation.isValid && confirmNewPasswordValidation.isValid) {
                                        isChangingPassword = true

                                        // Re-authenticate user first
                                        currentUser?.let { user ->
                                            val credential = EmailAuthProvider.getCredential(
                                                user.email ?: "",
                                                currentPassword
                                            )

                                            user.reauthenticate(credential)
                                                .addOnSuccessListener {
                                                    // Update password
                                                    user.updatePassword(newPassword)
                                                        .addOnSuccessListener {
                                                            isChangingPassword = false
                                                            currentPassword = ""
                                                            newPassword = ""
                                                            confirmNewPassword = ""
                                                            AppUtil.showToast(context, "✅ Password changed successfully!")
                                                        }
                                                        .addOnFailureListener {
                                                            isChangingPassword = false
                                                            AppUtil.showToast(context, "❌ Failed to change password")
                                                        }
                                                }
                                                .addOnFailureListener {
                                                    isChangingPassword = false
                                                    AppUtil.showToast(context, "❌ Current password is incorrect")
                                                }
                                        }
                                    } else {
                                        AppUtil.showToast(context, "Please fix the errors above")
                                    }
                                },
                                enabled = isEditing && !isChangingPassword && currentPassword.isNotBlank() &&
                                        newPassword.isNotBlank() && confirmNewPassword.isNotBlank(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (isChangingPassword) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Text("Change Password")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Action Buttons - Fixed at bottom
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isEditing) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    isEditing = false
                                    editFirstname = firstname
                                    editLastname = lastname
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel")
                            }

                            Button(
                                onClick = {
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
                                                    firstname = editFirstname
                                                    lastname = editLastname
                                                    isSaving = false
                                                    isEditing = false
                                                    AppUtil.showToast(context, "✅ Profile updated successfully")
                                                }
                                                .addOnFailureListener {
                                                    isSaving = false
                                                    AppUtil.showToast(context, "❌ Failed to update profile")
                                                }
                                        }
                                    } else {
                                        AppUtil.showToast(context, "Please fix the errors above")
                                    }
                                },
                                enabled = !isSaving && firstnameValidation.isValid && lastnameValidation.isValid,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Text("Save Changes")
                                }
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
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Logout",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Logout",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}