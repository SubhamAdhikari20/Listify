package com.example.listify.repository

import android.util.Log
import com.example.listify.model.NoteModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NoteRepositoryImpl : NoteRepository {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val reference: DatabaseReference = database.reference.child("notes")

    override fun addNote(
        noteModel: NoteModel,
        callback: (Boolean, String) -> Unit
    ) {
        val noteId = reference.push().key.toString()
        noteModel.noteId = noteId
        reference.child(noteId).setValue(noteModel).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Note added successfully")
            }
            else{
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun updateNote(
        noteId: String,
        data: MutableMap<String, Any>,
        callback: (Boolean, String) -> Unit
    ) {
        reference.child(noteId).updateChildren(data).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Note updated successfully")
            }
            else{
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun deleteNote(
        noteId: String,
        callback: (Boolean, String) -> Unit
    ) {
        reference.child(noteId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Note deleted successfully")
            }
            else{
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun getNoteById(
        noteId: String,
        callback: (NoteModel?, Boolean, String) -> Unit
    ) {
        reference.child(noteId).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val noteModel = snapshot.getValue(NoteModel::class.java)
                    callback(noteModel, true, "Note fetched")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null, false, error.message)
            }

        })
    }

    override fun getAllNote(
        userId: String,
        callback: (ArrayList<NoteModel>?, Boolean, String) -> Unit
    ) {
        reference.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    var notes = arrayListOf<NoteModel>()
                    for(eachData in snapshot.children){
                        var noteModel = eachData.getValue(NoteModel::class.java)

                        if (noteModel != null) {
                            if (noteModel.userId == userId){
                                notes.add(noteModel)
                            }
                        }

                    }

                    callback(notes, true, "Note fetched")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null, false, error.message)
            }

        })
    }

}