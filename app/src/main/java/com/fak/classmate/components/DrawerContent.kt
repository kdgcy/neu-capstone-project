package com.fak.classmate.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Star
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

data class DrawerMenuItem(
    val title: String,
    val icon: ImageVector,
    val route: String? = null,
    val badge: String? = null,
    val onClick: (() -> Unit)? = null
)

@Composable
fun DrawerContent(
    navController: NavController,
    onCloseDrawer: () -> Unit
) {
    // State for user data
    var userDisplayName by remember { mutableStateOf("ClassMate User") }
    var userEmail by remember { mutableStateOf("") }
    var firstname by remember { mutableStateOf("") }
    var lastname by remember { mutableStateOf("") }
    var completedToday by remember { mutableIntStateOf(0) }
    var currentStreak by remember { mutableIntStateOf(0) }

    val scope = rememberCoroutineScope()
    val currentUser = Firebase.auth.currentUser
    val firestore = Firebase.firestore

    // Fetch user data from Firestore
    LaunchedEffect(Unit) {
        currentUser?.let { user ->
            userEmail = user.email ?: ""

            firestore.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        firstname = document.getString("firstname") ?: ""
                        lastname = document.getString("lastname") ?: ""

                        userDisplayName = if (firstname.isNotEmpty() && lastname.isNotEmpty()) {
                            "$firstname $lastname"
                        } else if (firstname.isNotEmpty()) {
                            firstname
                        } else {
                            "ClassMate User"
                        }
                    }
                }

            // Get completed tasks today (you can enhance this)
            completedToday = 0 // TODO: Calculate from tasks
            currentStreak = 3 // TODO: Calculate actual streak
        }
    }

    // Main navigation items
    val navigationItems = listOf(
        DrawerMenuItem("Home", Icons.Default.Home, route = "home"),
        DrawerMenuItem("All Tasks", Icons.Default.List),
        DrawerMenuItem("Calendar", Icons.Default.DateRange),
        DrawerMenuItem("Statistics", Icons.Outlined.Star),
    )

    // Settings items
    val settingsItems = listOf(
        DrawerMenuItem("Settings", Icons.Default.Settings),
        DrawerMenuItem("About", Icons.Default.Info),
    )

    // Logout item
    val logoutItem = DrawerMenuItem("Logout", Icons.Default.ExitToApp, onClick = {
        Firebase.auth.signOut()
        navController.navigate("auth") {
            popUpTo("home") { inclusive = true }
        }
    })

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Beautiful Gradient Header
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
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Profile Section
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Profile Avatar
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        // User Info
                        Column {
                            Text(
                                text = userDisplayName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = userEmail,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Quick Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatBadge(
                            label = "Today",
                            value = completedToday.toString(),
                            icon = "✅"
                        )
                        StatBadge(
                            label = "Streak",
                            value = "$currentStreak days",
                            icon = "🔥"
                        )
                        StatBadge(
                            label = "Level",
                            value = "Pro",
                            icon = "⭐"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Navigation Items
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "NAVIGATION",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Bold
                )

                navigationItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                item.title,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        badge = item.badge?.let {
                            {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.error,
                                            CircleShape
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = it,
                                        color = MaterialTheme.colorScheme.onError,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        },
                        selected = false,
                        onClick = {
                            item.onClick?.invoke()
                            scope.launch {
                                onCloseDrawer()
                            }
                            item.route?.let { route ->
                                if (route == "home") {
                                    navController.navigate(route) {
                                        popUpTo(route) { inclusive = true }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedContainerColor = Color.Transparent
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Settings Section
                Text(
                    text = "PREFERENCES",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Bold
                )

                settingsItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                item.title,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        selected = false,
                        onClick = {
                            item.onClick?.invoke()
                            scope.launch {
                                onCloseDrawer()
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = Color.Transparent
                        )
                    )
                }
            }

            // Bottom section with logout
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            NavigationDrawerItem(
                icon = {
                    Icon(
                        imageVector = logoutItem.icon,
                        contentDescription = logoutItem.title,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = logoutItem.title,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
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
                    unselectedTextColor = MaterialTheme.colorScheme.error,
                    unselectedContainerColor = Color.Transparent
                )
            )

            // App Version
            Text(
                text = "ClassMate v1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 16.dp)
            )
        }
    }
}

@Composable
fun StatBadge(label: String, value: String, icon: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 24.sp
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
            fontSize = 11.sp
        )
    }
}