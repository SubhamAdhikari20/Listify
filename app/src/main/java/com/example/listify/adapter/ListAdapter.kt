package com.example.listify.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.listify.R
import com.example.listify.model.ListModel
import com.example.listify.utils.ListDiffUtil
import com.example.listify.viewmodel.ListViewModel
import kotlin.random.Random

class ListAdapter(
    var context: Context,
    private var listModelList: ArrayList<ListModel>,
    private val listViewModel: ListViewModel,
    var onItemClick: (ListModel) -> Unit,
    var onItemLongClick: (ListModel) -> Unit
) : RecyclerView.Adapter<ListAdapter.ListViewHolder>()   {

    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var listName : TextView = itemView.findViewById(R.id.todoListTextView)
        var listCheckBox : CheckBox = itemView.findViewById(R.id.checkBox)
        var listCard : CardView = itemView.findViewById(R.id.todoListCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val itemView : View = LayoutInflater.from(context).inflate(R.layout.sample_list_card_design, parent, false)
        return ListViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return listModelList.size
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        // Feed the listModelList from listModelListDatabase
        val list = listModelList[position]

        // Set list name
        holder.listName.text = list.listName

        // Set checkbox state
        holder.listCheckBox.isChecked = list.listCompleted == true

        // Update background color based on completion state
        val cardColor = if (list.listCompleted == true) {
            R.color.completedTaskColor
        } else {
            randomCardColors()
        }
        holder.listCard.setCardBackgroundColor(context.getColor(cardColor))

        // Checkbox listener
        holder.listCheckBox.setOnCheckedChangeListener { _, isChecked ->
            listViewModel.updateList(list.listId, hashMapOf("listCompleted" to isChecked)) { success, _ ->
                if (success) {
                    // Show appropriate toast message
                    val message = if (isChecked) {
                        "List marked as completed!"
                    } else {
                        "List marked as incomplete!"
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                } else {
                    // Rollback UI state if update fails
                    holder.listCheckBox.isChecked = !isChecked
                }
            }
        }

        // Set click listener
        holder.itemView.setOnClickListener {
            onItemClick(list)
        }

        // Set long click listener
        holder.itemView.setOnLongClickListener {
            onItemLongClick(list)
            true
        }

        holder.listCard.setCardBackgroundColor(holder.itemView.resources.getColor(randomCardColors(), null))
    }

    fun setListsFilteredList(listModelList: ArrayList<ListModel>){
        this.listModelList = listModelList
        notifyDataSetChanged()
    }

    fun updateData(lists: ArrayList<ListModel>){
//        val diffResult = DiffUtil.calculateDiff(ListDiffUtil(listModelList, lists))
        listModelList.clear()
        listModelList.addAll(lists)
//        diffResult.dispatchUpdatesTo(this)
        notifyDataSetChanged()
    }

    // Swipe to delete -> when swiped the listId is return through index
    fun getListId(position: Int):String{
        return listModelList[position].listId
    }
    
    private fun randomCardColors(): Int{
        val colorList = ArrayList<Int>()
        colorList.add(R.color.Color1)
        colorList.add(R.color.Color2)
        colorList.add(R.color.Color3)
        colorList.add(R.color.Color4)
        colorList.add(R.color.Color5)
        colorList.add(R.color.Color6)
        colorList.add(R.color.Color7)

        val seed = System.currentTimeMillis().toInt()
        val randomIndex = Random(seed).nextInt(colorList.size)
        return colorList[randomIndex]
    }
}