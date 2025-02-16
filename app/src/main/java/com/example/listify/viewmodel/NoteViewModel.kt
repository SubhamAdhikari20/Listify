package com.example.listify.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.listify.model.NoteModel
import com.example.listify.repository.NoteRepository

class NoteViewModel(private val noteRepo: NoteRepository)  {
    fun addNote(
        noteModel: NoteModel,
        callback: (Boolean, String) -> Unit
    ) {
        noteRepo.addNote(noteModel, callback)
    }

    fun updateNote(
        noteId: String,
        data: MutableMap<String, Any>,
        callback: (Boolean, String) -> Unit
    ) {
        noteRepo.updateNote(noteId, data, callback)
    }

    fun deleteNote(
        noteId: String,
        callback: (Boolean, String) -> Unit
    ) {
        noteRepo.deleteNote(noteId, callback)
    }


    var _notes = MutableLiveData<NoteModel?>()
    var notes = MutableLiveData<NoteModel?>()
        get() = _notes

    var _loadingNoteById = MutableLiveData<Boolean>()
    var loadingNoteById = MutableLiveData<Boolean>()
        get() = _loadingNoteById


    fun getNoteById(
        noteId: String,
    ) {
        _loadingNoteById.value = true
        noteRepo.getNoteById(noteId) { notes, success, message ->
            if (success) {
                _notes.value = notes
                _loadingNoteById.value = false
            }
        }
    }


    var _getAllnotes = MutableLiveData<ArrayList<NoteModel>>()
    var getAllnotes = MutableLiveData<ArrayList<NoteModel>>()
        get() = _getAllnotes

    var _loadingAllNotes = MutableLiveData<Boolean>()
    var loadingAllNotes = MutableLiveData<Boolean>()
        get() = _loadingAllNotes

    fun getAllNote(
        userId: String
    ) {
        _loadingAllNotes.value = true
        noteRepo.getAllNote(userId) { notes, success, message ->
            if (success) {
                Log.d("checkpoint", "i am here")
                _getAllnotes.value = notes
                _loadingAllNotes.value = false
            }
        }
    }

    fun clearUserData() {
        _getAllnotes.postValue(null)
    }
}