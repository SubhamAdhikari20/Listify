package com.example.listify.repository

import android.util.Log
import com.example.listify.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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
                callback(true, "Profile edited")
            }
            else{
                callback(false, it.exception?.message.toString())
            }
        }
    }
}