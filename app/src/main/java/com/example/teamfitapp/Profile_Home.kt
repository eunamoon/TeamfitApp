package com.example.teamfitapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.teamfitapp.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView

class Profile_Home : AppCompatActivity() {
    lateinit var profileIB : ImageButton
    lateinit var profileIV : CircleImageView

    lateinit var userName : TextView
    lateinit var edtUserName : ImageButton

    lateinit var privacyBtn : Button

    val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // 프로필 이미지 바로 적용
            profileIV.setImageURI(it)
            // 이미지 저장
            saveProfileImage(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_home)

        // ActionBar 설정
        supportActionBar?.apply {
            title = "마이페이지"
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowHomeEnabled(true)
/*
            // ActionBar 배경색을 흰색으로
            setBackgroundDrawable(resources.getDrawable(android.R.color.white, null))*/
        }
/*
        // 타이틀 텍스트뷰를 직접 찾아서 스타일 적용
        val titleTextView = findViewById<TextView>(resources.getIdentifier("action_bar_title", "id", "android"))
        titleTextView?.apply {
            typeface = resources.getFont(R.font.pretendard_semibold)
            setTextColor(resources.getColor(R.color.black, null))
            textSize = 18f
        }*/

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_profile)
        bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_profile -> {
                    // 프로필 화면이니까 아무것도 안함
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

        profileIB = findViewById(R.id.profile_image_btn)
        profileIV = findViewById(R.id.profile_image)
        userName = findViewById(R.id.userName)
        edtUserName = findViewById(R.id.edtUserName)
        privacyBtn = findViewById(R.id.privacyBtn)

        profileIB.setOnClickListener {
            changeProfileImage()
        }

        edtUserName.setOnClickListener {
            showEditNameDialog()
        }

        privacyBtn.setOnClickListener {
            val intent = Intent(this, Profile_PrivacySettings::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserData() // 화면 돌아올 때마다 데이터 새로고침
    }

    fun showEditNameDialog() {
        val builder = AlertDialog.Builder(this)
        val input = EditText(this)

        input.hint = "새로운 이름을 입력하세요."

        builder.setTitle("이름 변경")
            .setView(input)
            .setPositiveButton("확인") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    // 화면에 바로 반영
                    userName.text = newName
                    // 로컬 저장 및 Firebase 업데이트
                    saveUserName(newName)
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    fun saveUserName(name: String) {
        val sharedPref = getSharedPreferences("user_profile", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("user_name", name)
            apply()
        }

        // Firebase에 nickname 업데이트
        updateNicknameInFirebase(name)
    }

    private fun updateNicknameInFirebase(nickname: String) {
        val db = FirebaseFirestore.getInstance()

        // 이메일로 사용자 문서 찾기
        db.collection("users")
            .whereEqualTo("email", "test@test.com")
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val userDoc = documents.documents[0]
                    userDoc.reference.update("nickname", nickname)
                        .addOnSuccessListener {
                            // 성공적으로 업데이트됨
                        }
                        .addOnFailureListener { e ->
                            // 에러 처리
                            e.printStackTrace()
                        }
                }
            }
            .addOnFailureListener { e ->
                // 쿼리 실패 처리
                e.printStackTrace()
            }
    }

    //프로필 이미지 변경
    fun changeProfileImage() {
        galleryLauncher.launch("image/*")
    }

    // 프로필 이미지 저장
    fun saveProfileImage(uri: Uri) {
        val sharedPref = getSharedPreferences("user_profile", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("profile_image_uri", uri.toString())
            apply()
        }
    }

    fun loadUserData() {
        // Firebase에서 사용자 데이터 불러오기
        loadUserDataFromFirebase()
    }

    private fun loadUserDataFromFirebase() {
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .whereEqualTo("email", "test@test.com")
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val userDoc = documents.documents[0].data

                    // nickname 업데이트
                    val nickname = userDoc?.get("nickname") as? String ?: "사용자명"
                    userName.text = nickname

                    // 직군 정보 업데이트
                    val job = userDoc?.get("job") as? String ?: "UX·UI 디자인"
                    updateJobDisplay(job)

                    // SharedPreferences에도 저장 (오프라인용)
                    val sharedPref = getSharedPreferences("user_profile", MODE_PRIVATE)
                    sharedPref.edit().putString("user_name", nickname).apply()
                    sharedPref.edit().putString("user_job", job).apply()

                    // 정보 공개 상태 업데이트
                    val isPublic = userDoc?.get("informationAccess") as? Boolean ?: true
                    privacyBtn.text = if (isPublic) "정보 공개 중" else "정보 비공개"
                    sharedPref.edit().putBoolean("info_public", isPublic).apply()
                }
            }
            .addOnFailureListener { e ->
                // Firebase 실패시 SharedPreferences에서 불러오기 (백업)
                loadUserDataFromLocal()
                e.printStackTrace()
            }
    }

    private fun updateJobDisplay(job: String) {
        // selectRole TextView 찾기 (XML에서 직군을 표시하는 TextView)
        val selectRole = findViewById<TextView>(R.id.selectRole)
        selectRole.text = job
    }

    private fun loadUserDataFromLocal() {
        val sharedPref = getSharedPreferences("user_profile", MODE_PRIVATE)

        // 저장된 이름 불러오기
        val savedName = sharedPref.getString("user_name", "사용자명")
        userName.text = savedName

        // 정보 공개 상태 불러오기
        val isPublic = sharedPref.getBoolean("info_public", true)
        privacyBtn.text = if (isPublic) "정보 공개 중" else "정보 비공개"
    }
}