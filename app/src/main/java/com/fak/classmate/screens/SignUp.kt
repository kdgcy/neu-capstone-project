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
import androidx.compose.ui.graphics.Color
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
                firstnameValidation = authViewModel.validateFirstname(it)
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
                lastnameValidation = authViewModel.validateLastname(it)
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
                emailValidation = authViewModel.validateEmail(it)
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
                passwordValidation = authViewModel.validatePassword(it)
                // Re-validate confirm password when password changes
                if (confirmPassword.isNotEmpty()) {
                    confirmPasswordValidation = authViewModel.validateConfirmPassword(it, confirmPassword)
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
                confirmPasswordValidation = authViewModel.validateConfirmPassword(password, it)
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
                firstnameValidation = authViewModel.validateFirstname(firstname)
                lastnameValidation = authViewModel.validateLastname(lastname)
                emailValidation = authViewModel.validateEmail(email)
                passwordValidation = authViewModel.validatePassword(password)
                confirmPasswordValidation = authViewModel.validateConfirmPassword(password, confirmPassword)

                if (authViewModel.isFormValid(
                        firstnameValidation.isValid,
                        lastnameValidation.isValid,
                        emailValidation.isValid,
                        passwordValidation.isValid,
                        confirmPasswordValidation.isValid
                    )) {
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
            enabled = !isLoading && authViewModel.isFormValid(
                firstnameValidation.isValid,
                lastnameValidation.isValid,
                emailValidation.isValid,
                passwordValidation.isValid,
                confirmPasswordValidation.isValid
            ),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp)
        ) {
            Text(if (isLoading) "Creating account..." else "Sign up")
        }
    }
}