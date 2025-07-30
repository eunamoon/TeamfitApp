package com.example.teamfitapp

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat

class JobSingleCategoryAdapter(
    private val context: Context,
    private val categoryName: String,
    private val jobs: List<JobItem>,
    private val onJobSelected: (JobItem) -> Unit
) : BaseExpandableListAdapter() {

    override fun getGroupCount(): Int = 1 // 카테고리 1개만
    override fun getChildrenCount(groupPosition: Int): Int {
        println("Job count: ${jobs.size}")  // 로그 추가
        return jobs.size
    }
    override fun getGroup(groupPosition: Int): String = categoryName
    override fun getChild(groupPosition: Int, childPosition: Int): JobItem = jobs[childPosition]
    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()
    override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()
    override fun hasStableIds(): Boolean = false
    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true

    // 그룹 뷰 (카테고리 제목)
    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        println("getGroupView called: $groupPosition, expanded: $isExpanded")  // ← 이 줄 추가

        val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_expandable_list_item_1, parent, false)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = categoryName
        textView.textSize = 16f
        textView.setTextColor(Color.BLACK)

        println("Group text set: $categoryName")  // ← 이 줄 추가

        return view
    }

    // 자식 뷰 (직군 항목)
    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        println("getChildView called: group=$groupPosition, child=$childPosition")  // 로그 추가

        val jobItem = jobs[childPosition]
        println("Job item: ${jobItem.jobName}")  // 로그 추가

        val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_multiple_choice, parent, false)

        val checkBox = view.findViewById<CheckBox>(android.R.id.checkbox)
        val textView = view.findViewById<TextView>(android.R.id.text1)

        textView.text = jobItem.jobName
        checkBox.isChecked = jobItem.isSelected

        println("Setting text: ${jobItem.jobName}")  // 로그 추가

        if (jobItem.jobColor.startsWith("@color/")) {
            // colors.xml에서 색상 가져오기
            val colorName = jobItem.jobColor.substring(7) // "@color/" 제거
            val colorId = context.resources.getIdentifier(colorName, "color", context.packageName)
            if (colorId != 0) {
                textView.setBackgroundColor(ContextCompat.getColor(context, colorId))
            }
        } else {
            // 직접 헥스 코드 사용
            try {
                textView.setBackgroundColor(Color.parseColor(jobItem.jobColor))
            } catch (e: Exception) {
                // 오류 시 기본 색상
                textView.setBackgroundColor(Color.GRAY)
            }
        }

        textView.setTextColor(Color.WHITE)  // 배경색이 진하면 흰 글씨
        textView.setPadding(20, 10, 20, 10)  // 패딩 추가

        view.setOnClickListener {
            println("Item clicked: ${jobItem.jobName}")  // ← 이 줄 추가

            onJobSelected(jobItem)
        }

        return view
    }
}