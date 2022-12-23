package com.clausfonseca.rosacha.view.onboarding

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.databinding.ActivityMainBinding
import com.clausfonseca.rosacha.view.dashboard.product.ProductFragment
import com.clausfonseca.rosacha.view.dashboard.sales.SalesFragment


class RosaChaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configComponents()
    }

    private fun configComponents() {

        var oldItemId = 0
        val navController = findNavController(R.id.nav_host_fragment)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.home_fragment,
                R.id.product_fragment,
                R.id.clientFragment,
                R.id.price_fragment,
                R.id.sales_fragment -> {
                    binding.bottomNavigationView.visibility = View.VISIBLE
                    oldItemId = destination.id
                }
                else -> {
                    binding.bottomNavigationView.visibility = View.GONE
                }
            }
        }
        binding.bottomNavigationView.setupWithNavController(navController)

        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home_fragment -> {
                    when (oldItemId) {
                        R.id.product_fragment -> navController.navigate(R.id.action_product_fragment_to_home_fragment)
                        R.id.clientFragment -> navController.navigate(R.id.action_clientFragment_to_home_fragment)
                        R.id.price_fragment -> navController.navigate(R.id.action_price_fragment_to_home_fragment)
                        R.id.sales_fragment -> navController.navigate(R.id.action_sales_fragment_to_home_fragment)
                    }
                }
                R.id.client_fragment -> {
                    when (oldItemId) {
                        R.id.home_fragment -> navController.navigate(R.id.action_home_fragment_to_clientFragment)
                        R.id.product_fragment -> navController.navigate(R.id.action_product_fragment_to_clientFragment)
                        R.id.price_fragment -> navController.navigate(R.id.action_price_fragment_to_clientFragment)
                        R.id.sales_fragment -> navController.navigate(R.id.action_sales_fragment_to_clientFragment)
                    }
                }
                R.id.price_fragment -> {
                    when (oldItemId) {
                        R.id.home_fragment -> navController.navigate(R.id.action_home_fragment_to_price_fragment)
                        R.id.product_fragment -> navController.navigate(R.id.action_product_fragment_to_price_fragment)
                        R.id.clientFragment -> navController.navigate(R.id.action_clientFragment_to_price_fragment)
                        R.id.sales_fragment -> navController.navigate(R.id.action_sales_fragment_to_price_fragment)
                    }
                }
                R.id.product_fragment -> {
                    when (oldItemId) {
                        R.id.home_fragment -> navController.navigate(R.id.action_home_fragment_to_product_fragment)
                        R.id.price_fragment -> navController.navigate(R.id.action_price_fragment_to_product_fragment2)
                        R.id.clientFragment -> navController.navigate(R.id.action_clientFragment_to_product_fragment)
                        R.id.sales_fragment -> navController.navigate(R.id.action_sales_fragment_to_product_fragment)
                    }
                }
                R.id.sales_fragment -> {
                    when (oldItemId) {
                        R.id.home_fragment -> navController.navigate(R.id.action_home_fragment_to_sales_fragment)
                        R.id.product_fragment -> navController.navigate(R.id.action_product_fragment_to_sales_fragment)
                        R.id.price_fragment -> navController.navigate(R.id.action_price_fragment_to_sales_fragment)
                        R.id.clientFragment -> navController.navigate(R.id.action_clientFragment_to_sales_fragment)
                    }
                }
                else -> {}
            }
            true
        }
    }

    fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransition = fragmentManager.beginTransaction()
        fragmentTransition.replace(R.id.nav_host_fragment, fragment)
        fragmentTransition.commit()

    }
}



