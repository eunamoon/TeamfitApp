package com.example.teamfitapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.appcompat.app.AppCompatActivity
import com.example.teamfitapp.R

class Profile_PrivacySettings : AppCompatActivity() {

    lateinit var infoSwitch : ImageButton
    lateinit var jobSkillBtn : Button
    lateinit var jobDisplayText : TextView  // 직군을 표시하는 TextView 추가
    private var isInfoPublic = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_privacy_settings)

        //ActionBar 설정
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "정보 공개 설정"
        }

        infoSwitch = findViewById<ImageButton>(R.id.switch_info_public)
        jobSkillBtn = findViewById<Button>(R.id.jobSkillBtn)

        // 직군을 표시하는 TextView 찾기 (XML에서 ID 추가 필요)
        jobDisplayText = findViewById<TextView>(R.id.jobDisplayTextPrivacy)

        val sharedPref = getSharedPreferences("user_profile", MODE_PRIVATE)
        isInfoPublic = sharedPref.getBoolean("info_public", true)

        // 초기 이미지 설정
        updateSwitchImage()

        infoSwitch.setOnClickListener {
            // 상태 토글
            isInfoPublic = !isInfoPublic

            // 이미지 업데이트
            updateSwitchImage()

            // SharedPreferences에 저장
            sharedPref.edit().putBoolean("info_public", isInfoPublic).apply()

            // Firebase에 informationAccess 업데이트
            updateInformationAccess(isInfoPublic)
        }

        jobSkillBtn.setOnClickListener {
            val intent = Intent(this, Profile_JobSkillIntro::class.java)
            startActivity(intent)
        }

        // 데이터 로드
        loadUserData()
    }

    override fun onResume() {
        super.onResume()
        loadUserData() // 화면 돌아올 때마다 데이터 새로고침
    }

    private fun loadUserData() {
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

                    // 스위치 상태도 업데이트
                    val isPublic = userDoc?.get("informationAccess") as? Boolean ?: true
                    isInfoPublic = isPublic
                    updateSwitchImage()
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    private fun updateJobDisplay(job: String) {
        jobDisplayText.text = job

        // 직군에 따라 배경색 변경
        val backgroundRes = when {
            job.contains("개발") || job.contains("프론트엔드") || job.contains("백엔드") || job.contains("모바일") || job.contains("풀스택") -> R.drawable.gaebal_background
            job.contains("디자인") || job.contains("UI/UX") || job.contains("그래픽") -> R.drawable.design_background
            job.contains("기획") || job.contains("매니저") || job.contains("마케터") || job.contains("서비스 기획") -> R.drawable.marketing_background
            else -> R.drawable.design_background // 기본값
        }

        jobDisplayText.background = resources.getDrawable(backgroundRes, null)
    }

    private fun updateSwitchImage() {
        if (isInfoPublic) {
            infoSwitch.setImageResource(R.drawable.switch_on)
        } else {
            infoSwitch.setImageResource(R.drawable.switch_off)
        }
    }

    private fun updateInformationAccess(isPublic: Boolean) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .whereEqualTo("email", "test@test.com")
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val userDoc = documents.documents[0]
                    userDoc.reference.update("informationAccess", isPublic)
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