package com.fak.classmate.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fak.classmate.model.SubTaskModel
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

    private val _subtasks = MutableStateFlow<Map<String, List<SubTaskModel>>>(emptyMap())
    val subtasks: StateFlow<Map<String, List<SubTaskModel>>> = _subtasks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        Log.d("TaskViewModel", "TaskViewModel initialized")
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

                    firestore.collection("users")
                        .document(user.uid)
                        .collection("tasks")
                        .document(taskId)
                        .set(task)
                        .await()

                    Log.d("TaskViewModel", "Task created successfully")
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
        Log.d("TaskViewModel", "Updating task: $taskId")
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            viewModelScope.launch {
                try {
                    _isLoading.value = true

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

                        Log.d("TaskViewModel", "Task updated successfully")
                        onResult(true, null)
                    } ?: run {
                        onResult(false, "Task not found")
                    }

                } catch (e: Exception) {
                    Log.e("TaskViewModel", "Error updating task: ${e.message}", e)
                    _error.value = e.localizedMessage
                    onResult(false, e.localizedMessage ?: "Failed to update task")
                } finally {
                    _isLoading.value = false
                }
            }
        } ?: run {
            onResult(false, "User not authenticated")
        }
    }

    // Load all tasks from user's subcollection
    fun loadTasks() {
        Log.d("TaskViewModel", "Loading tasks...")
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            viewModelScope.launch {
                try {
                    _isLoading.value = true
                    _error.value = null

                    val querySnapshot = firestore.collection("users")
                        .document(user.uid)
                        .collection("tasks")
                        .orderBy("dueDate", Query.Direction.ASCENDING)
                        .get()
                        .await()

                    Log.d("TaskViewModel", "Found ${querySnapshot.documents.size} tasks")

                    val taskList = querySnapshot.documents.mapNotNull { document ->
                        try {
                            document.toObject(TaskModel::class.java)
                        } catch (e: Exception) {
                            Log.e("TaskViewModel", "Error parsing document ${document.id}: ${e.message}")
                            null
                        }
                    }

                    _tasks.value = taskList
                    loadAllSubtasks(user.uid, taskList.map { it.id })

                } catch (e: Exception) {
                    Log.e("TaskViewModel", "Error loading tasks: ${e.message}", e)
                    _error.value = e.localizedMessage ?: "Failed to load tasks"
                } finally {
                    _isLoading.value = false
                }
            }
        } ?: run {
            Log.e("TaskViewModel", "No current user found")
        }
    }

    // Load subtasks for all tasks
    private suspend fun loadAllSubtasks(userId: String, taskIds: List<String>) {
        try {
            val subtaskMap = mutableMapOf<String, List<SubTaskModel>>()

            for (taskId in taskIds) {
                val subtaskSnapshot = firestore.collection("users")
                    .document(userId)
                    .collection("tasks")
                    .document(taskId)
                    .collection("subtasks")
                    .orderBy("order", Query.Direction.ASCENDING)
                    .get()
                    .await()

                val subtaskList = subtaskSnapshot.documents.mapNotNull { document ->
                    try {
                        document.toObject(SubTaskModel::class.java)
                    } catch (e: Exception) {
                        Log.e("TaskViewModel", "Error parsing subtask ${document.id}: ${e.message}")
                        null
                    }
                }

                subtaskMap[taskId] = subtaskList
            }

            _subtasks.value = subtaskMap
        } catch (e: Exception) {
            Log.e("TaskViewModel", "Error loading subtasks: ${e.message}", e)
        }
    }

    // Get subtasks for a specific task
    fun getSubtasksForTask(taskId: String): List<SubTaskModel> {
        return _subtasks.value[taskId] ?: emptyList()
    }

    // Create a new subtask
    fun createSubtask(
        taskId: String,
        title: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            viewModelScope.launch {
                try {
                    val subtaskId = UUID.randomUUID().toString()
                    val currentSubtasks = getSubtasksForTask(taskId)
                    val nextOrder = currentSubtasks.size

                    val subtask = SubTaskModel(
                        id = subtaskId,
                        title = title.trim(),
                        isCompleted = false,
                        taskId = taskId,
                        userId = user.uid,
                        createdAt = Timestamp.now(),
                        updatedAt = Timestamp.now(),
                        order = nextOrder
                    )

                    firestore.collection("users")
                        .document(user.uid)
                        .collection("tasks")
                        .document(taskId)
                        .collection("subtasks")
                        .document(subtaskId)
                        .set(subtask)
                        .await()

                    // Update local state
                    val updatedSubtasks = currentSubtasks + subtask
                    val newSubtaskMap = _subtasks.value.toMutableMap()
                    newSubtaskMap[taskId] = updatedSubtasks
                    _subtasks.value = newSubtaskMap

                    Log.d("TaskViewModel", "Subtask created successfully")
                    onResult(true, null)

                } catch (e: Exception) {
                    Log.e("TaskViewModel", "Error creating subtask: ${e.message}", e)
                    onResult(false, e.localizedMessage ?: "Failed to create subtask")
                }
            }
        } ?: run {
            onResult(false, "User not authenticated")
        }
    }

    // Toggle subtask completion
    fun toggleSubtaskCompletion(
        taskId: String,
        subtaskId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            viewModelScope.launch {
                try {
                    val subtaskList = getSubtasksForTask(taskId)
                    val subtask = subtaskList.find { it.id == subtaskId }

                    subtask?.let {
                        val updatedSubtask = it.copy(
                            isCompleted = !it.isCompleted,
                            updatedAt = Timestamp.now()
                        )

                        firestore.collection("users")
                            .document(user.uid)
                            .collection("tasks")
                            .document(taskId)
                            .collection("subtasks")
                            .document(subtaskId)
                            .set(updatedSubtask)
                            .await()

                        // Update local state
                        val updatedSubtasks = subtaskList.map { currentSubtask ->
                            if (currentSubtask.id == subtaskId) updatedSubtask else currentSubtask
                        }
                        val newSubtaskMap = _subtasks.value.toMutableMap()
                        newSubtaskMap[taskId] = updatedSubtasks
                        _subtasks.value = newSubtaskMap

                        onResult(true, null)
                    } ?: run {
                        onResult(false, "Subtask not found")
                    }
                } catch (e: Exception) {
                    Log.e("TaskViewModel", "Error toggling subtask: ${e.message}", e)
                    onResult(false, e.localizedMessage ?: "Failed to update subtask")
                }
            }
        }
    }

    // Delete a subtask
    fun deleteSubtask(
        taskId: String,
        subtaskId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            viewModelScope.launch {
                try {
                    firestore.collection("users")
                        .document(user.uid)
                        .collection("tasks")
                        .document(taskId)
                        .collection("subtasks")
                        .document(subtaskId)
                        .delete()
                        .await()

                    // Update local state
                    val currentSubtasks = getSubtasksForTask(taskId)
                    val updatedSubtasks = currentSubtasks.filter { it.id != subtaskId }
                    val newSubtaskMap = _subtasks.value.toMutableMap()
                    newSubtaskMap[taskId] = updatedSubtasks
                    _subtasks.value = newSubtaskMap

                    onResult(true, null)
                } catch (e: Exception) {
                    Log.e("TaskViewModel", "Error deleting subtask: ${e.message}", e)
                    onResult(false, e.localizedMessage ?: "Failed to delete subtask")
                }
            }
        }
    }

    // Toggle task completion
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

                        firestore.collection("users")
                            .document(user.uid)
                            .collection("tasks")
                            .document(taskId)
                            .set(updatedTask)
                            .await()

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

    // Delete a task
    fun deleteTask(taskId: String, onResult: (Boolean, String?) -> Unit) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            viewModelScope.launch {
                try {
                    // Delete all subtasks first
                    val subtasks = getSubtasksForTask(taskId)
                    for (subtask in subtasks) {
                        firestore.collection("users")
                            .document(user.uid)
                            .collection("tasks")
                            .document(taskId)
                            .collection("subtasks")
                            .document(subtask.id)
                            .delete()
                            .await()
                    }

                    // Delete the task
                    firestore.collection("users")
                        .document(user.uid)
                        .collection("tasks")
                        .document(taskId)
                        .delete()
                        .await()

                    // Remove from local state
                    _tasks.value = _tasks.value.filter { it.id != taskId }
                    val newSubtaskMap = _subtasks.value.toMutableMap()
                    newSubtaskMap.remove(taskId)
                    _subtasks.value = newSubtaskMap

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