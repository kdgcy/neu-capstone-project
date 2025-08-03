package com.fak.classmate.model

import com.google.firebase.Timestamp
import java.util.Date

data class TaskModel(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val dueDate: Timestamp = Timestamp.now(),
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val category: TaskCategory = TaskCategory.ASSIGNMENT,
    val isCompleted: Boolean = false,
    val userId: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

enum class TaskPriority(val displayName: String, val color: Long) {
    HIGH("High Priority", 0xFFE57373),      // Light Red
    MEDIUM("Medium Priority", 0xFFFFB74D),  // Light Orange
    LOW("Low Priority", 0xFF81C784)         // Light Green
}

enum class TaskCategory(val displayName: String, val icon: String) {
    ASSIGNMENT("Assignment", "📝"),
    EXAM("Exam", "📚"),
    PROJECT("Project", "🎯"),
    STUDY("Study Session", "💡"),
    MEETING("Meeting", "👥"),
    PERSONAL("Personal", "⚡"),
    OTHER("Other", "📌")
}

// Extension functions for easy date handling
fun TaskModel.isOverdue(): Boolean {
    return !isCompleted && dueDate.toDate().before(Date())
}

fun TaskModel.isDueToday(): Boolean {
    val today = Date()
    val dueDay = dueDate.toDate()

    return today.year == dueDay.year &&
            today.month == dueDay.month &&
            today.date == dueDay.date
}

fun TaskModel.isDueSoon(): Boolean {
    val today = Date()
    val threeDaysFromNow = Date(today.time + (3 * 24 * 60 * 60 * 1000)) // 3 days in milliseconds

    return !isCompleted && dueDate.toDate().before(threeDaysFromNow) && dueDate.toDate().after(today)
}