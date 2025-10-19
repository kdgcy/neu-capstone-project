package com.fak.classmate

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fak.classmate.screens.About
import com.fak.classmate.screens.AddTask
import com.fak.classmate.screens.Auth
import com.fak.classmate.screens.Calendar
import com.fak.classmate.screens.EditTask
import com.fak.classmate.screens.Home
import com.fak.classmate.screens.Login
import com.fak.classmate.screens.Profile
import com.fak.classmate.screens.SignUp
import com.fak.classmate.screens.Splash
import com.fak.classmate.screens.TaskDetail
import com.fak.classmate.viewmodel.TaskViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val isLoggedIn = Firebase.auth.currentUser != null
    val firstPage = if (isLoggedIn) "home" else "auth"

    val taskViewModel: TaskViewModel = viewModel()

    NavHost(navController = navController, startDestination = "splash") {
        composable("auth") { Auth(modifier, navController) }
        composable("home") { Home(modifier, navController, taskViewModel) }
        composable("login") { Login(modifier, navController) }
        composable("signup") { SignUp(modifier, navController) }
        composable("profile") { Profile(modifier, navController) }
        composable("splash") { Splash(modifier, navController) }
        composable("addTask") { AddTask(modifier, navController, taskViewModel) }
        composable("about") { About(modifier, navController) }
        composable("calendar") { Calendar(modifier, navController, taskViewModel) }
        composable("taskDetail/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            TaskDetail(modifier, navController, taskId, taskViewModel)
        }
        composable("editTask/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            EditTask(modifier, navController, taskId, taskViewModel)
        }
    }
}