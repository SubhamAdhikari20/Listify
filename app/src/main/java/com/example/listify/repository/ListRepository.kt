package com.example.listify.repository

import com.example.listify.model.ListModel

interface ListRepository {
    
    fun addList(
        listModel: ListModel,
        callback: (Boolean, String) -> Unit
    )

    fun updateList(
        listId: String,
        data: MutableMap<String, Any>,
        callback: (Boolean, String) -> Unit
    )

    fun deleteList(
        listId: String,
        callback: (Boolean, String) -> Unit
    )

    fun getListById(
        listId: String,
        callback: (ListModel?, Boolean, String) -> Unit
    )

    fun getAllList(
        userId: String,
        callback: (ArrayList<ListModel>?, Boolean, String) -> Unit
    )
}