package com.example.listify.view.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.listify.R
import com.example.listify.databinding.ActivitySignUpBinding
import com.example.listify.model.UserModel
import com.example.listify.repository.UserRepositoryImpl
import com.example.listify.utils.LoadingUtils
import com.example.listify.viewmodel.UserViewModel

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var loadingUtils: LoadingUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.back_arrow)

        loadingUtils = LoadingUtils(this)
        val userRepo = UserRepositoryImpl()
        userViewModel = UserViewModel(userRepo)

        binding.signUpBtn.setOnClickListener {
            loadingUtils.show()
            val fullName = binding.nameInputText.text.toString()
            val email = binding.emailInputText.text.toString()
            val password = binding.passwordInputText.text.toString()
            val confirmPassword = binding.confirmPasswordInputText.text.toString()

            if (fullName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()){
                if (password == confirmPassword){
                    userViewModel.signUp(email, password){
                        success, message, userId->
                        if(success) {
                            val userModel = UserModel(userId = userId, fullName = fullName, email = email, password = password)
                            addUser(userModel)
                        }
                        else {
                            loadingUtils.dismiss()
                            Toast.makeText(
                                this@SignUpActivity,
                                message,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
                else{
                    loadingUtils.dismiss()
                    Toast.makeText(
                        this@SignUpActivity,
                        "password and confirm password do not match!!",
                        Toast.LENGTH_LONG
                    ).show()
                }

            }
            else{
                loadingUtils.dismiss()
                Toast.makeText(
                    this@SignUpActivity,
                    "Invalid, enter all the fields!!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        binding.loginBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signUpLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun addUser(userModel: UserModel){
        userViewModel.addUserToDatabase(userModel.userId, userModel){
            success, message ->
            if (success){
                loadingUtils.dismiss()
                Toast.makeText(
                    this@SignUpActivity,
                    "Registration Successful",
                    Toast.LENGTH_LONG
                ).show()
            }
            else{
                loadingUtils.dismiss()
                Toast.makeText(
                    this@SignUpActivity,
                    message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

}