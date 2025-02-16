package com.example.listify.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.listify.R
import com.example.listify.model.NoteModel

class NoteAdapter(
    var context: Context,
    private var data: ArrayList<NoteModel>
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>()  {
    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var noteTitle : TextView = itemView.findViewById(R.id.titleNoteTextView)
        var noteDesc : TextView = itemView.findViewById(R.id.descNoteTextView)
        var noteTime : TextView = itemView.findViewById(R.id.timeNoteTextView)
//        val loading : ProgressBar = itemView.findViewById(R.id.progressBarProduct)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView : View = LayoutInflater.from(context).inflate(R.layout.sample_note_card_design, parent, false)
        return NoteViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        // Feed the data from database
        holder.noteTitle.text = data[position].noteTitle
        holder.noteDesc.text = data[position].noteDesc.toString()
        holder.noteTime.text = data[position].noteDesc
    }

    fun updateData(products: ArrayList<NoteModel>){
        data.clear()
        data.addAll(products)
        notifyDataSetChanged()
    }

    // Swipe to delete -> when swiped the noteId is return through index
    fun getProductId(position: Int):String{
        return data[position].noteId
    }
}