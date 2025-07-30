package com.example.teamfitapp

data class Project(
    var id: String = "",
    val projectIndex: Int = 0,  // 추가
    val title: String = "",
    val description: String = "",  // 순서 변경
    val startDate: String = "",
    val endDate: String = "",
    val status: String = "ongoing",
    val leaderId: String = "",  // userId → leaderId로 변경
    val members: List<String> = emptyList(),  // 추가
    val createdAt: Long = System.currentTimeMillis()
)