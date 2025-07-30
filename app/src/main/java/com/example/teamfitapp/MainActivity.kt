package com.example.teamfitapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.teamfitapp.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_home -> {
                    // 현재 메인화면이니까 아무것도 안함
                    true
                }
                R.id.nav_profile -> {
                    // 프로필 화면으로 이동
                    val intent = Intent(this, Profile_Home::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_search -> {
                    val intent = Intent(this, ProjectManagement_Home::class.java)
                    startActivity(intent)
                    true
                }
                // 다른 메뉴들도 나중에 추가
                else -> false
            }
        }
    }
}