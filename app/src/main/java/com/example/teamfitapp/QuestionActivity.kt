package com.example.teamfitapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class QuestionActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private val selectedAnswers = mutableListOf<String>()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question)

        viewPager = findViewById(R.id.viewPager)

        val nickname = intent.getStringExtra("nickname") ?: "회원"

        val fragments = listOf(
            QuestionFragment1.newInstance(
                question = "${nickname}님의 의사소통 스타일은 무엇인가요?",
                option1 = "주관이 뚜렷한",
                option2 = "수용을 잘하는"
            ),
            QuestionFragment1.newInstance(
                question = "협업 시에 ${nickname}님은 보통 ...",
                option1 = "주도적으로 방향을 제시",
                option2 = "의견을 따르며 정리"
            ),
            QuestionFragment1.newInstance(
                question = "협업 시 선호하는 업무 스타일은 무엇인가요?",
                option1 = "빠르게 작업 후\n수정하여 완성도를 높이는",
                option2 = "처음부터 꼼꼼하게\n완성도를 높이는"
            ),
            QuestionFragment1.newInstance(
                question = "나는 피드백을 줄 때 / 받을 때",
                option1 = "직설적인 편이 좋다",
                option2 = "부드럽고 간접적인 편이 좋다"
            ),
            QuestionFragment1.newInstance(
                question = "프로젝트 기획 단계에서 ${nickname}님은...",
                option1 = "전체적인 그림을\n빠르게 구상한다",
                option2 = "단계별로 차근차근\n정리하여 구조화한다"
            ),
            QuestionFragment1.newInstance(
                question = "계획한 일정에 대해 어떻게 생각하시나요?",
                option1 = "데드라인을 최우선으로\n생각한다",
                option2 = "일정이 유동적이더라도\n만족을 중요시한다"
            )

            // 질문 추가 가능
        )

        val adapter = QuestionPagerAdapter(this, fragments)
        viewPager.adapter = adapter
        viewPager.isUserInputEnabled = false // swipe 비활성화, 버튼 클릭으로 이동

    }

    fun onAnswerSelected(answer: String) {
        val keyword = when (answer) {
            "주관이 뚜렷한" -> "#진취형"
            "수용을 잘하는" -> "#수용형"
            "주도적으로 방향을 제시" -> "#리더형"
            "의견을 따르며 정리" -> "#협력형"
            "빠르게 작업 후\n수정하여 완성도를 높이는" -> "#실행형"
            "처음부터 꼼꼼하게\n완성도를 높이는" -> "#준비형"
            "직설적인 편이 좋다" -> "#직설형"
            "부드럽고 간접적인 편이 좋다" -> "#배려형"
            "전체적인 그림을\n빠르게 구상한다" -> "#직관형"
            "단계별로 차근차근\n정리하여 구조화한다" -> "#분석형"
            "데드라인을 최우선으로\n생각한다" -> "#기한준수형"
            "일정이 유동적이더라도\n만족을 중요시한다" -> "#퀄리티 중시형"
            else -> "#기타"
        }

        selectedAnswers.add(keyword)

        if(selectedAnswers.size == 6) { // 질문 개수에 맞게 조절
            val user = FirebaseAuth.getInstance().currentUser
            val db = FirebaseFirestore.getInstance()

            val nickname = intent.getStringExtra("nickname") ?: ""
            val intro = intent.getStringExtra("intro") ?: ""
            val email = user?.email ?: ""

            if (user != null) {
                val userRef = db.collection("users").document(user.uid)
                val data = mapOf(
                    "email" to email,
                    "nickname" to nickname,
                    "intro" to intro,
                    "keywords" to selectedAnswers
                )

                userRef.set(data, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d("QuestionActivity", "모든 사용자 정보 저장 성공")
                    }
                    .addOnFailureListener { e ->
                        Log.e("QuestionActivity", "정보 저장 실패", e)
                    }
            }
            val intent = Intent(this, CommunityActivity::class.java)
            intent.putStringArrayListExtra("answers", ArrayList(selectedAnswers))
            startActivity(intent)
            finish()
        }else {
            viewPager.currentItem = selectedAnswers.size
        }

    }
}