package com.example.listify.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.listify.model.UserModel
import com.example.listify.repository.UserRepository
import com.google.firebase.auth.FirebaseUser

class UserViewModel(private val userRepo: UserRepository) {

    fun login(
        email:String,
        password:String,
        callback:(Boolean,String) -> Unit
    ){
        userRepo.login(email, password, callback)
    }

    fun signUp(
        email:String,
        password:String,
        callback:(Boolean,String, String) -> Unit
    ){
        userRepo.signUp(email, password, callback)
    }

    fun forgetPassword(
        email:String,
        callback:(Boolean,String) -> Unit
    ){
        userRepo.forgetPassword(email, callback)
    }

    fun addUserToDatabase(
        userId:String,
        userModel: UserModel,
        callback:(Boolean, String) -> Unit
    ){
        userRepo.addUserToDatabase(userId, userModel, callback)
    }

    fun logout(
        callback: (Boolean, String) -> Unit
    ){
        userRepo.logout(callback)
    }

    fun deleteAccount(
        userId: String,
        callback: (Boolean, String) -> Unit
    ){
        userRepo.deleteAccount(userId, callback)
    }

    fun getCurrentUser() : FirebaseUser?{
        return userRepo.getCurrentUser()
    }


    var _userData = MutableLiveData<UserModel?>()
    var userData = MutableLiveData<UserModel?>()
        get() = _userData

    var _loadingUser = MutableLiveData<Boolean?>()
    var loadingUser = MutableLiveData<Boolean?>()
        get() = _loadingUser

    fun getUserFromDatabase(
        userId: String,
    ){
        userRepo.getUserFromDatabase(userId) {
            user, success, message ->
            if (success) {
                // Only post the new value if it's different from the current one
                _userData.value = user
            }
        }
    }


    var _getAllUsersData = MutableLiveData<List<UserModel>?>()
    var getAllUsersData = MutableLiveData<List<UserModel>?>()
        get() = _getAllUsersData

    var _loadingAllUsers = MutableLiveData<Boolean?>()
    var loadingAllUsers = MutableLiveData<Boolean?>()
        get() = _loadingAllUsers

    fun getAllUsers(){
        userRepo.getAllUsers(){
            users, success, message ->
            if (success){
                _getAllUsersData.value = users
                _loadingAllUsers.value = false
            }
        }
    }

    fun editProfile(
        userId: String,
        data: MutableMap<String, Any>,
        callback: (Boolean, String) -> Unit
    ){
        userRepo.editProfile(userId, data, callback)
    }

    fun uploadImage(
        userId: String,
        data: MutableMap<String, Any>,
        callback: (Boolean, String) -> Unit
    ){
        userRepo.uploadImage(userId, data, callback)
    }

    fun returnImageAsString(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    ) {
        userRepo.returnImageAsString(context, imageUri, callback)
    }

    fun clearUserData() {
        _userData.postValue(null)
    }
}