package com.clausfonseca.rosacha.view.onboarding

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.databinding.ActivityMainBinding
import com.clausfonseca.rosacha.view.dashboard.HomeFragment
import com.clausfonseca.rosacha.view.dashboard.client.ClientFragment
import com.clausfonseca.rosacha.view.dashboard.price.PriceFragment
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

        val navController = findNavController(R.id.nav_host_fragment)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.home_fragment,
                R.id.product_fragment,
                R.id.client_fragment,
                R.id.price_fragment,
                R.id.sales_fragment -> {
                    binding.bottomNavigationView.visibility = View.VISIBLE

                }
                else -> {
                    binding.bottomNavigationView.visibility = View.GONE
                }
            }
        }
        binding.bottomNavigationView.setupWithNavController(navController)

        binding.bottomNavigationView.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.home_fragment -> replaceFragment(HomeFragment())
                R.id.product_fragment -> replaceFragment(ProductFragment())
                R.id.client_fragment -> replaceFragment(ClientFragment())
                R.id.price_fragment -> replaceFragment(PriceFragment())
                R.id.sales_fragment -> replaceFragment(SalesFragment())
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



