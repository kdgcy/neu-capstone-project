package com.fak.classmate.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun Auth(modifier: Modifier = Modifier,navController: NavController) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to Auth Screen"
        )

        Spacer(modifier = modifier.height(25.dp))

        OutlinedButton(onClick = {
            navController.navigate("login")
        },
            modifier = Modifier.fillMaxWidth(0.6f).height(50.dp)){
            Text("Login")
        }

        Spacer(modifier = Modifier.height(15.dp))

        Button(onClick = {
            navController.navigate("signup")
        },
            modifier = Modifier.fillMaxWidth(0.6f).height(50.dp)){
            Text("Sign up")
        }
    }
}