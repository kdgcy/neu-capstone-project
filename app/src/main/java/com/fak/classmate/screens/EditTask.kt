package com.fak.classmate.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fak.classmate.AppUtil
import com.fak.classmate.model.TaskCategory
import com.fak.classmate.model.TaskPriority
import com.fak.classmate.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTask(
    modifier: Modifier = Modifier,
    navController: NavController,
    taskId: String,
    taskViewModel: TaskViewModel = viewModel()
) {
    val tasks = taskViewModel.tasks.collectAsState().value
    val task = tasks.find { it.id == taskId }

    // Form state
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var selectedCategory by remember { mutableStateOf(TaskCategory.ASSIGNMENT) }
    var selectedDate by remember { mutableStateOf(Date()) }
    var isSaving by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    // Dropdown states
    var isPriorityExpanded by remember { mutableStateOf(false) }
    var isCategoryExpanded by remember { mutableStateOf(false) }
    var isDatePickerVisible by remember { mutableStateOf(false) }

    // Validation
    var titleError by remember { mutableStateOf("") }
    var descriptionError by remember { mutableStateOf("") }

    val context = LocalContext.current
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    // Load task data
    LaunchedEffect(task) {
        task?.let {
            title = it.title
            description = it.description
            selectedPriority = it.priority
            selectedCategory = it.category
            selectedDate = it.dueDate.toDate()
            isLoading = false
        }
    }

    // Validation function
    fun validateForm(): Boolean {
        var isValid = true

        titleError = when {
            title.isBlank() -> {
                isValid = false
                "Task title is required"
            }
            title.length < 3 -> {
                isValid = false
                "Title must be at least 3 characters"
            }
            title.length > 100 -> {
                isValid = false
                "Title must be less than 100 characters"
            }
            else -> ""
        }

        descriptionError = when {
            description.length > 500 -> {
                isValid = false
                "Description must be less than 500 characters"
            }
            else -> ""
        }

        return isValid
    }

    if (task == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Edit Task",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() },
                        enabled = !isSaving
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Task Information Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Task Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = title,
                            onValueChange = {
                                if (it.length <= 100) {
                                    title = it
                                    if (titleError.isNotEmpty()) titleError = ""
                                }
                            },
                            label = { Text("Task Title *") },
                            placeholder = { Text("Enter task title...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            isError = titleError.isNotEmpty(),
                            supportingText = {
                                if (titleError.isNotEmpty()) {
                                    Text(titleError, color = MaterialTheme.colorScheme.error)
                                } else {
                                    Text("${title.length}/100 characters")
                                }
                            },
                            enabled = !isSaving
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = {
                                if (it.length <= 500) {
                                    description = it
                                    if (descriptionError.isNotEmpty()) descriptionError = ""
                                }
                            },
                            label = { Text("Description (Optional)") },
                            placeholder = { Text("Add task details...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5,
                            isError = descriptionError.isNotEmpty(),
                            supportingText = {
                                if (descriptionError.isNotEmpty()) {
                                    Text(descriptionError, color = MaterialTheme.colorScheme.error)
                                } else {
                                    Text("${description.length}/500 characters")
                                }
                            },
                            enabled = !isSaving
                        )
                    }
                }

                // Task Details Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Task Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = dateFormatter.format(selectedDate),
                            onValueChange = { },
                            label = { Text("Due Date *") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(
                                    onClick = { isDatePickerVisible = true },
                                    enabled = !isSaving
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = "Select Date"
                                    )
                                }
                            },
                            enabled = !isSaving
                        )

                        ExposedDropdownMenuBox(
                            expanded = isPriorityExpanded && !isSaving,
                            onExpandedChange = { if (!isSaving) isPriorityExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedPriority.displayName,
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Priority *") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = isPriorityExpanded
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                enabled = !isSaving
                            )

                            ExposedDropdownMenu(
                                expanded = isPriorityExpanded,
                                onDismissRequest = { isPriorityExpanded = false }
                            ) {
                                TaskPriority.values().forEach { priority ->
                                    DropdownMenuItem(
                                        text = { Text(priority.displayName) },
                                        onClick = {
                                            selectedPriority = priority
                                            isPriorityExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = isCategoryExpanded && !isSaving,
                            onExpandedChange = { if (!isSaving) isCategoryExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = "${selectedCategory.icon} ${selectedCategory.displayName}",
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Category *") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = isCategoryExpanded
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                enabled = !isSaving
                            )

                            ExposedDropdownMenu(
                                expanded = isCategoryExpanded,
                                onDismissRequest = { isCategoryExpanded = false }
                            ) {
                                TaskCategory.values().forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text("${category.icon} ${category.displayName}") },
                                        onClick = {
                                            selectedCategory = category
                                            isCategoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { navController.navigateUp() },
                        modifier = Modifier.weight(1f),
                        enabled = !isSaving
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (validateForm()) {
                                isSaving = true
                                taskViewModel.updateTask(
                                    taskId = taskId,
                                    title = title.trim(),
                                    description = description.trim(),
                                    dueDate = selectedDate,
                                    priority = selectedPriority,
                                    category = selectedCategory
                                ) { success, errorMessage ->
                                    isSaving = false
                                    if (success) {
                                        AppUtil.showToast(context, "✅ Task updated successfully!")
                                        navController.navigateUp()
                                    } else {
                                        AppUtil.showToast(
                                            context,
                                            errorMessage ?: "❌ Failed to update task"
                                        )
                                    }
                                }
                            } else {
                                AppUtil.showToast(context, "Please fix the errors above")
                            }
                        },
                        enabled = !isSaving && title.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Save Changes")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Date Picker Dialog
    if (isDatePickerVisible) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.time
        )

        DatePickerDialog(
            onDismissRequest = { isDatePickerVisible = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val calendar = Calendar.getInstance().apply {
                                timeInMillis = millis
                                set(Calendar.HOUR_OF_DAY, 23)
                                set(Calendar.MINUTE, 59)
                                set(Calendar.SECOND, 59)
                            }
                            selectedDate = calendar.time
                        }
                        isDatePickerVisible = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { isDatePickerVisible = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}