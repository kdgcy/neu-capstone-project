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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fak.classmate.AppUtil
import com.fak.classmate.model.TaskCategory
import com.fak.classmate.model.TaskPriority
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTask(modifier: Modifier = Modifier, navController: NavController) {
    // Form state
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var selectedCategory by remember { mutableStateOf(TaskCategory.ASSIGNMENT) }
    var selectedDate by remember { mutableStateOf(Date()) }
    var isSaving by remember { mutableStateOf(false) }

    // Dropdown states
    var isPriorityExpanded by remember { mutableStateOf(false) }
    var isCategoryExpanded by remember { mutableStateOf(false) }
    var isDatePickerVisible by remember { mutableStateOf(false) }

    // Validation
    var titleError by remember { mutableStateOf("") }

    val context = LocalContext.current
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    // Validation function
    fun validateForm(): Boolean {
        titleError = when {
            title.isBlank() -> "Task title is required"
            title.length < 3 -> "Title must be at least 3 characters"
            title.length > 100 -> "Title must be less than 100 characters"
            else -> ""
        }
        return titleError.isEmpty()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Add New Task",
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
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
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Task Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Title Field
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            if (titleError.isNotEmpty()) titleError = ""
                        },
                        label = { Text("Task Title") },
                        placeholder = { Text("Enter task title...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = titleError.isNotEmpty(),
                        supportingText = if (titleError.isNotEmpty()) {
                            { Text(titleError, color = MaterialTheme.colorScheme.error) }
                        } else null
                    )

                    // Description Field
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description (Optional)") },
                        placeholder = { Text("Add task details...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                }
            }

            // Task Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Task Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Due Date Field
                    OutlinedTextField(
                        value = dateFormatter.format(selectedDate),
                        onValueChange = { },
                        label = { Text("Due Date") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { isDatePickerVisible = true }) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Select Date"
                                )
                            }
                        }
                    )

                    // Priority Dropdown
                    ExposedDropdownMenuBox(
                        expanded = isPriorityExpanded,
                        onExpandedChange = { isPriorityExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedPriority.displayName,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Priority") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isPriorityExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
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

                    // Category Dropdown
                    ExposedDropdownMenuBox(
                        expanded = isCategoryExpanded,
                        onExpandedChange = { isCategoryExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = "${selectedCategory.icon} ${selectedCategory.displayName}",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
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

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        if (validateForm()) {
                            isSaving = true
                            //Save task using TaskViewModel
                            AppUtil.showToast(context, "Task saved successfully!")
                            navController.navigateUp()
                        } else {
                            AppUtil.showToast(context, "Please fix the errors above")
                        }
                    },
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isSaving) "Saving..." else "Save Task")
                }
            }
        }
    }

    //Add DatePickerDialog when isDatePickerVisible is true
    //For now, we'll implement a simple date picker later
    if (isDatePickerVisible) {
        // Simple implementation - set date to tomorrow for demo
        selectedDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }.time
        isDatePickerVisible = false
        AppUtil.showToast(context, "Date set to tomorrow (temporary)")
    }
}