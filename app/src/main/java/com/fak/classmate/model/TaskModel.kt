package com.fak.classmate.model

import com.google.firebase.Timestamp

enum class TaskPriority(val displayName: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High")
}

enum class TaskCategory(val displayName: String, val icon: String) {
    SCHOOL("School", "📚"),
    WORK("Work", "💼"),
    PERSONAL("Personal", "👤"),
    OTHER("Other", "📌")
}

data class Subtask(
    val id: String = "",
    val title: String = "",
    var isCompleted: Boolean = false
) {
    // Convert to Map for Firebase
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "title" to title,
            "isCompleted" to isCompleted
        )
    }

    companion object {
        // Create from Map (Firebase data)
        fun fromMap(map: Map<String, Any>): Subtask {
            return Subtask(
                id = map["id"] as? String ?: "",
                title = map["title"] as? String ?: "",
                isCompleted = map["isCompleted"] as? Boolean ?: false
            )
        }
    }
}

data class TaskModel(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val dueDate: Timestamp = Timestamp.now(),
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val category: TaskCategory = TaskCategory.OTHER,
    val isCompleted: Boolean = false,
    val userId: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val subtasks: MutableList<Subtask> = mutableListOf()
) {
    // Convert to Map for Firebase
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "title" to title,
            "description" to description,
            "dueDate" to dueDate,
            "priority" to priority.name,
            "category" to category.name,
            "isCompleted" to isCompleted,
            "userId" to userId,
            "createdAt" to createdAt,
            "subtasks" to subtasks.map { it.toMap() }
        )
    }

    companion object {
        // Create from Map (Firebase document)
        fun fromMap(map: Map<String, Any>): TaskModel {
            val subtasksList = (map["subtasks"] as? List<*>)?.mapNotNull { subtaskData ->
                (subtaskData as? Map<String, Any>)?.let { Subtask.fromMap(it) }
            }?.toMutableList() ?: mutableListOf()

            return TaskModel(
                id = map["id"] as? String ?: "",
                title = map["title"] as? String ?: "",
                description = map["description"] as? String ?: "",
                dueDate = map["dueDate"] as? Timestamp ?: Timestamp.now(),
                priority = try {
                    TaskPriority.valueOf(map["priority"] as? String ?: "MEDIUM")
                } catch (e: Exception) {
                    TaskPriority.MEDIUM
                },
                category = try {
                    TaskCategory.valueOf(map["category"] as? String ?: "OTHER")
                } catch (e: Exception) {
                    TaskCategory.OTHER
                },
                isCompleted = map["isCompleted"] as? Boolean ?: false,
                userId = map["userId"] as? String ?: "",
                createdAt = map["createdAt"] as? Timestamp ?: Timestamp.now(),
                subtasks = subtasksList
            )
        }
    }
}