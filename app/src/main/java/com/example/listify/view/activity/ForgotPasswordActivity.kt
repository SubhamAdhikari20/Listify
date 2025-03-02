package com.example.listify.view.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.listify.R
import com.example.listify.databinding.ActivityForgotPasswordBinding
import com.example.listify.repository.UserRepositoryImpl
import com.example.listify.utils.LoadingUtils
import com.example.listify.viewmodel.UserViewModel

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var loadingUtils: LoadingUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.back_arrow)

        loadingUtils = LoadingUtils(this@ForgotPasswordActivity)
        val userRepo = UserRepositoryImpl()
        userViewModel = UserViewModel(userRepo)

        binding.resetPasswordButton.setOnClickListener {
            loadingUtils.show()
            val email = binding.emailForgotPasswordInputText.text.toString()

            if (email.isNotEmpty()){
                userViewModel.forgetPassword(email){
                    success, message ->
                    if (success){
                        loadingUtils.dismiss()
                        Toast.makeText(
                            this@ForgotPasswordActivity,
                            message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    else{
                        loadingUtils.dismiss()
                        Toast.makeText(
                            this@ForgotPasswordActivity,
                            message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            else{
                loadingUtils.dismiss()
                Toast.makeText(
                    this@ForgotPasswordActivity,
                    "Please, fill in the email fields!!!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.forgotPasswordLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

}