package com.example.teamfitapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.teamfitapp.R
import com.google.firebase.firestore.FirebaseFirestore

class Profile_Introduction : AppCompatActivity() {
    lateinit var introductionEditText: EditText
    lateinit var confirmBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_introduction)

        //ActionBar 설정
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = " "
        }

        introductionEditText = findViewById(R.id.introductionEditText)
        confirmBtn = findViewById(R.id.confirmBtn)

        // 저장된 자기소개 불러오기
        loadIntroduction()

        // 저장 버튼 클릭
        confirmBtn.setOnClickListener {
            saveIntroduction()
            finish() // JobSkillIntro로 돌아가기
        }
    }

    private fun loadIntroduction() {
        // Firebase에서 불러오기
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .whereEqualTo("email", "test@test.com")
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val userDoc = documents.documents[0].data
                    val introduction = userDoc?.get("selfIntro") as? String ?: ""

                    if (introduction.isNotEmpty()) {
                        introductionEditText.setText(introduction)
                        introductionEditText.setSelection(introduction.length)
                    }

                    // SharedPreferences에도 저장 (오프라인용)
                    val sharedPref = getSharedPreferences("user_profile", MODE_PRIVATE)
                    sharedPref.edit().putString("user_introduction", introduction).apply()
                }
            }
            .addOnFailureListener { e ->
                // Firebase 실패시 SharedPreferences에서 불러오기
                val sharedPref = getSharedPreferences("user_profile", MODE_PRIVATE)
                val savedIntroduction = sharedPref.getString("user_introduction", "")

                if (!savedIntroduction.isNullOrEmpty()) {
                    introductionEditText.setText(savedIntroduction)
                    introductionEditText.setSelection(savedIntroduction.length)
                }
                e.printStackTrace()
            }
    }

    private fun saveIntroduction() {
        val introduction = introductionEditText.text.toString().trim()

        // SharedPreferences에 저장
        val sharedPref = getSharedPreferences("user_profile", MODE_PRIVATE)
        sharedPref.edit().putString("user_introduction", introduction).apply()

        // Firebase에 저장
        updateIntroductionInFirebase(introduction)
    }

    private fun updateIntroductionInFirebase(introduction: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .whereEqualTo("email", "test@test.com")
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val userDoc = documents.documents[0]
                    userDoc.reference.update("selfIntro", introduction)
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