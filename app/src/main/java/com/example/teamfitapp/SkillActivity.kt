package com.example.teamfitapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class SkillActivity : AppCompatActivity() {

    private val selectedJobTags = mutableListOf<String>()
    private val selectedStackTags = mutableListOf<String>()

    private lateinit var nextButton: Button
    private lateinit var selectedSummaryText: TextView

    val nickname = intent.getStringExtra("nickname") ?: "회원"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_skill)

        selectedSummaryText = findViewById(R.id.selectedSummaryText)
        nextButton = findViewById(R.id.nextButton)

        findViewById<Button>(R.id.jobButton).setOnClickListener {
            JobBottomSheet { selected ->
                selectedJobTags.clear()
                selectedJobTags.addAll(selected)
                updateSummary()
            }.show(supportFragmentManager, "JobSheet")
        }

        findViewById<Button>(R.id.stackButton).setOnClickListener {
            StackBottomSheet { selected ->
                selectedStackTags.clear()
                selectedStackTags.addAll(selected)
                updateSummary()
            }.show(supportFragmentManager, "StackSheet")
        }

        // ✅ 클릭 리스너는 여기에 있어야 함
        nextButton.setOnClickListener {
            if (selectedJobTags.isEmpty() || selectedStackTags.isEmpty()) {
                Toast.makeText(this, "직군과 스택을 모두 선택해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveSkillInfo(selectedJobTags, selectedStackTags)

            val intent = Intent(this, QuestionActivity::class.java)
            intent.putStringArrayListExtra("jobTags", ArrayList(selectedJobTags))
            intent.putStringArrayListExtra("stackTags", ArrayList(selectedStackTags))
            intent.putExtra("nickname", nickname)
            startActivity(intent)
        }
    }

    private fun updateSummary() {
        val combined = selectedJobTags + selectedStackTags
        selectedSummaryText.text = "선택된 태그: ${combined.joinToString(", ")}"
    }

    private fun saveSkillInfo(jobTags: List<String>, stackTags: List<String>) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseFirestore.getInstance().collection("users").document(uid)

        val updateData = mapOf(
            "jobTags" to jobTags,
            "stackTags" to stackTags
        )

        userRef.set(updateData, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "스킬 정보 저장 완료", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "스킬 저장 실패: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}