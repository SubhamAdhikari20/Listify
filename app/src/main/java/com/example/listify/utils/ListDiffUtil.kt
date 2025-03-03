package com.example.listify.utils

import androidx.recyclerview.widget.DiffUtil
import com.example.listify.model.ListModel

class ListDiffUtil(
    private val oldList: List<ListModel>,
    private val newList: List<ListModel>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size
    override fun areItemsTheSame(oldPos: Int, newPos: Int) =
        oldList[oldPos].listId == newList[newPos].listId
    override fun areContentsTheSame(oldPos: Int, newPos: Int) =
        oldList[oldPos] == newList[newPos]
}