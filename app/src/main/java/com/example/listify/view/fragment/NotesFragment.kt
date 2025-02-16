package com.example.listify.view.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.listify.R
import com.example.listify.adapter.NoteAdapter
import com.example.listify.databinding.FragmentNotesBinding
import com.example.listify.repository.NoteRepositoryImpl
import com.example.listify.repository.UserRepositoryImpl
import com.example.listify.view.activity.AddNoteActivity
import com.example.listify.viewmodel.NoteViewModel
import com.example.listify.viewmodel.UserViewModel
import java.util.ArrayList


class NotesFragment : Fragment() {
    private lateinit var binding: FragmentNotesBinding
    private lateinit var noteAdapter: NoteAdapter
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNotesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val noteRepo = NoteRepositoryImpl()
        noteViewModel = NoteViewModel(noteRepo)
        noteAdapter = NoteAdapter(requireContext(), ArrayList())
        val userRepo = UserRepositoryImpl()
        userViewModel = UserViewModel(userRepo)

        val userId : String
        val currentUser = userViewModel.getCurrentUser()
        currentUser.let{
            userId = it?.uid.toString()
        }

        noteViewModel.getAllNote(userId)
        noteViewModel.getAllnotes.observe(requireActivity()){
            it?.let{
                noteAdapter.updateData(it)
            }
        }

        noteViewModel.loadingAllNotes.observe(requireActivity()){loading ->
            if (loading) {
                binding.progressBarProduct.visibility = View.VISIBLE
            }
            else{
                binding.progressBarProduct.visibility = View.GONE
            }
        }

        binding.notesRecyclerView.adapter = noteAdapter
        binding.notesRecyclerView.layoutManager = LinearLayoutManager(requireContext())


        binding.floatingActionButton.setOnClickListener {
            val intent = Intent(requireContext(), AddNoteActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }
    }


}