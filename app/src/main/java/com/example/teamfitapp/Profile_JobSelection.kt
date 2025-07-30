package com.example.teamfitapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.teamfitapp.R
import com.google.firebase.firestore.FirebaseFirestore

class Profile_JobSelection : AppCompatActivity() {
    private lateinit var confirmBtn: Button
    private val selectedJobs = mutableSetOf<String>()

    private val jobCategories = mapOf(
        "개발" to listOf("프론트엔드", "백엔드", "모바일", "풀스택"),
        "디자인" to listOf("UI/UX 디자이너", "그래픽/시각 디자이너"),
        "기획" to listOf("서비스 기획자", "프로젝트 매니저", "마케터", "운영 매니저")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_job_selection)

        //ActionBar 설정
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "직군"
        }

        confirmBtn = findViewById<Button>(R.id.confirmBtn)

        setupJobSelection()

        confirmBtn.setOnClickListener {
            saveJobSelection()
        }
    }

    private fun setupJobSelection() {
        val mainContainer = findViewById<LinearLayout>(R.id.jobContainer)

        jobCategories.entries.forEachIndexed { index, (category, jobs) ->
            // 카테고리 헤더 추가
            val categoryHeader = createCategoryHeader(category)
            mainContainer.addView(categoryHeader)

            // 세부 직군 컨테이너 (처음에는 숨김)
            val jobsContainer = createJobsContainer(jobs)
            mainContainer.addView(jobsContainer)

            // 구분선 추가 (마지막 카테고리가 아닌 경우에만)
            if (index < jobCategories.size - 1) {
                val divider = createDivider()
                mainContainer.addView(divider)
            }

            // 헤더 클릭시 펼치기/접기
            // 헤더 클릭시 펼치기/접기 + 화살표 변경
            categoryHeader.setOnClickListener {
                val arrowText = categoryHeader.getChildAt(1) as TextView

                if (jobsContainer.visibility == View.VISIBLE) {
                    jobsContainer.visibility = View.GONE
                    arrowText.text = "▶"
                    arrowText.tag = "collapsed"
                } else {
                    jobsContainer.visibility = View.VISIBLE
                    arrowText.text = "▼"
                    arrowText.tag = "expanded"
                }
            }
        }
    }

    private fun createDivider(): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2 // 1dp를 px로 변환
            ).apply {
                leftMargin = 30
                rightMargin = 30
                topMargin = 16
                bottomMargin = 16
            }
            setBackgroundColor(resources.getColor(android.R.color.darker_gray, null))
        }
    }

    private fun createCategoryHeader(category: String): LinearLayout {
        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 16
                bottomMargin = 8
            }
            setPadding(30, 16, 30, 16)  // 스킬 선택과 동일하게 패딩 적용
        }

        val categoryText = TextView(this).apply {
            text = category
            textSize = 16f
            typeface = resources.getFont(R.font.pretendard_medium)
            setTextColor(resources.getColor(android.R.color.black, null))
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val arrowText = TextView(this).apply {
            text = "▶"  // 처음에는 접혀있으니까 오른쪽 화살표
            textSize = 14f
            setTextColor(resources.getColor(android.R.color.black, null))
            tag = "collapsed"  // 상태 저장
        }

        headerLayout.addView(categoryText)
        headerLayout.addView(arrowText)

        return headerLayout
    }

    private fun createJobsContainer(jobs: List<String>): LinearLayout {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            visibility = View.GONE // 처음에는 숨김
            setPadding(32, 0, 0, 0) // 들여쓰기
        }

        jobs.forEach { job ->
            val checkBox = CheckBox(this).apply {
                text = job
                textSize = 14f
                typeface = resources.getFont(R.font.pretendard_medium)
                setTextColor(resources.getColor(R.color.gray2, null))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 8
                    bottomMargin = 8
                }
                setPadding(0, 12, 0, 12)

                // 커스텀 체크박스 아이콘 적용
                setButtonDrawable(R.drawable.custom_checkbox)

                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedJobs.add(job)
                        setTextColor(resources.getColor(R.color.black, null))
                    } else {
                        selectedJobs.remove(job)
                        setTextColor(resources.getColor(R.color.gray2, null))
                    }
                }
            }

            container.addView(checkBox)
        }

        return container
    }

    private fun saveJobSelection() {
        if (selectedJobs.isEmpty()) {
            // 선택된 직군이 없으면 알림
            return
        }

        val jobList = selectedJobs.toList()

        // SharedPreferences에 저장
        val sharedPref = getSharedPreferences("user_profile", MODE_PRIVATE)
        sharedPref.edit().putStringSet("selected_jobs", selectedJobs).apply()

        // Firebase에 저장
        updateJobsInFirebase(jobList)

        finish()
    }

    private fun updateJobsInFirebase(jobs: List<String>) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .whereEqualTo("email", "test@test.com")
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val userDoc = documents.documents[0]
                    userDoc.reference.update("job", jobs.joinToString(", "))
                        .addOnSuccessListener {
                            // 성공적으로 업데이트됨
                        }
                        .addOnFailureListener { e ->
                            e.printStackTrace()
                        }
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}