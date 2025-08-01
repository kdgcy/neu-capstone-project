package com.fak.classmate.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

data class DrawerItem(
    val title: String,
    val icon: ImageVector,
    val route: String? = null,
    val onClick: (() -> Unit)? = null
)

@Composable
fun DrawerContent(
    navController: NavController,
    onCloseDrawer: () -> Unit
) {
    // State for user data
    var userDisplayName by remember { mutableStateOf("ClassMate") }
    val scope = rememberCoroutineScope()

    // Fetch user data from Firestore
    LaunchedEffect(Unit) {
        val currentUser = Firebase.auth.currentUser
        currentUser?.let { user ->
            val firestore = Firebase.firestore
            firestore.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val firstname = document.getString("firstname") ?: ""
                        val lastname = document.getString("lastname") ?: ""

                        // Format: "Firstname L." (first letter of lastname with period)
                        userDisplayName = if (firstname.isNotEmpty() && lastname.isNotEmpty()) {
                            "$firstname ${lastname.first().uppercase()}."
                        } else if (firstname.isNotEmpty()) {
                            firstname
                        } else {
                            "ClassMate"
                        }
                    }
                }
                .addOnFailureListener {
                    userDisplayName = "ClassMate" // Fallback if fetch fails
                }
        }
    }

    // Main drawer items (without logout)
    val mainDrawerItems = listOf(
        DrawerItem("Dashboard", Icons.Default.Home),
        DrawerItem("Calendar", Icons.Default.DateRange),
        DrawerItem("Tasks", Icons.Default.Info)
    )

    // Logout item (separate)
    val logoutItem = DrawerItem("Logout", Icons.Default.ExitToApp, onClick = {
        Firebase.auth.signOut()
        navController.navigate("auth") {
            popUpTo("home") { inclusive = true }
        }
    })

    ModalDrawerSheet {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Drawer Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = userDisplayName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = Firebase.auth.currentUser?.email ?: "User",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            Spacer(modifier = Modifier.height(8.dp))

            // Main Navigation Items
            mainDrawerItems.forEach { item ->
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title
                        )
                    },
                    label = { Text(item.title) },
                    selected = false,
                    onClick = {
                        item.onClick?.invoke()
                        scope.launch {
                            onCloseDrawer()
                        }
                        // Handle navigation for other items
                        item.route?.let { route ->
                            // navController.navigate(route)
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }

            // Spacer to push logout to bottom
            Spacer(modifier = Modifier.weight(1f))

            // Bottom section with logout
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            NavigationDrawerItem(
                icon = {
                    Icon(
                        imageVector = logoutItem.icon,
                        contentDescription = logoutItem.title,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                label = {
                    Text(
                        text = logoutItem.title,
                        color = MaterialTheme.colorScheme.error
                    )
                },
                selected = false,
                onClick = {
                    logoutItem.onClick?.invoke()
                    scope.launch {
                        onCloseDrawer()
                    }
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedIconColor = MaterialTheme.colorScheme.error,
                    unselectedTextColor = MaterialTheme.colorScheme.error
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}