package com.example.listify.view.activity

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.listify.R
import com.example.listify.databinding.ActivityEditProfileDetailsBinding
import com.example.listify.repository.UserRepositoryImpl
import com.example.listify.utils.LoadingUtils
import com.example.listify.viewmodel.UserViewModel

class EditProfileDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileDetailsBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var loadingUtils: LoadingUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityEditProfileDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.back_arrow)

        loadingUtils = LoadingUtils(this@EditProfileDetailsActivity)
        // Retrieve passed values
        val fieldType = intent.getStringExtra("FIELD_TYPE") ?: ""
        val fieldValue = intent.getStringExtra("FIELD_VALUE") ?: ""
        val userId = intent.getStringExtra("userId") ?: ""

        binding.profileDetailTitleTexView.text = fieldType
        binding.profileDetailEditTextField.hint = fieldType

        if (binding.profileDetailEditTextField.hint.toString().lowercase() == "name"){
            binding.profileDetailEditTextField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PERSON_NAME
        }
        else if (binding.profileDetailEditTextField.hint.toString().lowercase() == "email"){
            binding.profileDetailEditTextField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }
        else if (binding.profileDetailEditTextField.hint.toString().lowercase() == "password"){
            binding.profileDetailEditTextField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        else if (binding.profileDetailEditTextField.hint.toString().lowercase() == "contact"){
            binding.profileDetailEditTextField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_CLASS_PHONE
        }
        else{
            return
        }

        binding.profileDetailEditTextField.setText(fieldValue)

        val userRepo = UserRepositoryImpl()
        userViewModel = UserViewModel(userRepo)
        userViewModel.clearUserData()

        binding.profileDetailsSaveChangesButton.setOnClickListener {
            loadingUtils.show()
            val updatedValue = binding.profileDetailEditTextField.text.toString()
            val mappedFieldType = mapFieldTypeToModel(fieldType)

            // Check if the field is not empty before updating
            if (updatedValue.isNotEmpty()) {
                if (mappedFieldType != null) {
                    // Create a map of the updated data
                    val data = mutableMapOf<String, Any>()
                    data[mappedFieldType] = updatedValue

                    Log.d("message", "$fieldType updated with value: $updatedValue")
                    userViewModel.editProfile(userId, data) {
                        isSuccess, message ->
                        if (isSuccess) {
                            loadingUtils.dismiss()
                            Toast.makeText(
                                this,
                                message,
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                        else {
                            loadingUtils.dismiss()
                            Toast.makeText(
                                this,
                                "Failed to update profile: $message",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                else {
                    loadingUtils.dismiss()
                    Toast.makeText(
                        this,
                        "Please enter a value for $fieldType",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            else{
                loadingUtils.dismiss()
                Toast.makeText(
                    this,
                    "Please, fill the detail",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.editProfileDetailsLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun mapFieldTypeToModel(fieldType: String): String? {
        return when (fieldType.uppercase()) {
            "NAME" -> "fullName"
            "CONTACT" -> "contact"
            "EMAIL" -> "email"
            "PASSWORD" -> "password"
            else -> null
        }
    }
}