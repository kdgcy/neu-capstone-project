package com.fak.classmate.screens

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fak.classmate.components.DrawerContent
import com.fak.classmate.model.TaskModel
import com.fak.classmate.model.TaskPriority
import com.fak.classmate.viewmodel.TaskViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(
    modifier: Modifier = Modifier,
    navController: NavController,
    taskViewModel: TaskViewModel = viewModel()
) {
    val tasks = taskViewModel.tasks.collectAsState().value
    val isLoading = taskViewModel.isLoading.collectAsState().value
    val error = taskViewModel.error.collectAsState().value

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var taskToDelete by remember { mutableStateOf<TaskModel?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Reload tasks when screen becomes visible
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner.lifecycle) {
        lifecycleOwner.lifecycle.currentStateFlow.collect { state ->
            if (state == Lifecycle.State.RESUMED) {
                taskViewModel.loadTasks()
            }
        }
    }

    // Show error snackbar
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            taskViewModel.clearError()
        }
    }

    // Categorize tasks by status and date
    val overdueTasks = tasks.filter { !it.isCompleted && it.dueDate.toDate().before(Date()) }
    val todayTasks = taskViewModel.getTasksDueToday().filter { !it.isCompleted }
    val upcomingTasks = tasks.filter {
        !it.isCompleted &&
                it.dueDate.toDate().after(Date()) &&
                !todayTasks.contains(it)
    }
    val completedTasks = tasks.filter { it.isCompleted }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                navController = navController,
                onCloseDrawer = {
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "ClassMate",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate("addTask") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Task",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        ) { innerPadding ->
            if (isLoading && tasks.isEmpty()) {
                // Show loading indicator only on first load
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    // Welcome message with task summary
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = "Hello! 👋",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = buildString {
                                        append("You have ")
                                        append("${tasks.count { !it.isCompleted }} pending task")
                                        if (tasks.count { !it.isCompleted } != 1) append("s")
                                        if (overdueTasks.isNotEmpty()) {
                                            append(" • ${overdueTasks.size} overdue")
                                        }
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    // Overdue Tasks Section
                    if (overdueTasks.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "⚠️ Overdue",
                                count = overdueTasks.size,
                                color = Color(0xFFE57373)
                            )
                        }

                        items(
                            items = overdueTasks,
                            key = { task -> task.id }
                        ) { task ->
                            SwipeableTaskCard(
                                task = task,
                                onTaskClick = { navController.navigate("taskDetail/${task.id}") },
                                onToggleComplete = {
                                    taskViewModel.toggleTaskCompletion(task.id) { success, error ->
                                        if (!success) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    error ?: "Failed to update task"
                                                )
                                            }
                                        }
                                    }
                                },
                                onDelete = {
                                    taskToDelete = task
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }

                    // Today's Tasks Section
                    if (todayTasks.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "📅 Due Today",
                                count = todayTasks.size,
                                color = Color(0xFFFFB74D)
                            )
                        }

                        items(
                            items = todayTasks,
                            key = { task -> task.id }
                        ) { task ->
                            SwipeableTaskCard(
                                task = task,
                                onTaskClick = { navController.navigate("taskDetail/${task.id}") },
                                onToggleComplete = {
                                    taskViewModel.toggleTaskCompletion(task.id) { success, error ->
                                        if (!success) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    error ?: "Failed to update task"
                                                )
                                            }
                                        }
                                    }
                                },
                                onDelete = {
                                    taskToDelete = task
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }

                    // Upcoming Tasks Section
                    if (upcomingTasks.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "📋 Upcoming",
                                count = upcomingTasks.size,
                                color = Color(0xFF64B5F6)
                            )
                        }

                        items(
                            items = upcomingTasks,
                            key = { task -> task.id }
                        ) { task ->
                            SwipeableTaskCard(
                                task = task,
                                onTaskClick = { navController.navigate("taskDetail/${task.id}") },
                                onToggleComplete = {
                                    taskViewModel.toggleTaskCompletion(task.id) { success, error ->
                                        if (!success) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    error ?: "Failed to update task"
                                                )
                                            }
                                        }
                                    }
                                },
                                onDelete = {
                                    taskToDelete = task
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }

                    // Completed Tasks Section
                    if (completedTasks.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "✅ Completed",
                                count = completedTasks.size,
                                color = Color(0xFF81C784)
                            )
                        }

                        items(
                            items = completedTasks,
                            key = { task -> task.id }
                        ) { task ->
                            SwipeableTaskCard(
                                task = task,
                                onTaskClick = { navController.navigate("taskDetail/${task.id}") },
                                onToggleComplete = {
                                    taskViewModel.toggleTaskCompletion(task.id) { success, error ->
                                        if (!success) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    error ?: "Failed to update task"
                                                )
                                            }
                                        }
                                    }
                                },
                                onDelete = {
                                    taskToDelete = task
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }

                    // Empty state
                    if (tasks.isEmpty()) {
                        item {
                            EmptyState(message = "No tasks yet! Tap + to create your first task 🎯")
                        }
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && taskToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Delete Task?") },
            text = {
                Text("Are you sure you want to delete \"${taskToDelete?.title}\"? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        taskToDelete?.let { task ->
                            taskViewModel.deleteTask(task.id) { success, errorMessage ->
                                if (success) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Task deleted")
                                    }
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            errorMessage ?: "Failed to delete task"
                                        )
                                    }
                                }
                            }
                        }
                        showDeleteDialog = false
                        taskToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    taskToDelete = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SectionHeader(title: String, count: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Box(
            modifier = Modifier
                .background(color.copy(alpha = 0.2f), CircleShape)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableTaskCard(
    task: TaskModel,
    onTaskClick: () -> Unit,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val swipeState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onToggleComplete()
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    false
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = swipeState,
        backgroundContent = {
            val color by animateColorAsState(
                when (swipeState.targetValue) {
                    SwipeToDismissBoxValue.Settled -> MaterialTheme.colorScheme.surface
                    SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50)
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                },
                label = "background color"
            )
            val scale by animateFloatAsState(
                if (swipeState.targetValue == SwipeToDismissBoxValue.Settled) 0.75f else 1f,
                label = "icon scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, shape = RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = when (swipeState.dismissDirection) {
                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                    else -> Alignment.Center
                }
            ) {
                Icon(
                    imageVector = when (swipeState.dismissDirection) {
                        SwipeToDismissBoxValue.StartToEnd -> Icons.Default.CheckCircle
                        SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                        else -> Icons.Default.CheckCircle
                    },
                    contentDescription = null,
                    modifier = Modifier.scale(scale),
                    tint = Color.White
                )
            }
        },
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true
    ) {
        TaskCard(task = task, onTaskClick = onTaskClick, modifier = modifier)
    }
}

@Composable
fun TaskCard(
    task: TaskModel,
    onTaskClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val priorityColor = when (task.priority) {
        TaskPriority.HIGH -> Color(0xFFE57373)
        TaskPriority.MEDIUM -> Color(0xFFFFB74D)
        TaskPriority.LOW -> Color(0xFF81C784)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onTaskClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = if (task.isCompleted) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${task.category.icon}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = dateFormatter.format(task.dueDate.toDate()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "${task.priority.displayName} Priority",
                tint = priorityColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}