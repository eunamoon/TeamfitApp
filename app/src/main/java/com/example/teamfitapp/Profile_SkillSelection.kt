package com.example.teamfitapp

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.teamfitapp.R
import com.google.firebase.firestore.FirebaseFirestore

class Profile_SkillSelection : AppCompatActivity() {
    private val selectedSkills = mutableListOf<String>()
    private lateinit var confirmBtn: Button
    private lateinit var selectedSummaryText: TextView

    private val skillCategories = mapOf(
        "개발" to listOf("JavaScript", "Python", "Java", "React", "Node.js", "Spring"),
        "디자인" to listOf("Figma", "Adobe XD", "Sketch", "Zeplin", "Adobe Photoshop", "Illustrator", "Adobe After Effects", "Adobe Premiere Pro"),
        "협업 툴" to listOf("Slack", "Notion", "Jira", "Trello", "GitHub", "GitLab")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_skill_selection)

        //ActionBar 설정
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "스킬"
        }

        confirmBtn = findViewById<Button>(R.id.confirmBtn)
        selectedSummaryText = findViewById<TextView>(R.id.selectedSummaryText)

        // 기존 선택된 스킬들 먼저 불러오기
        loadExistingSkills()
    }

    private fun loadExistingSkills() {
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .whereEqualTo("email", "test@test.com")
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val userDoc = documents.documents[0].data
                    val existingSkills = userDoc?.get("skill") as? List<String> ?: emptyList()

                    // 기존 스킬들을 selectedSkills에 추가
                    selectedSkills.clear()
                    selectedSkills.addAll(existingSkills)

                    // UI 설정
                    setupSkillSelection()
                    updateSummary()

                    confirmBtn.setOnClickListener {
                        saveSkillSelection()
                    }
                }
            }
            .addOnFailureListener { e ->
                // Firebase 실패시 로컬에서 불러오기
                val sharedPref = getSharedPreferences("user_profile", MODE_PRIVATE)
                val localSkills = sharedPref.getStringSet("selected_skills", emptySet())?.toList() ?: emptyList()

                selectedSkills.clear()
                selectedSkills.addAll(localSkills)

                setupSkillSelection()
                updateSummary()

                confirmBtn.setOnClickListener {
                    saveSkillSelection()
                }

                e.printStackTrace()
            }
    }

    private fun setupSkillSelection() {
        val mainContainer = findViewById<LinearLayout>(R.id.skillContainer)

        skillCategories.entries.forEachIndexed { index, (category, skills) ->
            // 카테고리 헤더 추가
            val categoryHeader = createCategoryHeader(category)
            mainContainer.addView(categoryHeader)

            // 스킬 컨테이너 (처음에는 접혀있는 상태)
            val skillsContainer = createSkillsContainer(skills)
            mainContainer.addView(skillsContainer)

            // 구분선 추가 (마지막 카테고리가 아닌 경우에만)
            if (index < skillCategories.size - 1) {
                val divider = createDivider()
                mainContainer.addView(divider)
            }

            // 헤더 클릭시 펼치기/접기 + 화살표 변경
            categoryHeader.setOnClickListener {
                val arrowText = categoryHeader.getChildAt(1) as TextView

                if (skillsContainer.visibility == View.VISIBLE) {
                    skillsContainer.visibility = View.GONE
                    arrowText.text = "▶"
                    arrowText.tag = "collapsed"
                } else {
                    skillsContainer.visibility = View.VISIBLE
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
                2
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
            setPadding(30, 16, 30, 16)
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
            text = "▶"
            textSize = 14f
            setTextColor(resources.getColor(android.R.color.black, null))
            tag = "collapsed"
        }

        headerLayout.addView(categoryText)
        headerLayout.addView(arrowText)

        return headerLayout
    }

    private fun createSkillsContainer(skills: List<String>): LinearLayout {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            visibility = View.GONE
            setPadding(30, 0, 30, 16)
        }

        // FlexboxLayout 스타일로 배치
        var currentRow = createNewRow()
        container.addView(currentRow)
        var currentRowWidth = 0
        val screenWidth = resources.displayMetrics.widthPixels
        val maxRowWidth = screenWidth - 120 // 좌우 마진 고려

        skills.forEach { skill ->
            val button = createSkillButton(skill)

            // 버튼 크기 측정
            button.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )

            val buttonWidth = button.measuredWidth + 12 // 마진 포함

            // 현재 행에 들어갈 수 있는지 확인
            if (currentRowWidth + buttonWidth > maxRowWidth && currentRow.childCount > 0) {
                // 새 행 생성
                currentRow = createNewRow()
                container.addView(currentRow)
                currentRowWidth = 0
            }

            currentRow.addView(button)
            currentRowWidth += buttonWidth
        }

        return container
    }

    private fun createSkillButton(skill: String): Button {
        return Button(this).apply {
            text = skill
            textSize = 12f
            typeface = resources.getFont(R.font.pretendard_medium)

            // 텍스트 길이에 따른 동적 너비 계산
            val paint = paint
            val textWidth = paint.measureText(skill)
            val minWidth = 100
            val idealWidth = (textWidth + 64).toInt()
            val maxWidth = 300
            val finalWidth = when {
                idealWidth < minWidth -> minWidth
                idealWidth > maxWidth -> maxWidth
                else -> idealWidth
            }

            layoutParams = LinearLayout.LayoutParams(
                finalWidth,
                85
            ).apply {
                rightMargin = 12
                bottomMargin = 8
            }

            setPadding(16, 12, 16, 12)
            maxLines = 1
            isSingleLine = true

            // 기존에 선택된 스킬인지 확인하여 초기 상태 설정
            if (selectedSkills.contains(skill)) {
                setTextColor(resources.getColor(R.color.black, null))
                setBackgroundResource(R.drawable.skill_selected_background)
            } else {
                setTextColor(resources.getColor(R.color.gray2, null))
                setBackgroundResource(R.drawable.skill_unselected_background)
            }

            setOnClickListener {
                toggleSkillSelection(skill, this)
                updateSummary()
            }
        }
    }

    private fun createNewRow(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 12
            }
        }
    }

    private fun toggleSkillSelection(skill: String, button: Button) {
        if (selectedSkills.contains(skill)) {
            // 선택 해제
            selectedSkills.remove(skill)
            button.setTextColor(resources.getColor(R.color.gray2, null))
            button.setBackgroundResource(R.drawable.skill_unselected_background)
        } else {
            // 선택
            selectedSkills.add(skill)
            button.setTextColor(resources.getColor(R.color.black, null))
            button.setBackgroundResource(R.drawable.skill_selected_background)
        }
    }

    private fun updateSummary() {
        selectedSummaryText.text = "선택된 스킬: ${selectedSkills.joinToString(", ")}"
    }

    private fun saveSkillSelection() {
        // SharedPreferences에 저장
        val sharedPref = getSharedPreferences("user_profile", MODE_PRIVATE)
        sharedPref.edit().putStringSet("selected_skills", selectedSkills.toSet()).apply()

        // Firebase에 저장 (빈 배열도 저장)
        updateSkillsInFirebase(selectedSkills)

        finish()
    }

    private fun updateSkillsInFirebase(skills: List<String>) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .whereEqualTo("email", "test@test.com")
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val userDoc = documents.documents[0]
                    userDoc.reference.update("skill", skills)  // 빈 배열도 저장됨
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