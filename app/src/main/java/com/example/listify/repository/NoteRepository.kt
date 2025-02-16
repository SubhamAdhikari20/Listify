package com.example.listify.repository

import com.example.listify.model.NoteModel

interface NoteRepository {

    fun addNote(
        noteModel: NoteModel,
        callback: (Boolean, String) -> Unit
    )

    fun updateNote(
        noteId: String,
        data: MutableMap<String, Any>,
        callback: (Boolean, String) -> Unit
    )

    fun deleteNote(
        noteId: String,
        callback: (Boolean, String) -> Unit
    )

    fun getNoteById(
        noteId: String,
        callback: (NoteModel?, Boolean, String) -> Unit
    )

    fun getAllNote(
        userId: String,
        callback: (ArrayList<NoteModel>?, Boolean, String) -> Unit
    )
}