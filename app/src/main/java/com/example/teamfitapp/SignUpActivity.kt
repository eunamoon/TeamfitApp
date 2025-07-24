package com.example.teamfitapp

import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class SignUpActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var sendEmailButton: ImageButton
    private lateinit var verifyCheckButton: ImageButton
    private lateinit var nextButton: ImageButton

    private var isEmailVerified = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        nameEditText = findViewById(R.id.nameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        phoneEditText = findViewById(R.id.phoneEditText)
        sendEmailButton = findViewById(R.id.sendVerificationEmailButton)
        verifyCheckButton = findViewById(R.id.verifyCheckButton)
        nextButton = findViewById(R.id.nextButton)

        nextButton.isEnabled = false

        sendEmailButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()

            sendEmailButton.setBackgroundResource(R.drawable.emailverifi_done)

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        auth.currentUser?.sendEmailVerification()
                            ?.addOnCompleteListener { verifiTask ->
                                if (verifiTask.isSuccessful) {
                                    Toast.makeText(this, "인증 메일을 보냈습니다. 이메일을 확인해주세요.", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(this, "인증 메일 전송 실패: ${verifiTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        val msg = task.exception?.message ?: "회원 생성 실패"
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    }
                }
        }

        verifyCheckButton.setOnClickListener {
            val user = auth.currentUser
            verifyCheckButton.setBackgroundResource(R.drawable.emailconfirm_done)
            user?.reload()?.addOnSuccessListener {
                if (user.isEmailVerified) {
                    if (!isEmailVerified) {
                        isEmailVerified = true
                        Toast.makeText(this, "✅ 이메일 인증이 완료되었습니다!", Toast.LENGTH_SHORT).show()
                        checkInputConditions()
                    } else {
                        Toast.makeText(this, "이미 인증된 상태입니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "❗ 아직 이메일 인증을 하지 않았습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        nextButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (!isEmailVerified) {
                Toast.makeText(this, "이메일 인증을 완료해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uid = auth.currentUser?.uid ?: return@setOnClickListener

            val userData = hashMapOf(
                "name" to name,
                "email" to email,
                "phone" to phone
            )

            firestore.collection("users")
                .document(uid)
                .set(userData)
                .addOnSuccessListener {
                    Toast.makeText(this, "회원가입 성공!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "회원 정보 저장 실패", Toast.LENGTH_SHORT).show()
                }
        }

        setupInputWatchers()
    }

    private fun setupInputWatchers() {
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkInputConditions()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        nameEditText.addTextChangedListener(watcher)
        emailEditText.addTextChangedListener(watcher)
        passwordEditText.addTextChangedListener(watcher)
        confirmPasswordEditText.addTextChangedListener(watcher)
        phoneEditText.addTextChangedListener(watcher)
    }

    private fun checkInputConditions() {
        val name = nameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val pw = passwordEditText.text.toString()
        val confirmPw = confirmPasswordEditText.text.toString()
        val phone = phoneEditText.text.toString().trim()

        nextButton.isEnabled = isEmailVerified &&
                name.isNotEmpty() &&
                email.isNotEmpty() &&
                pw.isNotEmpty() &&
                confirmPw.isNotEmpty() &&
                pw == confirmPw &&
                phone.isNotEmpty()
    }
}