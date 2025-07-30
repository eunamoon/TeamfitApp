package com.example.teamfitapp

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.CheckBox
import android.widget.LinearLayout
import android.content.res.Resources
import android.view.View
import android.widget.TextView
import android.widget.Button
import android.widget.HorizontalScrollView
import androidx.appcompat.widget.AppCompatButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

class Project_Management_Ing : AppCompatActivity() {
    lateinit var percentageText: TextView
    lateinit var userRoleText: TextView
    lateinit var taskContainer: LinearLayout
    lateinit var teamMemberContainer: LinearLayout
    lateinit var selectedMemberTaskContainer: LinearLayout

    private val checkboxList = mutableListOf<CheckBox>()
    private val textViewList = mutableListOf<TextView>()
    private var projectIndex: Int = 1
    private var currentUserId = "test@test.com"
    private var selectedMemberId: String = ""
    private val teamMemberButtons = mutableListOf<AppCompatButton>()
    private val teamMembers = mutableListOf<TeamMemberInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_management_ing)

        supportActionBar?.apply {
            title = "<"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        // 뷰 초기화
        percentageText = findViewById(R.id.percentageText)
        userRoleText = findViewById(R.id.userRoleText)
        taskContainer = findViewById(R.id.taskContainer)
        teamMemberContainer = findViewById(R.id.teamMemberContainer)
        selectedMemberTaskContainer = findViewById(R.id.selectedMemberTaskContainer)

        // Intent에서 프로젝트 정보 받기
        projectIndex = intent.getIntExtra("project_index", 1)

        // Firebase에서 내 업무 로드
        loadMyTasks()

        // 팀원 목록 로드
        loadTeamMembers()

        // 프로젝트 종료 버튼 클릭 리스너
        val confirmBtn = findViewById<Button>(R.id.confirmBtn)
        confirmBtn.setOnClickListener {
            endProject()
        }
    }

    private fun loadMyTasks() {
        val db = FirebaseFirestore.getInstance()

        // 먼저 users 테이블에서 최신 닉네임을 가져오고
        db.collection("users")
            .whereEqualTo("email", currentUserId)
            .get()
            .addOnSuccessListener { userDocuments ->
                var latestNickname = "사용자명" // 기본값

                if (!userDocuments.isEmpty) {
                    latestNickname = userDocuments.documents[0].getString("nickname") ?: "사용자명"
                }

                // 그 다음 project_members에서 업무 정보를 가져온다
                db.collection("project_members")
                    .whereEqualTo("projectIndex", projectIndex)
                    .whereEqualTo("userId", currentUserId)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            val document = documents.documents[0]
                            val userRole = document.getString("userRole") ?: ""
                            val taskNames = document.get("taskNames") as? List<String> ?: listOf()
                            val dueDates = document.get("dueDates") as? List<String> ?: listOf()
                            val isCompleted = document.get("isCompleted") as? List<Boolean> ?: listOf()

                            // 최신 닉네임을 사용해서 UI 업데이트
                            updateMyTasksUI(latestNickname, userRole, taskNames, dueDates, isCompleted)
                        } else {
                            Log.w("Firebase", "내 업무 데이터 없음")
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w("Firebase", "데이터 로드 실패: ", exception)
                    }
            }
            .addOnFailureListener { exception ->
                Log.w("Firebase", "사용자 데이터 로드 실패: ", exception)

                // users 테이블에서 가져오기 실패하면 기존 방식으로 fallback
                db.collection("project_members")
                    .whereEqualTo("projectIndex", projectIndex)
                    .whereEqualTo("userId", currentUserId)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            val document = documents.documents[0]
                            val userName = document.getString("userName") ?: ""
                            val userRole = document.getString("userRole") ?: ""
                            val taskNames = document.get("taskNames") as? List<String> ?: listOf()
                            val dueDates = document.get("dueDates") as? List<String> ?: listOf()
                            val isCompleted = document.get("isCompleted") as? List<Boolean> ?: listOf()

                            updateMyTasksUI(userName, userRole, taskNames, dueDates, isCompleted)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.w("Firebase", "프로젝트 멤버 데이터 로드 실패: ", e)
                    }
            }
    }

    private fun loadTeamMembers() {
        val db = FirebaseFirestore.getInstance()

        db.collection("project_members")
            .whereEqualTo("projectIndex", projectIndex)
            .get()
            .addOnSuccessListener { documents ->
                teamMembers.clear()

                for (document in documents) {
                    val userId = document.getString("userId") ?: ""
                    val userName = document.getString("userName") ?: ""
                    val userRole = document.getString("userRole") ?: ""

                    if (userId != currentUserId) { // 자신 제외
                        teamMembers.add(TeamMemberInfo(userId, userName, userRole))
                    }
                }

                createTeamMemberButtons(teamMembers)
            }
            .addOnFailureListener { exception ->
                Log.w("Firebase", "팀원 데이터 로드 실패: ", exception)
            }
    }

    private fun getRoleCategory(userRole: String): String {
        return when {
            userRole.contains("프론트엔드") || userRole.contains("백엔드") ||
                    userRole.contains("모바일") || userRole.contains("풀스택") ||
                    userRole.contains("개발") -> "개발"

            userRole.contains("디자이너") || userRole.contains("디자인") -> "디자인"

            userRole.contains("기획") || userRole.contains("매니저") ||
                    userRole.contains("마케터") || userRole.contains("운영") ||
                    userRole.contains("서비스") -> "기획"

            else -> "개발" // 기본값
        }
    }

    private fun createTeamMemberButtons(teamMembers: List<TeamMemberInfo>) {
        teamMemberContainer.removeAllViews()
        teamMemberButtons.clear()

        for (member in teamMembers) {
            val button = AppCompatButton(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = 8.dp
                    marginEnd = 8.dp
                }
                text = "${member.userName} | ${member.userRole}"
                textSize = 12f
                typeface = ResourcesCompat.getFont(this@Project_Management_Ing, R.font.pretendard_medium)

                // 큰 카테고리별 색상 및 배경 설정 (unchecked 상태)
                val roleCategory = getRoleCategory(member.userRole)
                when (roleCategory) {
                    "기획" -> {
                        setTextColor(ContextCompat.getColor(this@Project_Management_Ing, R.color.marketing))
                        background = ContextCompat.getDrawable(this@Project_Management_Ing, R.drawable.marketing_background_unchecked)
                    }
                    "개발" -> {
                        setTextColor(ContextCompat.getColor(this@Project_Management_Ing, R.color.gaebal))
                        background = ContextCompat.getDrawable(this@Project_Management_Ing, R.drawable.gaebal_background_unchecked)
                    }
                    "디자인" -> {
                        setTextColor(ContextCompat.getColor(this@Project_Management_Ing, R.color.design))
                        background = ContextCompat.getDrawable(this@Project_Management_Ing, R.drawable.design_background_unchecked)
                    }
                }

                setOnClickListener {
                    selectTeamMember(member.userId, member.userName, member.userRole)
                    updateButtonSelection(this, roleCategory)
                }
            }

            teamMemberContainer.addView(button)
            teamMemberButtons.add(button)
        }
    }

    private fun updateButtonSelection(selectedButton: AppCompatButton, selectedRoleCategory: String) {
        // 모든 버튼을 unselected 상태로
        teamMemberButtons.forEachIndexed { index, button ->
            val member = teamMembers[index]
            val roleCategory = getRoleCategory(member.userRole)
            when (roleCategory) {
                "기획" -> {
                    button.setTextColor(ContextCompat.getColor(this, R.color.marketing))
                    button.background = ContextCompat.getDrawable(this, R.drawable.marketing_background_unchecked)
                }
                "개발" -> {
                    button.setTextColor(ContextCompat.getColor(this, R.color.gaebal))
                    button.background = ContextCompat.getDrawable(this, R.drawable.gaebal_background_unchecked)
                }
                "디자인" -> {
                    button.setTextColor(ContextCompat.getColor(this, R.color.design))
                    button.background = ContextCompat.getDrawable(this, R.drawable.design_background_unchecked)
                }
            }
        }

        // 선택된 버튼을 selected 상태로
        when (selectedRoleCategory) {
            "기획" -> {
                selectedButton.setTextColor(ContextCompat.getColor(this, R.color.white))
                selectedButton.background = ContextCompat.getDrawable(this, R.drawable.marketing_background_checked)
            }
            "개발" -> {
                selectedButton.setTextColor(ContextCompat.getColor(this, R.color.white))
                selectedButton.background = ContextCompat.getDrawable(this, R.drawable.gaebal_background_checked)
            }
            "디자인" -> {
                selectedButton.setTextColor(ContextCompat.getColor(this, R.color.white))
                selectedButton.background = ContextCompat.getDrawable(this, R.drawable.design_background_checked)
            }
        }
    }

    private fun selectTeamMember(userId: String, userName: String, userRole: String) {
        selectedMemberId = userId
        loadSelectedMemberTasks(userId, userName, userRole)
    }

    private fun loadSelectedMemberTasks(userId: String, userName: String, userRole: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("project_members")
            .whereEqualTo("projectIndex", projectIndex)
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents[0]
                    val taskNames = document.get("taskNames") as? List<String> ?: listOf()
                    val dueDates = document.get("dueDates") as? List<String> ?: listOf()
                    val isCompleted = document.get("isCompleted") as? List<Boolean> ?: listOf()

                    displaySelectedMemberTasks(userName, userRole, taskNames, dueDates, isCompleted)
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firebase", "선택된 팀원 데이터 로드 실패: ", exception)
            }
    }

    private fun displaySelectedMemberTasks(userName: String, userRole: String, taskNames: List<String>, dueDates: List<String>, isCompleted: List<Boolean>) {
        selectedMemberTaskContainer.removeAllViews()

        // 팀원 정보 헤더
        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(30.dp, 16.dp, 30.dp, 8.dp)
        }

        val memberNameText = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = "$userName ($userRole)"
            textSize = 14f
            typeface = ResourcesCompat.getFont(this@Project_Management_Ing, R.font.pretendard_medium)
            setTextColor(ContextCompat.getColor(this@Project_Management_Ing, R.color.black))
        }

        val completedCount = isCompleted.count { it }
        val totalCount = taskNames.size
        val percentage = if (totalCount > 0) (completedCount * 100) / totalCount else 0

        val progressText = TextView(this).apply {
            text = "$percentage% 완료"
            textSize = 12f
            typeface = ResourcesCompat.getFont(this@Project_Management_Ing, R.font.pretendard_medium)
            setTextColor(ContextCompat.getColor(this@Project_Management_Ing, R.color.gray1))
        }

        headerLayout.addView(memberNameText)
        headerLayout.addView(progressText)
        selectedMemberTaskContainer.addView(headerLayout)

        // 팀원 업무 목록
        for (i in taskNames.indices) {
            val taskLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(30.dp, 8.dp, 30.dp, 8.dp)
            }

            // 업무명
            val taskNameText = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = taskNames.getOrElse(i) { "" }
                textSize = 13f
                typeface = ResourcesCompat.getFont(this@Project_Management_Ing, R.font.pretendard_medium)
                setTextColor(ContextCompat.getColor(this@Project_Management_Ing, R.color.black))
            }

            // 마감일
            val dueDateText = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    marginEnd = 12.dp
                }
                text = "~ ${dueDates.getOrElse(i) { "" }}"
                textSize = 11f
                setTextColor(ContextCompat.getColor(this@Project_Management_Ing, R.color.gray2))
            }

            // 상태 버튼 (확인완료/팀장확인)
            val statusButton = AppCompatButton(this).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 32.dp)
                val taskCompleted = isCompleted.getOrElse(i) { false }

                if (taskCompleted) {
                    text = "확인완료"
                    background = ContextCompat.getDrawable(this@Project_Management_Ing, R.drawable.button_selected)
                    setTextColor(ContextCompat.getColor(this@Project_Management_Ing, R.color.gray2))
                    isClickable = false
                } else {
                    text = "팀장확인"
                    background = ContextCompat.getDrawable(this@Project_Management_Ing, R.drawable.button_unselected)
                    setTextColor(ContextCompat.getColor(this@Project_Management_Ing, R.color.orange))

                    setOnClickListener {
                        showTaskConfirmDialog(userName, userRole, taskNames.getOrElse(i) { "" }, selectedMemberId, i)
                    }
                }

                textSize = 10f
                typeface = ResourcesCompat.getFont(this@Project_Management_Ing, R.font.pretendard_medium)
                setPadding(12.dp, 6.dp, 12.dp, 6.dp)
            }

            taskLayout.addView(taskNameText)
            taskLayout.addView(dueDateText)
            taskLayout.addView(statusButton)

            selectedMemberTaskContainer.addView(taskLayout)
        }
    }

    private fun showTaskConfirmDialog(memberName: String, memberRole: String, taskName: String, memberId: String, taskIndex: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_task_confirm, null)

        // 다이얼로그 텍스트 설정
        val memberNameText = dialogView.findViewById<TextView>(R.id.memberNameText)
        val memberRoleText = dialogView.findViewById<TextView>(R.id.memberRoleText)
        val dialogMessage = dialogView.findViewById<TextView>(R.id.dialogMessage)

        memberNameText.text = memberName
        memberRoleText.text = "포지션 | $memberRole"
        dialogMessage.text = "${memberName}님이 [$taskName] 담당 업무를 잘 수행했나요?"

        // 다이얼로그 생성 (배경 투명하게)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // 다이얼로그 배경을 투명하게 설정
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // 버튼 이벤트
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val confirmButton = dialogView.findViewById<Button>(R.id.confirmButton)

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        confirmButton.setOnClickListener {
            // Firebase 업데이트하고 UI 새로고침
            updateMemberTaskStatus(memberId, taskIndex, true)
            dialog.dismiss()

            // 선택된 팀원의 업무 목록 새로고침
            loadSelectedMemberTasks(memberId, memberName, memberRole)
        }

        dialog.show()
    }

    private fun updateMemberTaskStatus(memberId: String, taskIndex: Int, isCompleted: Boolean) {
        val db = FirebaseFirestore.getInstance()

        db.collection("project_members")
            .whereEqualTo("projectIndex", projectIndex)
            .whereEqualTo("userId", memberId)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents[0]
                    val documentId = document.id
                    val currentIsCompleted = document.get("isCompleted") as? MutableList<Boolean> ?: mutableListOf()

                    // 배열 크기 맞추기
                    while (currentIsCompleted.size <= taskIndex) {
                        currentIsCompleted.add(false)
                    }

                    // 해당 업무를 완료로 변경
                    currentIsCompleted[taskIndex] = isCompleted

                    // Firebase 업데이트
                    db.collection("project_members")
                        .document(documentId)
                        .update("isCompleted", currentIsCompleted)
                        .addOnSuccessListener {
                            Log.d("Firebase", "팀원 업무 상태 업데이트 성공")
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firebase", "업데이트 실패: ", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firebase", "문서 찾기 실패: ", e)
            }
    }

    private fun updateMyTasksUI(userName: String, userRole: String, taskNames: List<String>, dueDates: List<String>, isCompleted: List<Boolean>) {
        // 역할과 이름 업데이트
        userRoleText.text = "$userRole - $userName"

        // 기존 고정 체크박스들 숨기기
        hideStaticCheckboxes()

        // 동적으로 체크박스 생성
        createDynamicCheckboxes(taskNames, dueDates, isCompleted)

        // 초기 퍼센트 계산
        updatePercentage()
    }

    private fun hideStaticCheckboxes() {
        findViewById<LinearLayout>(R.id.staticTaskContainer).visibility = View.GONE
    }

    private fun createDynamicCheckboxes(taskNames: List<String>, dueDates: List<String>, isCompleted: List<Boolean>) {
        checkboxList.clear()
        textViewList.clear()
        taskContainer.removeAllViews()

        for (i in taskNames.indices) {
            val taskLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(12.dp, 12.dp, 12.dp, 12.dp)
            }

            // 체크박스 생성
            val checkbox = CheckBox(this).apply {
                layoutParams = LinearLayout.LayoutParams(24.dp, 24.dp).apply {
                    marginStart = 20.dp
                }
                setButtonDrawable(R.drawable.checkbox_custom)
                background = ContextCompat.getDrawable(this@Project_Management_Ing, android.R.color.transparent)
                // Firebase에서 가져온 isCompleted 값으로 체크 상태 설정
                isChecked = isCompleted.getOrElse(i) { false }
            }

            // 업무명 텍스트뷰
            val taskTextView = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = 12.dp
                }
                text = taskNames.getOrElse(i) { "" }
                textSize = 14f
                typeface = ResourcesCompat.getFont(this@Project_Management_Ing, R.font.pretendard_medium)
                // 체크 상태에 따라 텍스트 색상 설정
                setTextColor(if (checkbox.isChecked) {
                    ContextCompat.getColor(this@Project_Management_Ing, R.color.orange)
                } else {
                    ContextCompat.getColor(this@Project_Management_Ing, R.color.black)
                })
            }

            // 날짜 텍스트뷰
            val dateTextView = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    marginEnd = 20.dp
                }
                text = "~ ${dueDates.getOrElse(i) { "" }}"
                textSize = 12f
                setTextColor(ContextCompat.getColor(this@Project_Management_Ing, R.color.gray2))
            }

            // 체크박스 리스너 설정
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                // 텍스트 색상 변경
                taskTextView.setTextColor(if (isChecked) {
                    ContextCompat.getColor(this@Project_Management_Ing, R.color.orange)
                } else {
                    ContextCompat.getColor(this@Project_Management_Ing, R.color.black)
                })
                // 퍼센트 업데이트
                updatePercentage()
                // Firebase 업데이트
                updateTaskStatusInFirebase(i, isChecked)
            }

            // 레이아웃에 뷰들 추가
            taskLayout.addView(checkbox)
            taskLayout.addView(taskTextView)
            taskLayout.addView(dateTextView)

            // 컨테이너에 추가
            taskContainer.addView(taskLayout)

            // 리스트에 추가 (퍼센트 계산용)
            checkboxList.add(checkbox)
            textViewList.add(taskTextView)
        }
    }

    private fun updatePercentage() {
        val checkedCount = checkboxList.count { it.isChecked }
        val totalCount = checkboxList.size

        val percentage = if (totalCount > 0) {
            (checkedCount * 100) / totalCount
        } else {
            0
        }

        percentageText.text = "$percentage% 완료"
    }

    private fun updateTaskStatusInFirebase(taskIndex: Int, isChecked: Boolean) {
        val db = FirebaseFirestore.getInstance()

        // 현재 문서 찾기
        db.collection("project_members")
            .whereEqualTo("projectIndex", projectIndex)
            .whereEqualTo("userId", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents[0]
                    val documentId = document.id
                    val currentIsCompleted = document.get("isCompleted") as? MutableList<Boolean> ?: mutableListOf()

                    // 배열 크기가 taskIndex보다 작으면 확장
                    while (currentIsCompleted.size <= taskIndex) {
                        currentIsCompleted.add(false)
                    }

                    // 해당 인덱스 값 변경
                    currentIsCompleted[taskIndex] = isChecked

                    // Firebase 업데이트
                    db.collection("project_members")
                        .document(documentId)
                        .update("isCompleted", currentIsCompleted)
                        .addOnSuccessListener {
                            Log.d("Firebase", "업무 $taskIndex 상태 업데이트 성공: $isChecked")
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firebase", "업무 상태 업데이트 실패: ", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firebase", "문서 찾기 실패: ", e)
            }
    }

    private fun endProject() {
        val db = FirebaseFirestore.getInstance()

        // projects 테이블에서 해당 projectIndex의 상태를 "completed"로 변경
        db.collection("projects")
            .whereEqualTo("projectIndex", projectIndex)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val documentId = documents.documents[0].id

                    // status를 "completed"로 업데이트
                    db.collection("projects")
                        .document(documentId)
                        .update("status", "completed")
                        .addOnSuccessListener {
                            Log.d("Firebase", "프로젝트 $projectIndex 상태가 completed로 변경됨")

                            // 성공 시 이전 화면으로 돌아가기
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firebase", "프로젝트 상태 변경 실패: ", e)
                        }
                } else {
                    Log.w("Firebase", "프로젝트를 찾을 수 없음")
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firebase", "프로젝트 검색 실패: ", e)
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

// 팀원 정보 데이터 클래스
data class TeamMemberInfo(
    val userId: String,
    val userName: String,
    val userRole: String
)

// dp 변환 확장함수
val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()