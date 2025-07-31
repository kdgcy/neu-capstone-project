package com.fak.classmate

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fak.classmate.screens.Auth
import com.fak.classmate.screens.Home
import com.fak.classmate.screens.Login
import com.fak.classmate.screens.SignUp

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "auth") {
        composable("auth"){ Auth(modifier,navController) }
        composable("home"){ Home(modifier,navController) }
        composable("login"){ Login(modifier,navController) }
        composable("signup"){ SignUp(modifier,navController) }
    }
}