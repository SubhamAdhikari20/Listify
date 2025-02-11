package com.example.listify.model

data class UserModel(
    var userId: String = "",
    var fullName:  String = "",
    var email: String = "",
    var password:  String = "",
    var profilePicture: String ?= null,
)