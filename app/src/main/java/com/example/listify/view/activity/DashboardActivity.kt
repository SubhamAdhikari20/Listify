package com.example.listify.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentManager
import com.example.listify.R
import com.example.listify.adapter.ViewPagerAdapter
import com.example.listify.databinding.ActivityDashboardBinding
import com.google.android.material.tabs.TabLayoutMediator

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private var icons = arrayOf(
        R.drawable.outline_sticky_note_25,
        R.drawable.outline_check_box_25,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.toolbar.setOnMenuItemClickListener {
           menuItem ->
            when (menuItem.itemId) {
                R.id.myProfileMenuOption -> {
                    val intent = Intent(this@DashboardActivity, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        val fragmentManager : FragmentManager = supportFragmentManager
        viewPagerAdapter = ViewPagerAdapter(fragmentManager, lifecycle)
        binding.viewPager2.adapter = viewPagerAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager2){
//            tabs, position -> tabs.text = data[position]
                tabs, position -> tabs.icon = resources.getDrawable(icons[position], null)
        }.attach()

        binding.floatingActionButton.setOnClickListener {
            val intent = Intent(this, AddNoteActivity::class.java)
            startActivity(intent)
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Inflate the toolbar menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.dashboard_menu_toolbar, menu)
        return true
    }
}