package com.example.listify.viewmodel

import androidx.lifecycle.MutableLiveData
import com.example.listify.model.ListModel
import com.example.listify.repository.ListRepository

class ListViewModel(private val listRepo: ListRepository) {
    fun addList(
        listModel: ListModel,
        callback: (Boolean, String) -> Unit
    ) {
        listRepo.addList(listModel, callback)
    }

    fun updateList(
        listId: String,
        data: MutableMap<String, Any>,
        callback: (Boolean, String) -> Unit
    ) {
        listRepo.updateList(listId, data, callback)
    }

    fun deleteList(
        listId: String,
        callback: (Boolean, String) -> Unit
    ) {
        listRepo.deleteList(listId, callback)
    }


    var _lists = MutableLiveData<ListModel?>()
    var lists = MutableLiveData<ListModel?>()
        get() = _lists

    var _loadingListById = MutableLiveData<Boolean>()
    var loadingListById = MutableLiveData<Boolean>()
        get() = _loadingListById


    fun getListById(
        listId: String,
    ) {
        _loadingListById.value = true
        listRepo.getListById(listId) { lists, success, message ->
            if (success) {
                _lists.value = lists
                _loadingListById.value = false
            }
        }
    }


    var _getAlllists = MutableLiveData<ArrayList<ListModel>>()
    var getAlllists = MutableLiveData<ArrayList<ListModel>>()
        get() = _getAlllists

    var _loadingAllLists = MutableLiveData<Boolean>()
    var loadingAllLists = MutableLiveData<Boolean>()
        get() = _loadingAllLists

    fun getAllList(
        userId: String
    ) {
        _loadingAllLists.value = true
        listRepo.getAllList(userId) { lists, success, message ->
            if (success) {
                _getAlllists.value = lists ?: ArrayList()
                _loadingAllLists.value = false
            }
            else{
                _getAlllists.value = ArrayList()
                _loadingAllLists.value = false
            }
        }
    }

    fun clearUserData() {
        _getAlllists.postValue(null)
    }
}