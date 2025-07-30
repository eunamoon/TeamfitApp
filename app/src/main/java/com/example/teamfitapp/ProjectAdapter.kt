package com.example.teamfitapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProjectAdapter(
    private val projectList: List<Project>,
    private val onItemClick: (Project) -> Unit
) : RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder>() {

    // ViewHolder 클래스
    class ProjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val projectDate: TextView = itemView.findViewById(R.id.projectDate)
        val projectTitle: TextView = itemView.findViewById(R.id.projectTitle)
    }

    // ViewHolder 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_project, parent, false)
        return ProjectViewHolder(view)
    }

    // 데이터 바인딩
    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val project = projectList[position]

        // 날짜 형식 설정
        val dateText = "${project.startDate} - ${project.endDate}"
        holder.projectDate.text = dateText

        // 프로젝트 제목 설정
        holder.projectTitle.text = project.title

        // 클릭 리스너
        holder.itemView.setOnClickListener {
            onItemClick(project)
        }
    }

    // 아이템 개수 반환
    override fun getItemCount(): Int = projectList.size
}