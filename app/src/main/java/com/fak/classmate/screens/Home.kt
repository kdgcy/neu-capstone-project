package com.fak.classmate.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fak.classmate.components.DrawerContent
import com.fak.classmate.model.TaskCategory
import com.fak.classmate.model.TaskModel
import com.fak.classmate.model.TaskPriority
import com.fak.classmate.viewmodel.TaskViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

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
    val taskStats = taskViewModel.getTaskStats()

    // Filter and sort states
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var sortBy by remember { mutableStateOf("Date") }
    var showSortMenu by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var taskToDelete by remember { mutableStateOf<TaskModel?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Refresh tasks
    LaunchedEffect(Unit) {
        taskViewModel.loadTasks()
    }

    // Show error snackbar
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            taskViewModel.clearError()
        }
    }

    // Filter and sort tasks
    val filteredTasks = tasks
        .filter { task ->
            // Search filter
            val matchesSearch = task.title.contains(searchQuery, ignoreCase = true) ||
                    task.description.contains(searchQuery, ignoreCase = true)

            // Status filter
            val matchesFilter = when (selectedFilter) {
                "Pending" -> !task.isCompleted
                "Completed" -> task.isCompleted
                "Overdue" -> !task.isCompleted && task.dueDate.toDate().before(Date())
                else -> true // "All"
            }

            matchesSearch && matchesFilter
        }
        .let { filteredList ->
            // Sort
            when (sortBy) {
                "Priority" -> filteredList.sortedByDescending { it.priority.ordinal }
                "Category" -> filteredList.sortedBy { it.category.displayName }
                else -> filteredList.sortedBy { it.dueDate.toDate() } // "Date"
            }
        }

    // Categorize tasks
    val overdueTasks = filteredTasks.filter { !it.isCompleted && it.dueDate.toDate().before(Date()) }
    val todayTasks = taskViewModel.getTasksDueToday().filter { filteredTasks.contains(it) }
    val upcomingTasks = filteredTasks.filter {
        !it.isCompleted &&
                it.dueDate.toDate().after(Date()) &&
                !todayTasks.contains(it)
    }
    val completedTasks = filteredTasks.filter { it.isCompleted }

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
                    actions = {
                        IconButton(onClick = {
                            navController.navigate("profile")
                        }) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile",
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
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Search Bar
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search tasks...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            autoCorrect = false,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Search
                        )
                    )
                }

                // Stats Dashboard with Progress Circle
                item {
                    StatsSection(taskStats)
                }

                // Filter Chips
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LazyRow(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(listOf("All", "Pending", "Completed", "Overdue")) { filter ->
                                FilterChip(
                                    selected = selectedFilter == filter,
                                    onClick = { selectedFilter = filter },
                                    label = {
                                        Text(
                                            filter,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                )
                            }
                        }

                        // Sort Dropdown
                        Box {
                            OutlinedTextField(
                                value = sortBy,
                                onValueChange = { },
                                readOnly = true,
                                modifier = Modifier.width(120.dp),
                                textStyle = MaterialTheme.typography.bodySmall,
                                trailingIcon = {
                                    IconButton(onClick = { showSortMenu = true }) {
                                        Icon(
                                            Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Sort",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                },
                                singleLine = true
                            )

                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                listOf("Date", "Priority", "Category").forEach { sort ->
                                    DropdownMenuItem(
                                        text = { Text(sort) },
                                        onClick = {
                                            sortBy = sort
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Loading
                if (isLoading && tasks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                // Overdue Tasks Section
                if (overdueTasks.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "⚠️ Overdue",
                            count = overdueTasks.size,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    items(overdueTasks, key = { it.id }) { task ->
                        SwipeableTaskCard(
                            task = task,
                            onTaskClick = { navController.navigate("taskDetail/${task.id}") },
                            onToggleComplete = {
                                taskViewModel.toggleTaskCompletion(task.id) { _, _ -> }
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
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(todayTasks, key = { it.id }) { task ->
                        SwipeableTaskCard(
                            task = task,
                            onTaskClick = { navController.navigate("taskDetail/${task.id}") },
                            onToggleComplete = {
                                taskViewModel.toggleTaskCompletion(task.id) { _, _ -> }
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
                            title = "📆 Upcoming",
                            count = upcomingTasks.size,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    items(upcomingTasks, key = { it.id }) { task ->
                        SwipeableTaskCard(
                            task = task,
                            onTaskClick = { navController.navigate("taskDetail/${task.id}") },
                            onToggleComplete = {
                                taskViewModel.toggleTaskCompletion(task.id) { _, _ -> }
                            },
                            onDelete = {
                                taskToDelete = task
                                showDeleteDialog = true
                            }
                        )
                    }
                }

                // Completed Tasks Section (Collapsible)
                if (completedTasks.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "✅ Completed",
                            count = completedTasks.size,
                            color = Color(0xFF4CAF50)
                        )
                    }
                    items(completedTasks.take(3), key = { it.id }) { task ->
                        SwipeableTaskCard(
                            task = task,
                            onTaskClick = { navController.navigate("taskDetail/${task.id}") },
                            onToggleComplete = {
                                taskViewModel.toggleTaskCompletion(task.id) { _, _ -> }
                            },
                            onDelete = {
                                taskToDelete = task
                                showDeleteDialog = true
                            }
                        )
                    }
                }

                // Empty State
                if (filteredTasks.isEmpty() && !isLoading) {
                    item {
                        EmptyState(
                            message = if (searchQuery.isNotEmpty()) {
                                "No tasks found matching \"$searchQuery\""
                            } else if (selectedFilter != "All") {
                                "No $selectedFilter tasks"
                            } else {
                                "No tasks yet. Create your first task!"
                            }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && taskToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Task?") },
            text = {
                Text("Are you sure you want to delete \"${taskToDelete?.title}\"? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        taskToDelete?.let { task ->
                            taskViewModel.deleteTask(task.id) { success, _ ->
                                if (success) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("✅ Task deleted")
                                    }
                                }
                            }
                        }
                        showDeleteDialog = false
                        taskToDelete = null
                    }
                ) {
                    Text("Delete")
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
fun StatsSection(stats: com.fak.classmate.viewmodel.TaskStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Progress Circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(100.dp)
            ) {
                val progress = if (stats.total > 0) {
                    stats.completed.toFloat() / stats.total.toFloat()
                } else 0f

                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(100.dp),
                    strokeWidth = 8.dp,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(progress * 100).roundToInt()}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Complete",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            // Stats Grid
            Column(
                modifier = Modifier.weight(1f).padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatRow("Total", stats.total.toString(), MaterialTheme.colorScheme.onSurface)
                StatRow("Pending", stats.pending.toString(), MaterialTheme.colorScheme.tertiary)
                StatRow("Completed", stats.completed.toString(), Color(0xFF4CAF50))
                if (stats.overdue > 0) {
                    StatRow("Overdue", stats.overdue.toString(), MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
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