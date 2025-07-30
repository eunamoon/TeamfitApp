package com.example.teamfitapp

data class ProjectMember(
    var id: String = "",
    val projectIndex: Int = 0,
    val userId: String = "", // 팀원 이메일
    val userName: String = "", // 팀원 이름
    val userRole: String = "", // 팀원 역할
    val taskNames: List<String> = emptyList(), // 업무 목록 배열
    val isCompleted: List<Boolean> = emptyList(), // 완료 상태 배열 (taskNames와 인덱스 매칭)
    val dueDates: List<String> = emptyList(), // 마감일 배열
    val createdAt: Long = System.currentTimeMillis()
)
