package com.example.listify.model

data class NoteModel(
    var noteId : String = "",
    var noteTitle : String ?= "",
    var noteDesc : String ?= "",
    var noteTime : Long ?= 0
)