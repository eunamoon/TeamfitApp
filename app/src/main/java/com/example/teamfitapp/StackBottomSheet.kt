package com.example.teamfitapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.tabs.TabLayout


class StackBottomSheet(
    private val onSelected: (List<String>) -> Unit
) : BottomSheetDialogFragment() {

    private val selectedTags = mutableSetOf<String>()

    private val stackTags = mapOf(
        "개발" to listOf("React", "Next.js", "Vue.js", "Angular", "Svelte", "Node.js", "Django", "Spring Boot", "Flutter", "Kotlin", "Firebase"),
        "디자인" to listOf("Figma", "Adobe XD", "Sketch", "Photoshop", "Illustrator", "Premiere Pro"),
        "기타 협업 툴" to listOf("Notion", "Slack", "Git", "GitHub", "Miro")
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.fragment_stack_bottom_sheet, container, false)
        val chipGroup = root.findViewById<ChipGroup>(R.id.chipGroup)
        val tabLayout = root.findViewById<TabLayout>(R.id.tabLayout)

        stackTags.keys.forEach { category ->
            tabLayout.addTab(tabLayout.newTab().setText(category))
        }

        fun showChips(category: String) {
            chipGroup.removeAllViews()
            stackTags[category]?.forEach { tag ->
                val chip = Chip(requireContext()).apply {
                    text = tag
                    isCheckable = true
                    isCheckedIconVisible = true
                    setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) selectedTags.add(tag) else selectedTags.remove(tag)
                    }
                }
                chipGroup.addView(chip)
            }
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                showChips(tab?.text.toString())
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        showChips("개발") // 초기

        root.findViewById<ImageButton>(R.id.confirmBtn).setOnClickListener {
            onSelected(selectedTags.toList())
            dismiss()
        }

        return root
    }
}