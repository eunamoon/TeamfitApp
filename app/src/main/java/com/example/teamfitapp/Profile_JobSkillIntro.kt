package com.example.teamfitapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.teamfitapp.R
import com.google.firebase.firestore.FirebaseFirestore

class Profile_JobSkillIntro : AppCompatActivity() {
    lateinit var jobBtn: Button
    lateinit var skillBtn: Button
    lateinit var introBtn: Button
    lateinit var introTv: TextView
    lateinit var jobDisplayText: TextView
    lateinit var skillContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_job_skill_intro)

        //ActionBar 설정
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = " "
        }

        jobBtn = findViewById(R.id.jobBtn)
        skillBtn = findViewById(R.id.skillBtn)
        introBtn = findViewById(R.id.introBtn)
        introTv = findViewById(R.id.introTv)
        jobDisplayText = findViewById(R.id.jobDisplayText)
        skillContainer = findViewById(R.id.skillContainer)

        jobBtn.setOnClickListener {
            val intent = Intent(this, Profile_JobSelection::class.java)
            startActivity(intent)
        }

        skillBtn.setOnClickListener {
            val intent = Intent(this, Profile_SkillSelection::class.java)
            startActivity(intent)
        }

        introBtn.setOnClickListener {
            val intent = Intent(this, Profile_Introduction::class.java)
            startActivity(intent)
        }

        loadUserData()
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
    }

    private fun loadUserData() {
        loadUserDataFromFirebase()
    }

    private fun loadUserDataFromFirebase() {
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .whereEqualTo("email", "test@test.com")
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val userDoc = documents.documents[0].data

                    // 직군 정보 업데이트
                    val job = userDoc?.get("job") as? String ?: "UX·UI 디자인"
                    updateJobDisplay(job)

                    // 스킬 정보 업데이트 (배열로 받기)
                    val skills = userDoc?.get("skill") as? List<String> ?: emptyList()
                    updateSkillDisplay(skills)

                    // 자기소개 정보 업데이트
                    val introduction = userDoc?.get("selfIntro") as? String ?: "자기소개를 입력하세요."
                    introTv.text = introduction

                    // SharedPreferences에도 저장 (오프라인용)
                    val sharedPref = getSharedPreferences("user_profile", MODE_PRIVATE)
                    sharedPref.edit().putString("user_job", job).apply()
                    sharedPref.edit().putStringSet("selected_skills", skills.toSet()).apply()
                    sharedPref.edit().putString("user_introduction", introduction).apply()
                }
            }
            .addOnFailureListener { e ->
                loadUserDataFromLocal()
                e.printStackTrace()
            }
    }

    private fun loadUserDataFromLocal() {
        val sharedPref = getSharedPreferences("user_profile", MODE_PRIVATE)

        val savedJob = sharedPref.getString("user_job", "UX·UI 디자인")
        updateJobDisplay(savedJob ?: "UX·UI 디자인")

        val savedSkills =
            sharedPref.getStringSet("selected_skills", emptySet())?.toList() ?: emptyList()
        updateSkillDisplay(savedSkills)

        val savedIntroduction = sharedPref.getString("user_introduction", "자기소개를 입력하세요.")
        introTv.text = savedIntroduction
    }

    private fun updateJobDisplay(job: String) {
        jobDisplayText.text = job

        // 직군에 따라 배경색 변경
        val backgroundRes = when {
            job.contains("개발") || job.contains("프론트엔드") || job.contains("백엔드") || job.contains("모바일") || job.contains(
                "풀스택"
            ) -> R.drawable.gaebal_background

            job.contains("디자인") || job.contains("UI/UX") || job.contains("그래픽") -> R.drawable.design_background
            job.contains("기획") || job.contains("매니저") || job.contains("마케터") || job.contains("서비스 기획") -> R.drawable.marketing_background
            else -> R.drawable.design_background // 기본값
        }

        jobDisplayText.background = resources.getDrawable(backgroundRes, null)
    }

    private fun updateSkillDisplay(skills: List<String>) {
        skillContainer.removeAllViews() // 기존 스킬들 제거

        if (skills.isEmpty()) {
            // 스킬이 없을 때 기본 텍스트
            val emptyText = TextView(this).apply {
                text = "스킬을 선택해주세요"
                textSize = 14f
                typeface = resources.getFont(R.font.pretendard_medium)
                setTextColor(resources.getColor(R.color.gray2, null))
                setPadding(0, 16, 0, 16)
            }
            skillContainer.addView(emptyText)
            return
        }

        // 가로 스크롤뷰 생성
        val scrollView = HorizontalScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            isHorizontalScrollBarEnabled = false
        }

        // 스킬 버튼들을 담을 가로 레이아웃
        val skillRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // 각 스킬을 버튼으로 생성
        skills.forEach { skill ->
            val skillButton = Button(this).apply {
                text = skill
                textSize = 11f
                typeface = resources.getFont(R.font.pretendard_medium)
                setTextColor(resources.getColor(R.color.black, null))

                // 스킬 버튼 스타일
                setBackgroundResource(R.drawable.skill_selected_background)

                // 버튼 크기 설정 (최대 500으로 제한)
                val paint = paint
                val textWidth = paint.measureText(skill)
                val idealWidth = (textWidth + 64).toInt()
                val maxWidth = 500
                val finalWidth = if (idealWidth > maxWidth) maxWidth else idealWidth

                layoutParams = LinearLayout.LayoutParams(
                    finalWidth,  // 최대 500으로 제한된 가로 크기
                    80          // 고정 세로 크기
                ).apply {
                    rightMargin = 12
                }

                setPadding(16, 8, 16, 8)

                // 한 줄로 표시 (줄바꿈 방지)
                maxLines = 1
                isSingleLine = true

                // 클릭 불가능하게 설정 (표시용)
                isClickable = false
                isEnabled = false
            }

            skillRow.addView(skillButton)
        }

        scrollView.addView(skillRow)
        skillContainer.addView(scrollView)
    }

    // onSupportNavigateUp 함수를 여기로 이동 (클래스 레벨)
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}