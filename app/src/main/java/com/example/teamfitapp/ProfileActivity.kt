package com.example.teamfitapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity() {
    private lateinit var nicknameInput: EditText
    private lateinit var introInput: EditText
    private lateinit var nextButton: ImageButton
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var profileImageView: ImageView
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        nicknameInput = findViewById(R.id.nicknameEditText)
        introInput = findViewById(R.id.introEditText)
        nextButton = findViewById(R.id.nextButton)
        profileImageView = findViewById(R.id.profileImageView)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val answers = intent.getStringArrayListExtra("answers") ?: listOf()

        profileImageView.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        nextButton.setOnClickListener {
            val nickname = nicknameInput.text.toString().trim()
            val intro = introInput.text.toString().trim()
            val uid = auth.currentUser?.uid

            if (uid == null) {
                Toast.makeText(this, "사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (nickname.isEmpty() || intro.isEmpty()) {
                Toast.makeText(this, "닉네임과 자기소개를 모두 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("profileImages/$uid.jpg")

            if (selectedImageUri != null) {
                // 이미지 선택됨 → 업로드 후 URL 저장
                imageRef.putFile(selectedImageUri!!)
                    .continueWithTask { task ->
                        if (!task.isSuccessful) {
                            task.exception?.let { throw it }
                        }
                        imageRef.downloadUrl
                    }
                    .addOnSuccessListener { downloadUri ->
                        val imageUrl = downloadUri.toString()
                        saveProfileData(nickname, intro, imageUrl, answers)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "이미지 업로드 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // 이미지 미선택 → 기본 이미지 or null
                val defaultUrl = "https://teamfitapp.com/default-profile.jpg" // Optional
                saveProfileData(nickname, intro, defaultUrl, answers)
            }
        }
    }

    private fun saveProfileData(nickname: String, intro: String, imageUrl: String, answers: List<String>) {
        val uid = auth.currentUser?.uid ?: return
        val userData = hashMapOf(
            "email" to auth.currentUser?.email,
            "nickname" to nickname,
            "intro" to intro,
            "keywords" to answers,
            "profileImageUrl" to imageUrl
        )

        firestore.collection("users").document(uid).set(userData)
            .addOnSuccessListener {
                val intent = Intent(this, SkillActivity::class.java)
                intent.putStringArrayListExtra("answers", ArrayList(answers))
                intent.putExtra("nickname", nickname)
                intent.putExtra("intro", intro)
                intent.putExtra("profileImageUrl", imageUrl)
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "회원 정보 저장 실패: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            profileImageView.setImageURI(it)
        }
    }
}