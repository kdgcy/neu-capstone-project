package com.fak.classmate.viewmodel

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
    private val currentUser = auth.currentUser

    // State flows for UI
    private val _tasks = MutableStateFlow<List<TaskModel>>(emptyList())
    val tasks: StateFlow<List<TaskModel>> = _tasks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadTasks()
    }

    // Create a new task
    fun createTask(
        title: String,
        description: String,
        dueDate: Date,
        priority: TaskPriority,
        category: TaskCategory,
        onResult: (Boolean, String?) -> Unit
    ) {
        currentUser?.let { user ->
            viewModelScope.launch {
                try {
                    _isLoading.value = true

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

                    firestore.collection("tasks")
                        .document(taskId)
                        .set(task)
                        .await()

                    // Refresh tasks after creation
                    loadTasks()
                    onResult(true, null)

                } catch (e: Exception) {
                    _error.value = e.localizedMessage
                    onResult(false, e.localizedMessage ?: "Failed to create task")
                } finally {
                    _isLoading.value = false
                }
            }
        } ?: run {
            onResult(false, "User not authenticated")
        }
    }

    // Load all tasks for current user
    fun loadTasks() {
        currentUser?.let { user ->
            viewModelScope.launch {
                try {
                    _isLoading.value = true
                    _error.value = null

                    val querySnapshot = firestore.collection("tasks")
                        .whereEqualTo("userId", user.uid)
                        .orderBy("dueDate", Query.Direction.ASCENDING)
                        .get()
                        .await()

                    val taskList = querySnapshot.documents.mapNotNull { document ->
                        document.toObject(TaskModel::class.java)
                    }

                    _tasks.value = taskList

                } catch (e: Exception) {
                    _error.value = e.localizedMessage ?: "Failed to load tasks"
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    // Update task completion status
    fun toggleTaskCompletion(taskId: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val task = _tasks.value.find { it.id == taskId }
                task?.let {
                    val updatedTask = it.copy(
                        isCompleted = !it.isCompleted,
                        updatedAt = Timestamp.now()
                    )

                    firestore.collection("tasks")
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

    // Update an existing task
    fun updateTask(
        taskId: String,
        title: String,
        description: String,
        dueDate: Date,
        priority: TaskPriority,
        category: TaskCategory,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val task = _tasks.value.find { it.id == taskId }
                task?.let {
                    val updatedTask = it.copy(
                        title = title.trim(),
                        description = description.trim(),
                        dueDate = Timestamp(dueDate),
                        priority = priority,
                        category = category,
                        updatedAt = Timestamp.now()
                    )

                    firestore.collection("tasks")
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

    // Delete a task
    fun deleteTask(taskId: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                firestore.collection("tasks")
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