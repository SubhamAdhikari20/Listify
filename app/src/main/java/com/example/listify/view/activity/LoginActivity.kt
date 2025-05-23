package com.example.listify.view.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.listify.R
import com.example.listify.databinding.ActivityLoginBinding
import com.example.listify.repository.UserRepositoryImpl
import com.example.listify.utils.LoadingUtils
import com.example.listify.viewmodel.UserViewModel

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var loadingUtils: LoadingUtils
    private lateinit var sharePreferences : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingUtils = LoadingUtils(this@LoginActivity)
        val userRepo = UserRepositoryImpl()
        userViewModel = UserViewModel(userRepo)
        sharePreferences = getSharedPreferences("user", Context.MODE_PRIVATE)

        // Auto Login
        val editor = sharePreferences
        val savedEmail : String = editor.getString("email", null).toString()
        val savedPassword : String = editor.getString("password", null).toString()

        if (!savedEmail.isNullOrEmpty() && !savedPassword.isNullOrEmpty()) {
            autoLogin(savedEmail, savedPassword)
        }

        binding.loginButton.setOnClickListener {
            loadingUtils.show()
            val email = binding.emailInputText.text.toString()
            val password = binding.passwordInputText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()){
                userViewModel.login(email, password){
                    success, message ->
                    if (success) {
                        loadingUtils.dismiss()

                        if (binding.rememberMe.isChecked){
                            val editor = sharePreferences.edit()
                            editor.putString("email", email)
                            editor.putString("password", password)
                            editor.apply()

                            val intent = Intent(
                                this@LoginActivity, DashboardActivity::class.java
                            )
                            intent.putExtra("email", email)
                            intent.putExtra("password", password)
                            startActivity(intent)
                            finish()
                        }
                        else {
                            // Clear credentials if "Remember Me" is unchecked
                            sharePreferences.edit().clear().apply()
                        }

                        Toast.makeText(
                            this@LoginActivity,
                            message,
                            Toast.LENGTH_LONG
                        ).show()

                        val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    else{
                        loadingUtils.dismiss()
                        Toast.makeText(
                            this@LoginActivity,
                            message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            else{
                loadingUtils.dismiss()
                Toast.makeText(
                    this@LoginActivity,
                    "Invalid, Please fill in both fields!!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        binding.signInButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.forgotPasswordTextView.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun autoLogin(email: String, password: String) {
        loadingUtils.show()
        userViewModel.login(email, password) { success, message ->
            loadingUtils.dismiss()
            if (success) {
                startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                finish()
            }
            else {
                // Clear invalid credentials
                sharePreferences.edit().clear().apply()
//                Toast.makeText(
//                    this@LoginActivity,
//                    "Auto-login failed: $message",
//                    Toast.LENGTH_LONG
//                ).show()
            }
        }
    }


}