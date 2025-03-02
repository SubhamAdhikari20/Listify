package com.example.listify.repository

import android.content.Context
import android.net.Uri
import com.example.listify.model.UserModel
import com.google.firebase.auth.FirebaseUser

interface UserRepository {

    fun login(
        email:String,
        password:String,
        callback:(Boolean,String) -> Unit
    )

    fun signUp(
        email:String,
        password:String,
        callback:(Boolean,String, String) -> Unit
    )

    fun forgetPassword(
        email:String,
        callback:(Boolean,String) -> Unit
    )

    fun addUserToDatabase(
        userId:String,
        userModel: UserModel,
        callback:(Boolean, String) -> Unit
    )

    fun logout(
        callback: (Boolean, String) -> Unit
    )

    fun deleteAccount(
        userId: String,
        callback: (Boolean, String) -> Unit
    )

    // Authentication Database
    fun getCurrentUser() : FirebaseUser?

    // Real-time Database
    fun getUserFromDatabase(
        userId: String,
        callback: (UserModel?, Boolean, String) -> Unit
    )

    fun getAllUsers(
        callback: (ArrayList<UserModel>?, Boolean, String) -> Unit
    )

    fun editProfile(
        userId: String,
        data: MutableMap<String, Any>,
        callback: (Boolean, String) -> Unit
    )

    fun uploadImage(
        userId: String,
        data: MutableMap<String, Any>,
        callback: (Boolean, String) -> Unit
    )

    fun returnImageAsString(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    )

//    fun getFileNameFromUri(
//        context: Context, uri: Uri
//    ): String?
}