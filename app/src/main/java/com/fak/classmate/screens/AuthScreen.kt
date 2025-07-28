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

@Composable
fun AuthScreen(){
    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedButton(onClick = {
            //implement navigation here later...
        },
            modifier = Modifier.fillMaxWidth(0.6F)) { Text("Login") }

        Spacer(modifier = Modifier.height(15.dp))

        Button(onClick = {
            //implement navigation here later...
        },
            modifier = Modifier.fillMaxWidth(0.6F)) { Text("Sign up") }
    }
}