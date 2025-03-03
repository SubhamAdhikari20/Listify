package com.example.listify.view.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.listify.R
import com.example.listify.databinding.ActivityProfileBinding
import com.example.listify.repository.UserRepositoryImpl
import com.example.listify.utils.LoadingUtils
import com.example.listify.viewmodel.UserViewModel
import java.io.ByteArrayOutputStream

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var loadingUtils: LoadingUtils
    private lateinit var sharePreferences : SharedPreferences
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>

    private var selectedImageUri: Uri? = null
    private var profilePic : String = ""
    private var cameraImageUri: Uri? = null

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
            if (!user?.profilePicture.isNullOrEmpty()) {
                Glide.with(this@ProfileActivity)
                    .load(user?.profilePicture)
                    .circleCrop()
                    .into(binding.profileImage)

                profilePic = user?.profilePicture.toString()
            }
            else{
                Glide.with(this@ProfileActivity)
                    .load("https://res.cloudinary.com/dd6mrii30/image/upload/v1740941813/profile-picture_jopwcf.png")
                    .circleCrop()
                    .into(binding.profileImage)
            }
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

        // Initialize the image picker launcher
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                binding.profileImage.setImageURI(it)
                selectedImageUri = uri
//                binding.profileImage.visibility = View.VISIBLE
            }
        }

        // Initialize the camera launcher
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                cameraImageUri?.let { uri ->
                    binding.profileImage.setImageURI(uri)
                    selectedImageUri = uri
                } ?: run {
                    // Fallback to bitmap if needed
                    val imageBitmap = result.data?.extras?.get("data") as? Bitmap
                    imageBitmap?.let {
                        binding.profileImage.setImageBitmap(it)
                        selectedImageUri = getImageUriFromBitmap(it)
                    }
                }
            }
        }

        // Initialize the permissions launcher
        permissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.all { it.value }
            if (allGranted) {
                openImagePickerDialog()
            } else {
                Toast.makeText(this, "Permissions required to proceed!", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle "Select Image" button click
        binding.profileImage.setOnClickListener {
            checkPermissionsAndOpenPicker()
        }

        binding.selectImageBtn.setOnClickListener {
            checkPermissionsAndOpenPicker()
        }

        binding.uploadImageBtn.setOnClickListener {
//            uploadImage(userId)
            showUploadConfirmationDialog(userId)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.myProfileLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            contentResolver,
            bitmap,
            "ProfileImage",
            null
        )
        return Uri.parse(path)
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

    private fun uploadImage(userId: String) {
        loadingUtils.show()
        val updatedData = mutableMapOf<String, Any>()

        selectedImageUri?.let { uri ->
            userViewModel.returnImageAsString(this@ProfileActivity, uri) { imageUrl ->
                if (imageUrl != null) {
                    updatedData["profilePicture"] = imageUrl
                    userViewModel.uploadImage(userId, updatedData) { success, message ->
                        loadingUtils.dismiss()
                        if (success) {
                            // Refresh user data after successful upload
                            userViewModel.getUserFromDatabase(userId)
                            selectedImageUri = null
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Upload failed: $message", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    loadingUtils.dismiss()
                    Toast.makeText(this, "Image processing failed", Toast.LENGTH_SHORT).show()
                }
            }
        } ?: run {
            loadingUtils.dismiss()
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showUploadConfirmationDialog(userId: String) {
        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Confirm Upload")
            .setMessage("Are you sure you want to update your profile picture?")
            .setPositiveButton("Upload") { dialog, _ ->
                uploadImage(userId)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                selectedImageUri = null
                Glide.with(this@ProfileActivity)
                    .load(profilePic)
                    .circleCrop()
                    .into(binding.profileImage)
                dialog.dismiss()
            }
            .show()
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
                dialog.dismiss()
            }
            .show()
    }

    // Function to check and request permissions
    private fun checkPermissionsAndOpenPicker() {
        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        }
        else {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }

        if (requiredPermissions.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) {
            openImagePickerDialog()
        }
        else {
            permissionsLauncher.launch(requiredPermissions)
        }
    }

    private fun openImagePickerDialog() {
        val options = arrayOf("Choose from Gallery", "Take a Photo")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Image")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> { // Open Gallery
                    imagePickerLauncher.launch("image/*")
                }
                1 -> { // Open Camera
                    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    if (takePictureIntent.resolveActivity(packageManager) != null) {
                        cameraLauncher.launch(takePictureIntent)
                    } else {
                        Toast.makeText(this, "Camera not available!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        builder.show()
    }

}