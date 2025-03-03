package com.example.listify.model

data class ListModel(
    var listId : String = "",
    var userId : String = "",
    var listName : String ?= "",
    var listCompleted: Boolean? = false,
    var listTime : Long ?= 0
)