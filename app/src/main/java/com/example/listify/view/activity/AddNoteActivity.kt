package com.example.listify.view.activity

import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.listify.R
import com.example.listify.databinding.ActivityAddNoteBinding
import com.example.listify.model.NoteModel
import com.example.listify.repository.NoteRepositoryImpl
import com.example.listify.utils.LoadingUtils
import com.example.listify.viewmodel.NoteViewModel

class AddNoteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddNoteBinding
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var loadingUtils: LoadingUtils
    private lateinit var main : ConstraintLayout
//    var userId : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.back_arrow)

        loadingUtils = LoadingUtils(this@AddNoteActivity)
        val noteRepo = NoteRepositoryImpl()
        noteViewModel = NoteViewModel(noteRepo)

        var noteId = intent.getStringExtra("noteId") ?: ""
        var userId = intent.getStringExtra("userId") ?: ""
        var noteTitle = intent.getStringExtra("noteTitle") ?: ""
        var noteDesc = intent.getStringExtra("noteDesc") ?: ""
//        var noteTime = intent.getStringExtra("noteTime") ?: ""

        binding.titleNoteTextField.setText(noteTitle)
        binding.descNoteTextField.setText(noteDesc)


        binding.toolbar.setOnMenuItemClickListener {
            menuItem ->
            when (menuItem.itemId) {
                R.id.addNoteOption -> {
                    if (noteId.isEmpty()){
                        addNote(userId)
                    }
                    else{
                        updateNote(noteId)
                    }
                    true
                }
                else -> false
            }
        }

        binding.floatingActionButton.setOnClickListener {
            if (noteId.isEmpty()){
                addNote(userId)
            }
            else{
                updateNote(noteId)
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addNoteLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_note_menu_toolbar, menu)
        return true
    }

    private fun addNote(userId: String){
        loadingUtils.show()
        var noteTitle = binding.titleNoteTextField.text.toString()
        var noteDesc = binding.descNoteTextField.text.toString()
        var noteModel = NoteModel(noteTitle = noteTitle, userId = userId, noteDesc = noteDesc, noteTime = System.currentTimeMillis())

        noteViewModel.addNote(noteModel){
             success, message ->
            if (success){
                Toast.makeText(
                    this@AddNoteActivity,
                    message,
                    Toast.LENGTH_LONG
                ).show()

                /*
                Snackbar.make(
                    main,
                    message,
                    Snackbar.LENGTH_LONG
                ).setAction("Retry") {

                }.show()
                 */

                finish()
                loadingUtils.dismiss()
            }
            else{
                Toast.makeText(
                    this@AddNoteActivity,
                    message,
                    Toast.LENGTH_LONG
                ).show()
                loadingUtils.dismiss()
            }
        }
    }

    private fun updateNote(noteId: String){
        loadingUtils.show()
        var noteTitle = binding.titleNoteTextField.text.toString()
        var noteDesc = binding.descNoteTextField.text.toString()
        var noteTime = System.currentTimeMillis()

        var updatedData = mutableMapOf<String, Any>()
        updatedData["noteTitle"] = noteTitle
        updatedData["noteDesc"] = noteDesc
        updatedData["noteTime"] = noteTime

        noteViewModel.updateNote(noteId, updatedData){
            success, message ->
            if (success){
                Toast.makeText(
                    this@AddNoteActivity,
                    message,
                    Toast.LENGTH_LONG
                ).show()
                finish()
                loadingUtils.dismiss()
            }
            else{
                Toast.makeText(
                    this@AddNoteActivity,
                    message,
                    Toast.LENGTH_LONG
                ).show()
                loadingUtils.dismiss()
            }
        }

    }


}