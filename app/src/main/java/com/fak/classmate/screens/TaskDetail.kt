package com.fak.classmate.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fak.classmate.AppUtil
import com.fak.classmate.model.SubTaskModel
import com.fak.classmate.model.TaskPriority
import com.fak.classmate.model.getCompletionProgress
import com.fak.classmate.model.getProgressPercentage
import com.fak.classmate.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetail(
    modifier: Modifier = Modifier,
    navController: NavController,
    taskId: String,
    taskViewModel: TaskViewModel = viewModel()
) {
    // Collect tasks and find the specific task
    val tasks = taskViewModel.tasks.collectAsState().value
    val task = tasks.find { it.id == taskId }
    val subtasks = taskViewModel.getSubtasksForTask(taskId)

    var isLoading by remember { mutableStateOf(false) }
    var showAddSubtask by remember { mutableStateOf(false) }
    var newSubtaskTitle by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Date formatters
    val fullDateFormatter = SimpleDateFormat("EEEE, MMM dd, yyyy 'at' h:mm a", Locale.getDefault())

    // Load tasks if not already loaded
    LaunchedEffect(Unit) {
        if (tasks.isEmpty()) {
            taskViewModel.loadTasks()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Task Details",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigateUp()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                actions = {
                    if (task != null) {
                        IconButton(onClick = {
                            navController.navigate("editTask/$taskId")
                        }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Task",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        IconButton(onClick = {
                            isLoading = true
                            taskViewModel.deleteTask(taskId) { success, errorMessage ->
                                isLoading = false
                                if (success) {
                                    AppUtil.showToast(context, "Task deleted successfully")
                                    navController.navigateUp()
                                } else {
                                    AppUtil.showToast(context, errorMessage ?: "Failed to delete task")
                                }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Task",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        if (task == null) {
            // Task not found or still loading
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Task not found",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(onClick = { navController.navigateUp() }) {
                    Text("Go Back")
                }
            }
        } else {
            // Show task details with subtasks
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Task Title Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (task.isCompleted) {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                }

                                // Priority flag
                                val priorityColor = when (task.priority) {
                                    TaskPriority.HIGH -> Color(0xFFE57373)
                                    TaskPriority.MEDIUM -> Color(0xFFFFB74D)
                                    TaskPriority.LOW -> Color(0xFF81C784)
                                }

                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "${task.priority.displayName} Priority",
                                    tint = priorityColor,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Status
                            Text(
                                text = if (task.isCompleted) "✅ Completed" else "⏳ Pending",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = if (task.isCompleted) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                }
                            )
                        }
                    }
                }

                // Subtasks Progress Card
                if (subtasks.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                val (completed, total) = subtasks.getCompletionProgress()
                                val progress = subtasks.getProgressPercentage() / 100f

                                Text(
                                    text = "Progress",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "$completed of $total subtasks completed",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                )
                            }
                        }
                    }
                }

                // Subtasks Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Subtasks",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )

                                IconButton(onClick = { showAddSubtask = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Subtask",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            // Add Subtask Input
                            if (showAddSubtask) {
                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = newSubtaskTitle,
                                    onValueChange = { newSubtaskTitle = it },
                                    label = { Text("New subtask") },
                                    placeholder = { Text("Enter subtask title...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = {
                                        showAddSubtask = false
                                        newSubtaskTitle = ""
                                    }) {
                                        Text("Cancel")
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = {
                                            if (newSubtaskTitle.isNotBlank()) {
                                                taskViewModel.createSubtask(taskId, newSubtaskTitle) { success, errorMessage ->
                                                    if (success) {
                                                        showAddSubtask = false
                                                        newSubtaskTitle = ""
                                                        AppUtil.showToast(context, "Subtask added!")
                                                    } else {
                                                        AppUtil.showToast(context, errorMessage ?: "Failed to add subtask")
                                                    }
                                                }
                                            }
                                        },
                                        enabled = newSubtaskTitle.isNotBlank()
                                    ) {
                                        Text("Add")
                                    }
                                }
                            }
                        }
                    }
                }

                // Subtasks List
                items(subtasks) { subtask ->
                    SubtaskItem(
                        subtask = subtask,
                        onToggleComplete = {
                            taskViewModel.toggleSubtaskCompletion(taskId, subtask.id) { success, errorMessage ->
                                if (!success) {
                                    AppUtil.showToast(context, errorMessage ?: "Failed to update subtask")
                                }
                            }
                        },
                        onDelete = {
                            taskViewModel.deleteSubtask(taskId, subtask.id) { success, errorMessage ->
                                if (success) {
                                    AppUtil.showToast(context, "Subtask deleted")
                                } else {
                                    AppUtil.showToast(context, errorMessage ?: "Failed to delete subtask")
                                }
                            }
                        }
                    )
                }

                // Task Information Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Task Information",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // Description
                            if (task.description.isNotBlank()) {
                                Text(
                                    text = "Description",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = task.description,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                            }

                            // Due Date
                            Text(
                                text = "Due Date",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = fullDateFormatter.format(task.dueDate.toDate()),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // Priority
                            Text(
                                text = "Priority",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = task.priority.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // Category
                            Text(
                                text = "Category",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "${task.category.icon} ${task.category.displayName}",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // Created Date
                            Text(
                                text = "Created",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = fullDateFormatter.format(task.createdAt.toDate()),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Action Button - ✅ FIXED HERE!
                item {
                    Button(
                        onClick = {
                            isLoading = true
                            Log.d("TaskDetail", "BEFORE toggle - task completed: ${task.isCompleted}")
                            taskViewModel.toggleTaskCompletion(taskId) { success, errorMessage ->
                                isLoading = false
                                Log.d("TaskDetail", "Toggle result: success=$success")
                                if (success) {
                                    val message = if (!task.isCompleted) "Task completed!" else "Task marked as pending"
                                    AppUtil.showToast(context, message)
                                    taskViewModel.loadTasks()
                                    Log.d("TaskDetail", "Called loadTasks()")
                                } else {
                                    AppUtil.showToast(context, errorMessage ?: "Failed to update task")
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (task.isCompleted) "Mark as Pending" else "Mark as Completed"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SubtaskItem(
    subtask: SubTaskModel,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (subtask.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Completion toggle
            IconButton(onClick = onToggleComplete) {
                Icon(
                    imageVector = if (subtask.isCompleted) {
                        Icons.Default.CheckCircle
                    } else {
                        Icons.Default.CheckCircle
                    },
                    contentDescription = if (subtask.isCompleted) "Mark as incomplete" else "Mark as complete",
                    tint = if (subtask.isCompleted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    }
                )
            }

            // Subtask title
            Text(
                text = subtask.title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                textDecoration = if (subtask.isCompleted) {
                    TextDecoration.LineThrough
                } else {
                    TextDecoration.None
                },
                color = if (subtask.isCompleted) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )

            // Delete button
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete subtask",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}