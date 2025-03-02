package com.example.listify.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.util.Log
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.listify.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.File
import java.io.InputStream
import java.util.concurrent.Executors

class UserRepositoryImpl : UserRepository {
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var reference = database.reference.child("users")

    override fun login(
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Login Successful")
            }
            else{
                callback(false, it.exception?.message.toString())
            }
        }
    }

    override fun signUp(
        email: String,
        password: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Sign Up Successful.", auth.currentUser?.uid.toString())
            }
            else{
                callback(false, it.exception?.message.toString(), "")
            }
        }
    }

    override fun forgetPassword(
        email: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Password reset link sent to $email")
            }
            else{
                callback(false, it.exception?.message.toString())
            }
        }
    }

    override fun addUserToDatabase(
        userId: String,
        userModel: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        reference.child(userId).setValue(userModel).addOnCompleteListener {
            if(it.isSuccessful){
                callback(true, "Registration Successful")
            }
            else{
                callback(false, it.exception?.message.toString())
            }
        }
    }

    override fun logout(
        callback: (Boolean, String) -> Unit
    ) {
        try {
            auth.signOut()
            callback(true, "Logout success")
        }
        catch(e: Exception) {
            callback(false, e.message.toString())
        }
    }

    override fun deleteAccount(
        userId: String,
        callback: (Boolean, String) -> Unit
    ) {
        val user = auth.currentUser
        user?.delete()?.addOnCompleteListener { authTask ->
            if (authTask.isSuccessful) {
                // Delete from Realtime Database
                reference.child(userId).removeValue()
                    .addOnCompleteListener { dbTask ->
                        if (dbTask.isSuccessful) {
                            auth.signOut()
                            callback(true, "Account deleted successfully")
                        }
                        else {
                            callback(false, "Failed to delete user data: ${dbTask.exception?.message}")
                        }
                    }
            } else {
                callback(false, "Authentication deletion failed: ${authTask.exception?.message}")
            }
        } ?: run {
            callback(false, "No authenticated user found")
        }
    }

    // Authentication Database
    override fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    // Inside your Repository class
    override fun getUserFromDatabase(
        userId: String,
        callback: (UserModel?, Boolean, String) -> Unit
    ) {
        reference.child(userId).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userModel = snapshot.getValue(UserModel::class.java)
                    callback(userModel, true, "Fetched")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null, false, error.message)
            }
        })
    }

    override fun getAllUsers(
        callback: (ArrayList<UserModel>?, Boolean, String) -> Unit
    ) {
        reference.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    var users = arrayListOf<UserModel>()
                    for (eachData in snapshot.children){
                        var userModel = eachData.getValue(UserModel::class.java)
                        if (userModel != null){
                            users.add(userModel)
                        }
                    }

                    callback(users, true, "All posts fetched successfully")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null, true, error.message)
            }

        })
    }

    override fun editProfile(
        userId: String,
        data: MutableMap<String, Any>,
        callback: (Boolean, String) -> Unit
    ) {
        reference.child(userId).updateChildren(data).addOnCompleteListener {
            if (it.isSuccessful){
                callback(true, "Profile updated successfully")
            }
            else{
                callback(false, it.exception?.message.toString())
            }
        }
    }

    override fun uploadImage(
        userId: String,
        data: MutableMap<String, Any>,
        callback: (Boolean, String) -> Unit
    ) {
        reference.child(userId).updateChildren(data).addOnCompleteListener {
            if (it.isSuccessful){
                callback(true, "Profile Picture uploaded")
            }
            else{
                callback(false, it.exception?.message.toString())
            }
        }
    }

    private val cloudinary = Cloudinary(
        mapOf(
            "cloud_name" to "dd6mrii30",
            "api_key" to "515234253369976",
            "api_secret" to "xPCyIe3O10lYncDzKEh9B6TINRg"
        )
    )

    override fun returnImageAsString(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    ) {
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())

        executor.execute {
            try {
                val file = FileUtil.from(context, imageUri)
                val response = cloudinary.uploader().upload(file, ObjectUtils.emptyMap())
                val secureUrl = response["secure_url"]?.toString()

                handler.post {
                    file?.delete() // Clean up temp file
                    callback(secureUrl)
                }
            } catch (e: Exception) {
                handler.post {
                    Log.e("Cloudinary", "Upload failed", e)
                    callback(null)
                }
            }
        }
    }

    object FileUtil {
        fun from(context: Context, uri: Uri): File? {
            val contentResolver = context.contentResolver
            val fileExtension = getFileExtension(context, uri)

            return try {
                val tempFile = File.createTempFile("upload_", ".$fileExtension", context.cacheDir)
                tempFile.outputStream().use { output ->
                    contentResolver.openInputStream(uri)?.use { input ->
                        input.copyTo(output)
                    }
                }
                tempFile
            } catch (e: Exception) {
                null
            }
        }

        private fun getFileExtension(context: Context, uri: Uri): String {
            return context.contentResolver.getType(uri)?.substringAfterLast("/") ?: "jpg"
        }
    }

//    override fun getFileNameFromUri(
//        context: Context, uri: Uri
//    ): String? {
//        var fileName: String? = null
//        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
//        cursor?.use {
//            if (it.moveToFirst()) {
//                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
//                if (nameIndex != -1) {
//                    fileName = it.getString(nameIndex)
//                }
//            }
//        }
//        return fileName
//    }

}