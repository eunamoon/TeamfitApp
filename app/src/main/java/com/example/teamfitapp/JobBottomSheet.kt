package com.example.teamfitapp

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class JobBottomSheet(
    private val onSelected: (List<String>) -> Unit
) : BottomSheetDialogFragment() {

    private val selectedTags = mutableSetOf<String>()
    private val elseMap = mutableMapOf<String, EditText>() // groupTag → EditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.fragment_job_botton_sheet, container, false)
        val containerLayout = root.findViewById<LinearLayout>(R.id.jobContainer)

        val jobMap = mapOf(
            "개발" to listOf("프론트엔드", "백엔드", "모바일", "풀스택", "기타(직접입력)"),
            "디자인" to listOf("UI/UX", "그래픽/시각", "기타(직접입력)"),
            "기획" to listOf("서비스 기획자", "프로젝트 매니저", "마케터", "운영 매니저", "기타(직접입력)")
        )

        jobMap.forEach { (group, items) ->
            val groupLabel = TextView(requireContext()).apply {
                text = group
                setTypeface(null, Typeface.BOLD)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setPadding(0, 24, 0, 12)
            }
            containerLayout.addView(groupLabel)

            val subLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                visibility = View.GONE
            }

            groupLabel.setOnClickListener {
                subLayout.visibility = if (subLayout.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }

            items.forEach { item ->
                val checkBox = CheckBox(requireContext()).apply {
                    text = item
                    setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            selectedTags.add(item)
                            if (item.contains("기타")) {
                                elseMap[group]?.visibility = View.VISIBLE
                            }
                        } else {
                            selectedTags.remove(item)
                            if (item.contains("기타")) {
                                elseMap[group]?.visibility = View.GONE
                                elseMap[group]?.text?.clear()
                            }
                        }
                    }
                }
                subLayout.addView(checkBox)

                // 해당 항목이 기타이면 EditText 추가
                if (item.contains("기타")) {
                    val input = EditText(requireContext()).apply {
                        hint = "$group 항목 직접 입력"
                        visibility = View.GONE
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { topMargin = 8 }
                    }
                    subLayout.addView(input)
                    elseMap[group] = input
                }
            }

            containerLayout.addView(subLayout)
        }

        root.findViewById<ImageButton>(R.id.confirmBtn).setOnClickListener {
            val result = selectedTags.toMutableList()
            // 기타 항목 대체
            jobMap.keys.forEach { group ->
                if (result.contains("기타(직접입력)")) {
                    val customText = elseMap[group]?.text?.toString()?.trim().orEmpty()
                    if (customText.isNotEmpty()) {
                        result.remove("기타(직접입력)")
                        result.add(customText)
                    }
                }
            }
            onSelected(result)
            dismiss()
        }

        return root
    }
}