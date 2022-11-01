package com.clausfonseca.rosacha.ui.dashboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.databinding.ActivityDashboardBinding
import com.clausfonseca.rosacha.ui.dashboard.client.ClientFragment
import com.clausfonseca.rosacha.ui.dashboard.price.PriceFragment
import com.clausfonseca.rosacha.ui.dashboard.product.ProductFragment
import com.clausfonseca.rosacha.ui.onboarding.HomeFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

//        val navHostFragment = supportFragmentManager.findFragmentById(R.id.dash_host_fragment)as NavHostFragment
//        navController = navHostFragment.navController
//        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
//        setupWithNavController(bottomNavigationView, navController)

        replaceFragment(HomeFragment())
        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.homeFragment -> replaceFragment(HomeFragment())
                R.id.productFragment -> replaceFragment(ProductFragment())
                R.id.clientFragment -> replaceFragment(ClientFragment())
                R.id.price -> replaceFragment(PriceFragment())
                else -> {}
            }
            true
        }
    }
    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransition = fragmentManager.beginTransaction()
        fragmentTransition.replace(R.id.dash_host_fragment, fragment)
        fragmentTransition.commit()
    }
}