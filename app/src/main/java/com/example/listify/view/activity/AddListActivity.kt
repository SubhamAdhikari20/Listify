package com.example.listify.view.activity

import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.listify.R
import com.example.listify.databinding.ActivityAddListBinding
import com.example.listify.model.ListModel
import com.example.listify.repository.ListRepositoryImpl
import com.example.listify.utils.LoadingUtils
import com.example.listify.viewmodel.ListViewModel

class AddListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddListBinding
    private lateinit var listViewModel: ListViewModel
    private lateinit var loadingUtils: LoadingUtils
    
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        binding = ActivityAddListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.back_arrow)

        loadingUtils = LoadingUtils(this@AddListActivity)
        val listRepo = ListRepositoryImpl()
        listViewModel = ListViewModel(listRepo)

        var listId = intent.getStringExtra("listId") ?: ""
        var userId = intent.getStringExtra("userId") ?: ""
        var listName = intent.getStringExtra("listName") ?: ""
        

        binding.listNameTextField.setText(listName)

        binding.toolbar.setOnMenuItemClickListener {
                menuItem ->
            when (menuItem.itemId) {
                R.id.addNoteOption -> {
                    if (listId.isEmpty()){
                        addList(userId)
                    }
                    else{
                        updateList(listId)
                    }
                    true
                }
                else -> false
            }
        }

        binding.floatingActionButton.setOnClickListener {
            if (listId.isEmpty()){
                addList(userId)
            }
            else{
                updateList(listId)
            }
        }
        
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addListLayout)) { v, insets ->
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

    private fun addList(userId: String){
        loadingUtils.show()
        var listName = binding.listNameTextField.text.toString()
        var listModel = ListModel(listName = listName, userId = userId, listTime = System.currentTimeMillis())

        listViewModel.addList(listModel){
                success, message ->
            if (success){
                Toast.makeText(
                    this@AddListActivity,
                    message,
                    Toast.LENGTH_LONG
                ).show()
                finish()
                loadingUtils.dismiss()
            }
            else{
                Toast.makeText(
                    this@AddListActivity,
                    message,
                    Toast.LENGTH_LONG
                ).show()
                loadingUtils.dismiss()
            }
        }
    }

    private fun updateList(listId: String){
        loadingUtils.show()
        var listName = binding.listNameTextField.text.toString()
        var listTime = System.currentTimeMillis()
        var listCompleted = intent.getStringExtra("listCompleted") ?: ""

        var updatedData = mutableMapOf<String, Any>()
        updatedData["listName"] = listName
        updatedData["listTime"] = listTime
        updatedData["listCompleted"] = listCompleted.toBoolean()

        listViewModel.updateList(listId, updatedData){
            success, message ->
            if (success){
                Toast.makeText(
                    this@AddListActivity,
                    message,
                    Toast.LENGTH_LONG
                ).show()
                finish()
                loadingUtils.dismiss()
            }
            else{
                Toast.makeText(
                    this@AddListActivity,
                    message,
                    Toast.LENGTH_LONG
                ).show()
                loadingUtils.dismiss()
            }
        }

    }
}