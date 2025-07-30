package com.example.teamfitapp

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProjectManagement_Home : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var projectAdapter: ProjectAdapter
    private lateinit var tabLayout: TabLayout
    private val projectList = mutableListOf<Project>()
    private var currentStatus = "ongoing" // 현재 선택된 탭 상태

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_management_home)

        //ActionBar 설정
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)  //뒤로가기 버튼 활성화
            setDisplayShowHomeEnabled(true)
            title = "프로젝트 관리"
            setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this@ProjectManagement_Home, R.color.white)))
        }

        // 탭 레이아웃 초기화
        tabLayout = findViewById(R.id.tabLayout)

        // 탭 선택 리스너 설정
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        currentStatus = "ongoing"
                        loadProjectsFromFirebase("ongoing")
                    }
                    1 -> {
                        currentStatus = "completed"
                        loadProjectsFromFirebase("completed")
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_project)
        bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, Profile_Home::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_search -> {
                    //아무것도X
                    true
                }
                // 다른 메뉴들도 나중에 추가
                else -> false
            }
        }

        setupRecyclerView()

        // 테스트 데이터 Firebase에 추가 (한 번만 실행)
        //addTestDataToFirebase()  // 한 번 실행 후 주석 처리(데이터가 추가됐으니)
        //addMemberTestData()  // 팀원/업무 데이터 추가 (한 번 실행 후 주석)
        //addMultipleProjectMemberData() // 한 번 실행 후 주석 처리

        // Firebase에서 진행 중인 프로젝트 데이터 로드 (기본값)
        loadProjectsFromFirebase("ongoing")
    }

    // Firebase에서 상태별 프로젝트 로드
    private fun loadProjectsFromFirebase(status: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("projects")
            .whereEqualTo("status", status)
            .get()
            .addOnSuccessListener { documents ->
                projectList.clear()
                for (document in documents) {
                    val project = document.toObject(Project::class.java)
                    project.id = document.id
                    projectList.add(project)
                }
                projectAdapter.notifyDataSetChanged()
                Log.d("Firebase", "$status 프로젝트 ${projectList.size}개 로드됨")
            }
            .addOnFailureListener { exception ->
                Log.w("Firebase", "데이터 로드 실패: ", exception)
            }
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewProjects)

        // 어댑터 초기화 (클릭 리스너 포함)
        projectAdapter = ProjectAdapter(projectList) { project ->
            // 프로젝트 클릭 시 실행될 코드
            if (currentStatus == "ongoing") {
                // 진행 중인 프로젝트만 상세 화면으로 이동
                val intent = Intent(this, Project_Management_Ing::class.java)
                intent.putExtra("project_index", project.projectIndex)
                intent.putExtra("project_title", project.title)
                Log.d("DEBUG", "전달하는 프로젝트 인덱스: ${project.projectIndex}")
                startActivity(intent)
            } else {
                // 종료된 프로젝트는 클릭해도 이동하지 않거나 다른 화면으로 이동
                Log.d("DEBUG", "종료된 프로젝트: ${project.title}")
            }
        }

        recyclerView.adapter = projectAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onResume() {
        super.onResume()
        // 화면이 다시 보여질 때 현재 탭 상태에 맞는 데이터 새로고침
        loadProjectsFromFirebase(currentStatus)
    }

    // 테스트 데이터를 Firebase에 추가
    private fun addTestDataToFirebase() {
        val db = FirebaseFirestore.getInstance()

        val testProjects = listOf(
            hashMapOf(
                "projectIndex" to 1,
                "title" to "뮤지컬 회사 시아 관련 앱 서비스 프로젝트",
                "description" to "뮤지컬 관련 앱을 만드는 프로젝트입니다",
                "startDate" to "2025.08.01",
                "endDate" to "2025.10.31",
                "status" to "ongoing",
                "leaderId" to "test@test.com",
                "members" to listOf("test@test.com"),
                "createdAt" to System.currentTimeMillis()
            ),
            hashMapOf(
                "projectIndex" to 2,
                "title" to "팀 관리 앱 개발 프로젝트",
                "description" to "팀원들을 효율적으로 관리하는 앱",
                "startDate" to "2025.07.15",
                "endDate" to "2025.09.30",
                "status" to "ongoing",
                "leaderId" to "leader@test.com",
                "members" to listOf("leader@test.com", "member@test.com"),
                "createdAt" to System.currentTimeMillis()
            )
        )

        testProjects.forEach { project ->
            db.collection("projects")
                .add(project)
                .addOnSuccessListener {
                    Log.d("Firebase", "테스트 데이터 추가됨: ${project["title"]}")
                }
                .addOnFailureListener { e ->
                    Log.w("Firebase", "데이터 추가 실패: ", e)
                }
        }
    }

    private fun addMemberTestData() {
        val db = FirebaseFirestore.getInstance()

        val testMembers = listOf(
            hashMapOf(
                "projectIndex" to 1,
                "userId" to "test@test.com",
                "userName" to "홍길동",
                "userRole" to "UX/UI 디자인",
                "taskNames" to listOf("화면 와이어프레임", "화면 디자인", "화면 프로토타입"),
                "isCompleted" to listOf(true, true, true), // 모두 완료 = 100%
                "dueDates" to listOf("2025.08.05", "2025.08.07", "2025.08.09"),
                "createdAt" to System.currentTimeMillis()
            ),
            hashMapOf(
                "projectIndex" to 1,
                "userId" to "backend@test.com",
                "userName" to "팀멤버",
                "userRole" to "백엔드",
                "taskNames" to listOf("배경 조사 및 데스크 리서치", "유저 리서치(인터뷰 및 퍼소나)", "기능 도출 및 서비스 컨셉 정리", "문서화 작업"),
                "isCompleted" to listOf(false, false, false, true), // 1개만 완료
                "dueDates" to listOf("2025.08.01", "2025.08.02", "2025.08.03", "2025.08.14"),
                "createdAt" to System.currentTimeMillis()
            ),
            hashMapOf(
                "projectIndex" to 1,
                "userId" to "frontend@test.com",
                "userName" to "프론트맨",
                "userRole" to "프론트엔드",
                "taskNames" to listOf("UI 구현", "컴포넌트 개발"),
                "isCompleted" to listOf(false, false),
                "dueDates" to listOf("2025.08.10", "2025.08.15"),
                "createdAt" to System.currentTimeMillis()
            )
        )

        testMembers.forEach { member ->
            db.collection("project_members")
                .add(member)
                .addOnSuccessListener {
                    Log.d("Firebase", "팀원 데이터 추가됨: ${member["userName"]}")
                }
        }
    }

    // 여러 프로젝트 인덱스에 대한 팀원 데이터 추가
    private fun addMultipleProjectMemberData() {
        val db = FirebaseFirestore.getInstance()

        val testMembers = listOf(
            // 프로젝트 인덱스 0번 - test@test.com 사용자
            hashMapOf(
                "projectIndex" to 0,
                "userId" to "test@test.com",
                "userName" to "홍길동",
                "userRole" to "UX/UI 디자인",
                "taskNames" to listOf("인덱스0 와이어프레임", "인덱스0 디자인", "인덱스0 프로토타입"),
                "isCompleted" to listOf(false, false, true),
                "dueDates" to listOf("2025.07.20", "2025.07.25", "2025.07.30"),
                "createdAt" to System.currentTimeMillis()
            ),

            // 프로젝트 인덱스 1번 - test@test.com 사용자
            hashMapOf(
                "projectIndex" to 1,
                "userId" to "test@test.com",
                "userName" to "홍길동",
                "userRole" to "UX/UI 디자인",
                "taskNames" to listOf("화면 와이어프레임", "화면 디자인", "화면 프로토타입", "기타 세부작업"),
                "isCompleted" to listOf(true, true, false, false), // 50% 완료
                "dueDates" to listOf("2025.08.05", "2025.08.07", "2025.08.09", "2025.08.12"),
                "createdAt" to System.currentTimeMillis()
            ),

            // 프로젝트 인덱스 2번 - test@test.com 사용자
            hashMapOf(
                "projectIndex" to 2,
                "userId" to "test@test.com",
                "userName" to "홍길동",
                "userRole" to "프론트엔드",
                "taskNames" to listOf("UI 컴포넌트 개발", "API 연동", "테스트 코드 작성"),
                "isCompleted" to listOf(true, false, false), // 33% 완료
                "dueDates" to listOf("2025.07.15", "2025.07.20", "2025.07.25"),
                "createdAt" to System.currentTimeMillis()
            ),

            // 추가: 다른 팀원 예시 (프로젝트 1번)
            hashMapOf(
                "projectIndex" to 1,
                "userId" to "backend@test.com",
                "userName" to "김개발",
                "userRole" to "백엔드",
                "taskNames" to listOf("API 서버 구축", "데이터베이스 설계", "보안 처리"),
                "isCompleted" to listOf(true, true, false),
                "dueDates" to listOf("2025.08.01", "2025.08.03", "2025.08.15"),
                "createdAt" to System.currentTimeMillis()
            )
        )

        testMembers.forEach { member ->
            db.collection("project_members")
                .add(member)
                .addOnSuccessListener {
                    Log.d("Firebase", "프로젝트${member["projectIndex"]} ${member["userName"]} 데이터 추가됨")
                }
                .addOnFailureListener { e ->
                    Log.w("Firebase", "데이터 추가 실패: ", e)
                }
        }
    }
}