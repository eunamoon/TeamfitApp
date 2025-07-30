package com.example.teamfitapp

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.CheckBox
import android.widget.TextView

class JobExpandableListAdapter(
    private val context: Context,
    private val jobData: List<JobCategory>,
    private val onJobSelected: (JobItem) -> Unit
) : BaseExpandableListAdapter() {

    override fun getGroupCount(): Int = jobData.size
    override fun getChildrenCount(groupPosition: Int): Int = jobData[groupPosition].jobs.size
    override fun getGroup(groupPosition: Int): JobCategory = jobData[groupPosition]
    override fun getChild(groupPosition: Int, childPosition: Int): JobItem =
        jobData[groupPosition].jobs[childPosition]
    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()
    override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()
    override fun hasStableIds(): Boolean = false
    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true

    // 그룹 뷰 (카테고리 헤더)
    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val groupName = getGroup(groupPosition).categoryName
        val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_expandable_list_item_1, parent, false)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = groupName
        textView.textSize = 16f
        textView.setTextColor(Color.BLACK)
        textView.setPadding(40, 30, 40, 30)
        return view
    }

    // 자식 뷰 (직군 항목)
    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val jobItem = getChild(groupPosition, childPosition)
        val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_multiple_choice, parent, false)

        val checkBox = view.findViewById<CheckBox>(android.R.id.checkbox)
        val textView = view.findViewById<TextView>(android.R.id.text1)

        textView.text = jobItem.jobName
        textView.textSize = 14f
        textView.setPadding(60, 20, 40, 20)
        checkBox.isChecked = jobItem.isSelected

        view.setOnClickListener {
            // 다른 모든 항목 선택 해제
            jobData.forEach { category ->
                category.jobs.forEach { job ->
                    job.isSelected = false
                }
            }
            // 현재 항목만 선택
            jobItem.isSelected = true
            onJobSelected(jobItem)
            notifyDataSetChanged()
        }

        return view
    }
}