package com.fak.classmate.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fak.classmate.model.TaskCategory
import com.fak.classmate.model.TaskModel
import com.fak.classmate.model.TaskPriority
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

class TaskViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val firestore = Firebase.firestore

    // State flows for UI
    private val _tasks = MutableStateFlow<List<TaskModel>>(emptyList())
    val tasks: StateFlow<List<TaskModel>> = _tasks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        Log.d("TaskViewModel", "TaskViewModel initialized with subcollection approach")
        loadTasks()
    }

    // Create a new task in user's subcollection
    fun createTask(
        title: String,
        description: String,
        dueDate: Date,
        priority: TaskPriority,
        category: TaskCategory,
        onResult: (Boolean, String?) -> Unit
    ) {
        Log.d("TaskViewModel", "Creating task: $title")
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            viewModelScope.launch {
                try {
                    _isLoading.value = true
                    Log.d("TaskViewModel", "User authenticated: ${user.uid}")

                    val taskId = UUID.randomUUID().toString()
                    val task = TaskModel(
                        id = taskId,
                        title = title.trim(),
                        description = description.trim(),
                        dueDate = Timestamp(dueDate),
                        priority = priority,
                        category = category,
                        isCompleted = false,
                        userId = user.uid,
                        createdAt = Timestamp.now(),
                        updatedAt = Timestamp.now()
                    )

                    Log.d("TaskViewModel", "Task object created: $task")

                    // Save to subcollection: users/{userId}/tasks/{taskId}
                    firestore.collection("users")
                        .document(user.uid)
                        .collection("tasks")
                        .document(taskId)
                        .set(task)
                        .await()

                    Log.d("TaskViewModel", "Task saved to subcollection successfully")

                    // Refresh tasks after creation
                    loadTasks()
                    onResult(true, null)

                } catch (e: Exception) {
                    Log.e("TaskViewModel", "Error creating task: ${e.message}", e)
                    _error.value = e.localizedMessage
                    onResult(false, e.localizedMessage ?: "Failed to create task")
                } finally {
                    _isLoading.value = false
                }
            }
        } ?: run {
            Log.e("TaskViewModel", "No authenticated user found")
            onResult(false, "User not authenticated")
        }
    }

    // Load all tasks from user's subcollection
    fun loadTasks() {
        Log.d("TaskViewModel", "Loading tasks from subcollection...")
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            viewModelScope.launch {
                try {
                    _isLoading.value = true
                    _error.value = null

                    Log.d("TaskViewModel", "Loading tasks for user: ${user.uid}")

                    // Query subcollection: users/{userId}/tasks
                    val querySnapshot = firestore.collection("users")
                        .document(user.uid)
                        .collection("tasks")
                        .orderBy("dueDate", Query.Direction.ASCENDING)
                        .get()
                        .await()

                    Log.d("TaskViewModel", "Found ${querySnapshot.documents.size} documents in subcollection")

                    val taskList = querySnapshot.documents.mapNotNull { document ->
                        Log.d("TaskViewModel", "Document ID: ${document.id}")
                        Log.d("TaskViewModel", "Document data: ${document.data}")
                        try {
                            val task = document.toObject(TaskModel::class.java)
                            Log.d("TaskViewModel", "Parsed task: $task")
                            task
                        } catch (e: Exception) {
                            Log.e("TaskViewModel", "Error parsing document ${document.id}: ${e.message}")
                            null
                        }
                    }

                    Log.d("TaskViewModel", "Successfully parsed ${taskList.size} tasks")
                    taskList.forEach { task ->
                        Log.d("TaskViewModel", "Task - Title: ${task.title}, ID: ${task.id}")
                    }

                    _tasks.value = taskList
                    Log.d("TaskViewModel", "Tasks state updated. Current count: ${_tasks.value.size}")

                } catch (e: Exception) {
                    Log.e("TaskViewModel", "Error loading tasks: ${e.message}", e)
                    _error.value = e.localizedMessage ?: "Failed to load tasks"
                } finally {
                    _isLoading.value = false
                }
            }
        } ?: run {
            Log.e("TaskViewModel", "No current user found when loading tasks")
        }
    }

    // Update task completion status in subcollection
    fun toggleTaskCompletion(taskId: String, onResult: (Boolean, String?) -> Unit) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            viewModelScope.launch {
                try {
                    val task = _tasks.value.find { it.id == taskId }
                    task?.let {
                        val updatedTask = it.copy(
                            isCompleted = !it.isCompleted,
                            updatedAt = Timestamp.now()
                        )

                        // Update in subcollection
                        firestore.collection("users")
                            .document(user.uid)
                            .collection("tasks")
                            .document(taskId)
                            .set(updatedTask)
                            .await()

                        // Update local state
                        _tasks.value = _tasks.value.map { currentTask ->
                            if (currentTask.id == taskId) updatedTask else currentTask
                        }

                        onResult(true, null)
                    } ?: run {
                        onResult(false, "Task not found")
                    }
                } catch (e: Exception) {
                    _error.value = e.localizedMessage
                    onResult(false, e.localizedMessage ?: "Failed to update task")
                }
            }
        }
    }

    // Delete a task from subcollection
    fun deleteTask(taskId: String, onResult: (Boolean, String?) -> Unit) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            viewModelScope.launch {
                try {
                    // Delete from subcollection
                    firestore.collection("users")
                        .document(user.uid)
                        .collection("tasks")
                        .document(taskId)
                        .delete()
                        .await()

                    // Remove from local state
                    _tasks.value = _tasks.value.filter { it.id != taskId }
                    onResult(true, null)

                } catch (e: Exception) {
                    _error.value = e.localizedMessage
                    onResult(false, e.localizedMessage ?: "Failed to delete task")
                }
            }
        }
    }

    // Get tasks filtered by completion status
    fun getTasksByCompletion(completed: Boolean): List<TaskModel> {
        return _tasks.value.filter { it.isCompleted == completed }
    }

    // Get tasks by priority
    fun getTasksByPriority(priority: TaskPriority): List<TaskModel> {
        return _tasks.value.filter { it.priority == priority }
    }

    // Get tasks by category
    fun getTasksByCategory(category: TaskCategory): List<TaskModel> {
        return _tasks.value.filter { it.category == category }
    }

    // Get overdue tasks
    fun getOverdueTasks(): List<TaskModel> {
        return _tasks.value.filter { task ->
            !task.isCompleted && task.dueDate.toDate().before(Date())
        }
    }

    // Get tasks due today
    fun getTasksDueToday(): List<TaskModel> {
        val today = Date()
        return _tasks.value.filter { task ->
            val dueDay = task.dueDate.toDate()
            today.year == dueDay.year &&
                    today.month == dueDay.month &&
                    today.date == dueDay.date
        }
    }

    // Get tasks due soon (within 3 days)
    fun getTasksDueSoon(): List<TaskModel> {
        val today = Date()
        val threeDaysFromNow = Date(today.time + (3 * 24 * 60 * 60 * 1000))

        return _tasks.value.filter { task ->
            !task.isCompleted &&
                    task.dueDate.toDate().after(today) &&
                    task.dueDate.toDate().before(threeDaysFromNow)
        }
    }

    // Get task statistics
    fun getTaskStats(): TaskStats {
        val allTasks = _tasks.value
        Log.d("TaskViewModel", "Getting task stats. Total tasks: ${allTasks.size}")
        return TaskStats(
            total = allTasks.size,
            completed = allTasks.count { it.isCompleted },
            pending = allTasks.count { !it.isCompleted },
            overdue = getOverdueTasks().size,
            dueToday = getTasksDueToday().size,
            dueSoon = getTasksDueSoon().size
        )
    }

    // Clear error state
    fun clearError() {
        _error.value = null
    }
}

// Data class for task statistics
data class TaskStats(
    val total: Int,
    val completed: Int,
    val pending: Int,
    val overdue: Int,
    val dueToday: Int,
    val dueSoon: Int
)