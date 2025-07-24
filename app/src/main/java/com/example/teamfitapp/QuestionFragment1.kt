package com.example.teamfitapp

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2

class QuestionFragment1 : Fragment() {
    private var question: String? = null
    private var option1: String? = null
    private var option2: String? = null
    private var selectedAnswer: String? = null
    private var currentPage: Int = 1
    private var totalPages: Int = 1
    private lateinit var btnNext: ImageButton
    private lateinit var tvProgress: TextView
    private var imageResId: Int = R.drawable.question1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            question = it.getString("question")
            option1 = it.getString("option1")
            option2 = it.getString("option2")
            currentPage = it.getInt("currentPage")
            totalPages = it.getInt("totalPages")
            imageResId = it.getInt("imageResId", R.drawable.question1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_question1, container, false)

        val questionText = view.findViewById<TextView>(R.id.tvQuestion)
        val btnOption1 = view.findViewById<Button>(R.id.btnOption1)
        val btnOption2 = view.findViewById<Button>(R.id.btnOption2)
        val btnNext = view.findViewById<ImageButton>(R.id.btnNext)
        tvProgress = view.findViewById(R.id.tvProgress)

        // 진행 상태 표시
        tvProgress.text = "$currentPage/$totalPages"

        questionText.text = question
        btnOption1.text = option1
        btnOption2.text = option2

        val parentActivity = activity as? QuestionActivity

        val imageView = view.findViewById<ImageView>(R.id.imageView)
        imageView.setBackgroundResource(imageResId)

        btnNext.isEnabled = false // 기본 비활성화

        btnOption1.setOnClickListener {
            selectedAnswer = option1
            btnNext.isEnabled = true

            btnOption1.setBackgroundResource(R.drawable.btn_selected)

            btnOption2.setBackgroundResource(R.drawable.btn_unselected)

            btnNext.setBackgroundResource(R.drawable.btnnext) //다음 버튼 활성화 이미지
        }

        btnOption2.setOnClickListener {
            selectedAnswer = option2
            btnNext.isEnabled = true
            btnOption2.setBackgroundResource(R.drawable.btn_selected)

            btnOption1.setBackgroundResource(R.drawable.btn_unselected)

            btnNext.setBackgroundResource(R.drawable.btnnext) //다음 버튼 활성화 이미지

        }

        btnNext.setOnClickListener {
            selectedAnswer?.let { answer ->
                (activity as? QuestionActivity)?.onAnswerSelected(answer)
            }
        }


        return view
    }

    companion object {
        fun newInstance(
            question: String,
            option1: String,
            option2: String,
            currentPage: Int,
            totalPages: Int,
            imageResId: Int
        ) = QuestionFragment1().apply {
            arguments = Bundle().apply {
                putString("question", question)
                putString("option1", option1)
                putString("option2", option2)
                putInt("currentPage", currentPage)
                putInt("totalPages", totalPages)
                putInt("imageResId", imageResId)
            }
        }
    }


}