package com.fak.classmate.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import com.fak.classmate.viewmodel.AuthViewModel

@Composable
fun SignUp(modifier: Modifier = Modifier,navController: NavController,authViewModel: AuthViewModel = viewModel()) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        var firstname by remember{ mutableStateOf("") }
        var lastname by remember{ mutableStateOf("") }
        var email by remember{ mutableStateOf("") }
        var password by remember{ mutableStateOf("") }
        val context = LocalContext.current
        var isLoading by remember { mutableStateOf(false) }

        Text(
            text = "Register your account",
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold
        )



        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = firstname,
            onValueChange = { firstname = it },
            label = { Text("First name") },
            singleLine = true
        )



        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = lastname,
            onValueChange = { lastname = it },
            label = { Text("Last name") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(25.dp))

        Button(onClick = {
            isLoading = true
            authViewModel.signup(firstname, lastname, email, password){success,errorMessage->
                if(success){
                    isLoading = false
                    navController.navigate("home"){
                        popUpTo("auth"){inclusive=true}
                    }
                }else{
                    isLoading = false
                    AppUtil.showToast(context,errorMessage?:"Something went wrong")
                }
            }
        },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(0.6f).height(50.dp)) {
            Text(if(isLoading) "Creating account" else "Sign up")
        }

    }
}