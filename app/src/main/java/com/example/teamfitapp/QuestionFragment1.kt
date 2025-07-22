package com.example.teamfitapp

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [QuestionFragment1.newInstance] factory method to
 * create an instance of this fragment.
 */
class QuestionFragment1 : Fragment() {
    private var question: String? = null
    private var option1: String? = null
    private var option2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            question = it.getString("question")
            option1 = it.getString("option1")
            option2 = it.getString("option2")
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

        questionText.text = question
        btnOption1.text = option1
        btnOption2.text = option2

        val parentActivity = activity as? QuestionActivity

        btnOption1.setOnClickListener {
            parentActivity?.onAnswerSelected(option1 ?: "")
        }

        btnOption2.setOnClickListener {
            parentActivity?.onAnswerSelected(option2 ?: "")
        }

        return view
    }

    companion object {
        fun newInstance(question: String, option1: String, option2: String) =
            QuestionFragment1().apply {
                arguments = Bundle().apply {
                    putString("question", question)
                    putString("option1", option1)
                    putString("option2", option2)
                }
            }
    }


}