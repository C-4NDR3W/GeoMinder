package com.example.geominder

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.SearchView
import android.widget.ToggleButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController // Declare navController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Set up BottomNavigationView with NavController
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        NavigationUI.setupWithNavController(bottomNavigationView, navController)

        val switchButton = findViewById<ImageButton>(R.id.switchView)

        var isNoteView = true //default state
//mekanisme ganti state belom
        switchButton.setOnClickListener{
            if (isNoteView){
                switchButton.setImageResource(R.drawable.baseline_map_24)
                navController.navigate(R.id.noteViewFragment)
            } else {
                switchButton.setImageResource(R.drawable.baseline_note_24)
                //navigate to map
            }
        }


        findViewById<FloatingActionButton>(R.id.fab_center).setOnClickListener { item ->
            // Perform navigation or any other action when the FAB is clicked
            when (item.id){
                R.id.navigation_note_creator -> {
                    navController.navigate(R.id.noteCreatorFragment)
                }
                R.id.navigation_notifications -> {
                    navController.navigate(R.id.navigation_notifications)
                }
                R.id.navigation_profile -> {
                    navController.navigate(R.id.profileFragment)
                }
            }
        }
    }
}