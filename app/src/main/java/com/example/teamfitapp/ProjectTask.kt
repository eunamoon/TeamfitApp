package com.example.teamfitapp

data class ProjectTask(
    var id: String = "",
    val projectIndex: Int = 0,
    val taskIndex: Int = 0,
    val userId: String = "", // 이메일
    val userName: String = "",
    val userRole: String = "",
    val taskName: String = "",
    val isCompleted: Boolean = false,
    val dueDate: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
