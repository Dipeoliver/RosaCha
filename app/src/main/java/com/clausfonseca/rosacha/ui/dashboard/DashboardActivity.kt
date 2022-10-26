package com.clausfonseca.rosacha.ui.dashboard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.databinding.ActivityDashboardBinding
import com.clausfonseca.rosacha.databinding.FragmentAddProductBinding
import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var navController:NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val navController = findNavController(R.id.fragment_dashboard)

//        navController = navHostFragment.findNavController()
        binding.bottomNavigationView.setupWithNavController(navController)
        val appBarConfiguration =
            AppBarConfiguration(setOf(R.id.homeFragment, R.id.productFragment, R.id.clientFragment))
        setupActionBarWithNavController(navController, appBarConfiguration)
    }
}