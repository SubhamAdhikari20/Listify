package com.example.listify.adapter

import android.content.Context
import android.provider.ContactsContract.CommonDataKinds.Note
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.listify.R
import com.example.listify.model.NoteModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class NoteAdapter(
    var context: Context,
    private var noteModelList: ArrayList<NoteModel>,
    var onItemClick: (NoteModel) -> Unit,
    var onItemLongClick: (NoteModel) -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>()  {
    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var noteTitle : TextView = itemView.findViewById(R.id.titleNoteTextView)
        var noteDesc : TextView = itemView.findViewById(R.id.descNoteTextView)
        var noteTime : TextView = itemView.findViewById(R.id.timeNoteTextView)
        var noteCard : CardView = itemView.findViewById(R.id.noteCard)
//        val loading : ProgressBar = itemView.findViewById(R.id.progressBarProduct)
    }

//    interface NotesClickListener{
//        fun onItemClick(note: NoteModel)
//        fun onItemLongClick(note: NoteModel, cardView: CardView)
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView : View = LayoutInflater.from(context).inflate(R.layout.sample_note_card_design, parent, false)
        return NoteViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return noteModelList.size
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        // Feed the noteModelList from noteModelListDatabase
        val note = noteModelList[position]
        holder.noteTitle.text = note.noteTitle
        holder.noteDesc.text = note.noteDesc.toString()
        holder.noteTime.text = note.noteTime?.let {
            Date(
                it
            )
        }?.let { SimpleDateFormat("EEE, dd-MMMM-yyyy, hh:mm a", Locale.getDefault()).format(it) }

        // Set click listener
        holder.itemView.setOnClickListener {
            onItemClick(note)
        }

        holder.itemView.setOnLongClickListener {
            onItemLongClick(note)
            true
        }

        holder.noteCard.setCardBackgroundColor(holder.itemView.resources.getColor(randomCardColors(), null))
    }

    fun setNotesFilteredList(noteModelList: ArrayList<NoteModel>){
        this.noteModelList = noteModelList
        notifyDataSetChanged()
    }

    fun updateData(notes: ArrayList<NoteModel>){
        noteModelList.clear()
        noteModelList.addAll(notes)
        notifyDataSetChanged()
    }

    // Swipe to delete -> when swiped the noteId is return through index
    fun getNoteId(position: Int):String{
        return noteModelList[position].noteId
    }

    fun randomCardColors(): Int{
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