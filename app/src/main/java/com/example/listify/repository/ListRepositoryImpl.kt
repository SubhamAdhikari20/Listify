package com.example.listify.repository

import android.util.Log
import com.example.listify.model.ListModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ListRepositoryImpl : ListRepository {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val reference: DatabaseReference = database.reference.child("lists")
    
    override fun addList(
        listModel: ListModel, 
        callback: (Boolean, String) -> Unit
    ) {
        val listId = reference.push().key.toString()
        listModel.listId = listId
        reference.child(listId).setValue(listModel).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "List added successfully")
            }
            else{
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun updateList(
        listId: String,
        data: MutableMap<String, Any>,
        callback: (Boolean, String) -> Unit
    ) {
        reference.child(listId).updateChildren(data).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "List updated successfully")
            }
            else{
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun deleteList(
        listId: String, 
        callback: (Boolean, String) -> Unit
    ) {
        reference.child(listId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "List deleted successfully")
            }
            else{
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun getListById(
        listId: String,
        callback: (ListModel?, Boolean, String) -> Unit
    ) {
        reference.child(listId).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val listModel = snapshot.getValue(ListModel::class.java)
                    callback(listModel, true, "List fetched")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null, false, error.message)
            }

        })
    }

    override fun getAllList(
        userId: String,
        callback: (ArrayList<ListModel>?, Boolean, String) -> Unit
    ) {
        reference.orderByChild("userId").equalTo(userId).addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    var lists = arrayListOf<ListModel>()
                    for(eachData in snapshot.children){
                        try {
                            var listModel = eachData.getValue(ListModel::class.java)
                            if (listModel != null) {
                                lists.add(listModel)
                            }
                        }
                        catch (e: Exception) {
                            Log.e("GetLists", "Error parsing list: ${e.message}")
                            callback(null, false, "${e.message}")
                        }
                    }
                    callback(lists, true, "List fetched")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null, false, error.message)
            }

        })
    }
}