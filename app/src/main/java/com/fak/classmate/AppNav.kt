package com.fak.classmate

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fak.classmate.screens.AuthScreen
import com.fak.classmate.screens.Login
import com.fak.classmate.screens.Route
import com.fak.classmate.screens.SignUp

@Composable
fun AppNav(){
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Route.AUTH
    ) {
        composable(Route.AUTH){
            AuthScreen(navController)
        }
        composable(Route.SIGNUP){
            SignUp(navController)
        }
        composable(Route.LOGIN){
            Login(navController)
        }
    }
}