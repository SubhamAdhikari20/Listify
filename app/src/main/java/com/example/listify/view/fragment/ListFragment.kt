package com.example.listify.view.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SearchView.OnQueryTextListener
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.listify.R
import com.example.listify.adapter.ListAdapter
import com.example.listify.databinding.FragmentListBinding
import com.example.listify.model.ListModel
import com.example.listify.repository.ListRepositoryImpl
import com.example.listify.repository.UserRepositoryImpl
import com.example.listify.utils.LoadingUtils
import com.example.listify.view.activity.AddListActivity
import com.example.listify.viewmodel.ListViewModel
import com.example.listify.viewmodel.UserViewModel
import java.util.ArrayList
import java.util.Locale


class ListFragment : Fragment() {
    private lateinit var binding: FragmentListBinding
    private lateinit var listAdapter: ListAdapter
    private lateinit var listViewModel: ListViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var loadingUtils: LoadingUtils
    private var listModelList = ArrayList<ListModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingUtils = LoadingUtils(requireActivity())
        val listRepo = ListRepositoryImpl()
        listViewModel = ListViewModel(listRepo)
        val userRepo = UserRepositoryImpl()
        userViewModel = UserViewModel(userRepo)

        val userId : String
        val currentUser = userViewModel.getCurrentUser()
        currentUser.let{
            userId = it?.uid.toString()
        }

        listViewModel.getAllList(userId)
        setupObservers()

//        binding.listsRecyclerView.setHasFixedSize(true)
        binding.listsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        listAdapter = ListAdapter(
            requireContext(),
            listModelList,
            listViewModel,
            onItemClick = { list ->
                val intent = Intent(requireContext(), AddListActivity::class.java).apply {
                    if (userId == list.userId) {
                        putExtra("listId", list.listId)
                        putExtra("userId", userId)
                        putExtra("listName", list.listName)
                        putExtra("listCompleted", list.listCompleted)
                    }
                }
                startActivity(intent)
            },
            onItemLongClick = { list ->
                showDeleteDialog(list.listId, 0)
            }
        )

        binding.listsRecyclerView.adapter = listAdapter

        binding.searchListBar.setOnQueryTextListener(object: OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterListsList(newText)
                return true
            }

        })

        binding.listFloatingActionButton.setOnClickListener {
            val intent = Intent(requireContext(), AddListActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)

        }

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                TODO("Not yet implemented")
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//                var listId = listAdapter.getListId(viewHolder.adapterPosition)
                val position = viewHolder.adapterPosition
                val listId = listAdapter.getListId(position)
                showDeleteDialog(listId, position)
//                showDeleteDialog(listId)
            }

        }).attachToRecyclerView(binding.listsRecyclerView)

    }

    private fun filterListsList(query: String?) {
        val queryText = query?.lowercase(Locale.ROOT) ?: ""
        val filteredListList = ArrayList<ListModel>()

        for (list in listModelList) {
            val listNameMatch = list.listName?.lowercase(Locale.ROOT)?.contains(queryText) ?: false
            if (listNameMatch) {
                filteredListList.add(list)
            }
        }
        listAdapter.setListsFilteredList(filteredListList)
    }

    private fun showDeleteDialog(listId: String, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete List")
            .setMessage("Do you want to delete this list?")
            .setIcon(R.drawable.baseline_delete_25)
            .setPositiveButton("Delete") { dialog, _ ->
                loadingUtils.show()
                listViewModel.deleteList(listId) { success, message ->
                    loadingUtils.dismiss()
                    if (success) {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    } else {
                        // Revert the swipe if deletion fails
                        listAdapter.notifyItemChanged(position)
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                // Revert the swipe action
                listAdapter.notifyItemChanged(position)
                dialog.dismiss()
            }
            .show()
    }

    private fun setupObservers() {
        listViewModel.getAlllists.observe(viewLifecycleOwner) { lists ->
            lists?.let {
                listAdapter.updateData(it)

                if (it.isNotEmpty()) {
                    binding.listsRecyclerView.visibility = View.VISIBLE
                    binding.listsRecyclerView.smoothScrollToPosition(it.size - 1)
                }
                else{
                    binding.listsRecyclerView.visibility = View.GONE
                }
            }
        }

        listViewModel.loadingAllLists.observe(viewLifecycleOwner) { loading ->
            if (loading) {
                binding.progressBarList.visibility = View.VISIBLE
//                binding.listsRecyclerView.visibility = View.GONE
            } else {
                binding.progressBarList.visibility = View.GONE
            }
        }
    }

}