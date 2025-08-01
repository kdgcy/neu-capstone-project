package com.fak.classmate.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fak.classmate.AppUtil
import com.fak.classmate.model.ValidationState
import com.fak.classmate.viewmodel.AuthViewModel

@Composable
fun SignUp(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        var firstname by remember { mutableStateOf("") }
        var lastname by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        val context = LocalContext.current
        var isLoading by remember { mutableStateOf(false) }

        // Validation states
        var firstnameValidation by remember { mutableStateOf(ValidationState()) }
        var lastnameValidation by remember { mutableStateOf(ValidationState()) }
        var emailValidation by remember { mutableStateOf(ValidationState()) }
        var passwordValidation by remember { mutableStateOf(ValidationState()) }
        var confirmPasswordValidation by remember { mutableStateOf(ValidationState()) }

        // Validation functions
        fun validateFirstname(name: String): ValidationState {
            return when {
                name.isBlank() -> ValidationState(false, "First name is required")
                name.length < 2 -> ValidationState(false, "First name must be at least 2 characters")
                !name.matches(Regex("^[a-zA-Z\\s]+$")) -> ValidationState(false, "First name can only contain letters")
                else -> ValidationState(true, "")
            }
        }

        fun validateLastname(name: String): ValidationState {
            return when {
                name.isBlank() -> ValidationState(false, "Last name is required")
                name.length < 2 -> ValidationState(false, "Last name must be at least 2 characters")
                !name.matches(Regex("^[a-zA-Z\\s]+$")) -> ValidationState(false, "Last name can only contain letters")
                else -> ValidationState(true, "")
            }
        }

        fun validateEmail(email: String): ValidationState {
            return when {
                email.isBlank() -> ValidationState(false, "Email is required")
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                    ValidationState(false, "Please enter a valid email address")
                else -> ValidationState(true, "")
            }
        }

        fun validatePassword(password: String): ValidationState {
            return when {
                password.isBlank() -> ValidationState(false, "Password is required")
                password.length < 6 -> ValidationState(false, "Password must be at least 6 characters")
                !password.matches(Regex(".*[A-Z].*")) -> ValidationState(false, "Password must contain at least one uppercase letter")
                !password.matches(Regex(".*[a-z].*")) -> ValidationState(false, "Password must contain at least one lowercase letter")
                !password.matches(Regex(".*\\d.*")) -> ValidationState(false, "Password must contain at least one number")
                else -> ValidationState(true, "")
            }
        }

        fun validateConfirmPassword(password: String, confirmPassword: String): ValidationState {
            return when {
                confirmPassword.isBlank() -> ValidationState(false, "Please confirm your password")
                password != confirmPassword -> ValidationState(false, "Passwords do not match")
                else -> ValidationState(true, "")
            }
        }

        fun isFormValid(): Boolean {
            return firstnameValidation.isValid &&
                    lastnameValidation.isValid &&
                    emailValidation.isValid &&
                    passwordValidation.isValid &&
                    confirmPasswordValidation.isValid
        }

        Text(
            text = "Register your account",
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(20.dp))

        // First Name Field
        OutlinedTextField(
            value = firstname,
            onValueChange = {
                firstname = it
                firstnameValidation = validateFirstname(it)
            },
            label = { Text("First name") },
            singleLine = true,
            isError = !firstnameValidation.isValid && firstname.isNotEmpty(),
            supportingText = {
                if (!firstnameValidation.isValid && firstname.isNotEmpty()) {
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
            value = lastname,
            onValueChange = {
                lastname = it
                lastnameValidation = validateLastname(it)
            },
            label = { Text("Last name") },
            singleLine = true,
            isError = !lastnameValidation.isValid && lastname.isNotEmpty(),
            supportingText = {
                if (!lastnameValidation.isValid && lastname.isNotEmpty()) {
                    Text(
                        text = lastnameValidation.errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailValidation = validateEmail(it)
            },
            label = { Text("Email") },
            singleLine = true,
            isError = !emailValidation.isValid && email.isNotEmpty(),
            supportingText = {
                if (!emailValidation.isValid && email.isNotEmpty()) {
                    Text(
                        text = emailValidation.errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordValidation = validatePassword(it)
                // Re-validate confirm password when password changes
                if (confirmPassword.isNotEmpty()) {
                    confirmPasswordValidation = validateConfirmPassword(it, confirmPassword)
                }
            },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            isError = !passwordValidation.isValid && password.isNotEmpty(),
            supportingText = {
                if (!passwordValidation.isValid && password.isNotEmpty()) {
                    Text(
                        text = passwordValidation.errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Password Field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                confirmPasswordValidation = validateConfirmPassword(password, it)
            },
            label = { Text("Confirm Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            isError = !confirmPasswordValidation.isValid && confirmPassword.isNotEmpty(),
            supportingText = {
                if (!confirmPasswordValidation.isValid && confirmPassword.isNotEmpty()) {
                    Text(
                        text = confirmPasswordValidation.errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(25.dp))

        Button(
            onClick = {
                // Validate all fields before submission
                firstnameValidation = validateFirstname(firstname)
                lastnameValidation = validateLastname(lastname)
                emailValidation = validateEmail(email)
                passwordValidation = validatePassword(password)
                confirmPasswordValidation = validateConfirmPassword(password, confirmPassword)

                if (isFormValid()) {
                    isLoading = true
                    authViewModel.signup(firstname, lastname, email, password) { success, errorMessage ->
                        if (success) {
                            isLoading = false
                            navController.navigate("home") {
                                popUpTo("auth") { inclusive = true }
                            }
                        } else {
                            isLoading = false
                            AppUtil.showToast(context, errorMessage ?: "Something went wrong")
                        }
                    }
                } else {
                    AppUtil.showToast(context, "Please fix the errors above")
                }
            },
            enabled = !isLoading && isFormValid(),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp)
        ) {
            Text(if (isLoading) "Creating account..." else "Sign up")
        }
    }
}