package com.example.listify.view.activity

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Note
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.listify.R
import com.example.listify.databinding.ActivityProfileBinding
import com.example.listify.repository.NoteRepositoryImpl
import com.example.listify.repository.UserRepositoryImpl
import com.example.listify.utils.LoadingUtils
import com.example.listify.viewmodel.NoteViewModel
import com.example.listify.viewmodel.UserViewModel

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var loadingUtils: LoadingUtils
    private lateinit var sharePreferences : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.back_arrow)

        loadingUtils = LoadingUtils(this@ProfileActivity)
        val userRepo = UserRepositoryImpl()
        userViewModel = UserViewModel(userRepo)
        userViewModel.clearUserData()

        val userId : String
        val currentUser = userViewModel.getCurrentUser()
        currentUser.let{    // it -> currentUser
            Log.d("current user userId",it?.uid.toString())
            userViewModel.getUserFromDatabase(it?.uid.toString())
            userId = it?.uid.toString()
        }

        userViewModel.userData.observe(this@ProfileActivity){
            user ->
            binding.nameTextView.text = user?.fullName.toString()
            binding.emailTextView.text = user?.email.toString()
            binding.passwordTextView.text = user?.password.toString()
            binding.contactTextView.text = user?.contact.toString()
        }

        binding.nameProfileCardView.setOnClickListener {
            openEditProfileDetail(userId, "Name", binding.nameTextView.text.toString())
        }

        binding.emailProfileCardView.setOnClickListener {
            openEditProfileDetail(userId, "Email", binding.emailTextView.text.toString())
        }

        binding.passwordProfileCardView.setOnClickListener {
            openEditProfileDetail(userId, "Password", binding.passwordTextView.text.toString())
        }

        binding.contactProfileCardView.setOnClickListener {
            openEditProfileDetail(userId, "Contact", binding.contactTextView.text.toString())
        }

        binding.logoutBtn.setOnClickListener {
            confirmLogoutDialog()
        }

        binding.deleteAccountBtn.setOnClickListener {
            confirmDeleteAccountDialog(userId)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.myProfileLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun openEditProfileDetail(userId: String, fieldType: String, fieldValue: String){
        val intent = Intent(this, EditProfileDetailsActivity::class.java)
        intent.putExtra("userId", userId)
        intent.putExtra("FIELD_TYPE", fieldType)
        intent.putExtra("FIELD_VALUE", fieldValue)
        startActivity(intent)
    }

    private fun confirmLogoutDialog(){
        AlertDialog.Builder(this@ProfileActivity)
            .setTitle("Confirm Logout")
            .setMessage("Do you want to logout?")
            .setPositiveButton("Logout") { dialog, _ ->
                loadingUtils.show()
                userViewModel.logout {
                    success, message ->
                    if (success){
                        loadingUtils.dismiss()
                        dialog.dismiss()
                        Toast.makeText(
                            this@ProfileActivity,
                            message,
                            Toast.LENGTH_SHORT
                        ).show()

                        sharePreferences = getSharedPreferences("user", Context.MODE_PRIVATE)
                        sharePreferences.edit().clear().apply()
                        val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    else{
                        loadingUtils.dismiss()
                        dialog.dismiss()
                        Toast.makeText(
                            this@ProfileActivity,
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                loadingUtils.dismiss()
                dialog.dismiss()
            }
            .show()
    }

    private fun confirmDeleteAccountDialog(userId: String){
        AlertDialog.Builder(this@ProfileActivity)
            .setTitle("Confirm Delete Account")
            .setMessage("Are you sure you want to delete your account?")
            .setPositiveButton("Delete") { dialog, _ ->
                loadingUtils.show()
                userViewModel.deleteAccount(userId) {
                    success, message ->
                    if (success){
                        loadingUtils.show()
                        dialog.dismiss()
                        Toast.makeText(
                            this@ProfileActivity,
                            message,
                            Toast.LENGTH_SHORT
                        ).show()

                        sharePreferences = getSharedPreferences("user", Context.MODE_PRIVATE)
                        sharePreferences.edit().clear().apply()
                        val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    else{
                        loadingUtils.dismiss()
                        dialog.dismiss()
                        Toast.makeText(
                            this@ProfileActivity,
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                loadingUtils.dismiss()
                dialog.dismiss()
            }
            .show()
    }

}