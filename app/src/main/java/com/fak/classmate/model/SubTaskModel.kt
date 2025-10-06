package com.fak.classmate.model

import com.google.firebase.Timestamp
import java.util.Date

data class SubTaskModel(
    val id: String = "",
    val title: String = "",
    val isCompleted: Boolean = false,
    val taskId: String = "",           // Parent task ID
    val userId: String = "",           // For security/validation
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val order: Int = 0                 // For ordering subtasks
)

// Extension functions for subtask progress
fun List<SubTaskModel>.getCompletionProgress(): Pair<Int, Int> {
    val completed = this.count { it.isCompleted }
    val total = this.size
    return Pair(completed, total)
}

fun List<SubTaskModel>.getProgressPercentage(): Float {
    if (this.isEmpty()) return 0f
    val (completed, total) = getCompletionProgress()
    return (completed.toFloat() / total.toFloat()) * 100f
}

fun List<SubTaskModel>.isAllCompleted(): Boolean {
    return this.isNotEmpty() && this.all { it.isCompleted }
}

fun List<SubTaskModel>.hasAnyCompleted(): Boolean {
    return this.any { it.isCompleted }
}