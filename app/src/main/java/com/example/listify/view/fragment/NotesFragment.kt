package com.example.listify.view.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SearchView.OnQueryTextListener
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.listify.R
import com.example.listify.adapter.NoteAdapter
import com.example.listify.databinding.FragmentNotesBinding
import com.example.listify.model.NoteModel
import com.example.listify.repository.NoteRepositoryImpl
import com.example.listify.repository.UserRepositoryImpl
import com.example.listify.utils.LoadingUtils
import com.example.listify.view.activity.AddNoteActivity
import com.example.listify.viewmodel.NoteViewModel
import com.example.listify.viewmodel.UserViewModel
import java.util.ArrayList
import java.util.Locale

class NotesFragment : Fragment() {
    private lateinit var binding: FragmentNotesBinding
    private lateinit var noteAdapter: NoteAdapter
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var loadingUtils: LoadingUtils
    private var noteModelList = ArrayList<NoteModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingUtils = LoadingUtils(requireActivity())
        val noteRepo = NoteRepositoryImpl()
        noteViewModel = NoteViewModel(noteRepo)
        val userRepo = UserRepositoryImpl()
        userViewModel = UserViewModel(userRepo)

        val userId : String
        val currentUser = userViewModel.getCurrentUser()
        currentUser.let{
            userId = it?.uid.toString()
        }

//        if (userId.isNotEmpty()){
//            Log.d("userid", userId)
//        }

        noteViewModel.getAllNote(userId)
        setupObservers()

        binding.notesRecyclerView.setHasFixedSize(true)
        binding.notesRecyclerView.layoutManager = StaggeredGridLayoutManager(2, LinearLayout.VERTICAL)
//        binding.notesRecyclerView.layoutManager = LinearLayoutManager(requireContext())


        noteAdapter = NoteAdapter(
            requireContext(),
            noteModelList,
            onItemClick = { note ->
                val intent = Intent(requireContext(), AddNoteActivity::class.java).apply {
                    if (userId == note.userId) {
                        putExtra("noteId", note.noteId)
                        putExtra("userId", userId)
                        putExtra("noteTitle", note.noteTitle)
                        putExtra("noteDesc", note.noteDesc)
                        putExtra("noteTime", note.noteTime)
                    }
                }
                startActivity(intent)
            },
            onItemLongClick = { note ->
                showDeleteDialog(note)
            }
        )

        binding.notesRecyclerView.adapter = noteAdapter


        binding.searchNotesBar.setOnQueryTextListener(object: OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterNotesList(newText)
                return true
            }

        })

        binding.floatingActionButton.setOnClickListener {
            val intent = Intent(requireContext(), AddNoteActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }
    }

    private fun filterNotesList(query: String?) {
        val queryText = query?.lowercase(Locale.ROOT) ?: ""
        val filteredNoteList = ArrayList<NoteModel>()

        for (note in noteModelList) {
            val titleMatch = note.noteTitle?.lowercase(Locale.ROOT)?.contains(queryText) ?: false
            val descMatch = note.noteDesc?.lowercase(Locale.ROOT)?.contains(queryText) ?: false

            if (titleMatch || descMatch) {
                filteredNoteList.add(note)
            }
        }

        noteAdapter.setNotesFilteredList(filteredNoteList)
//        if (filteredNoteList.isEmpty() && queryText.isNotEmpty()) {
//            Toast.makeText(context, "No matching notes", Toast.LENGTH_SHORT).show()
//        }
    }

    private fun showDeleteDialog(note: NoteModel) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Note")
            .setMessage("Do you want to delete this note?")
            .setIcon(R.drawable.baseline_delete_25) // Add your delete icon
            .setPositiveButton("Delete") { dialog, _ ->
                loadingUtils.show()
                noteViewModel.deleteNote(note.noteId){
                    success, message ->
                    if (success){
                        loadingUtils.dismiss()
                        dialog.dismiss()
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                    else{
                        loadingUtils.dismiss()
                        dialog.dismiss()
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun setupObservers() {
        noteViewModel.getAllnotes.observe(requireActivity()){
            it?.let {
                noteAdapter.updateData(it)
            }
            binding.notesRecyclerView.smoothScrollToPosition(noteModelList.size - 1)
        }

        noteViewModel.loadingAllNotes.observe(requireActivity()){loading ->
            if (loading) {
                binding.progressBarProduct.visibility = View.VISIBLE
            }
            else{
                binding.progressBarProduct.visibility = View.GONE
            }
        }
    }
}