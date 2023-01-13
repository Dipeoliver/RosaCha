package com.clausfonseca.rosacha.view.onboarding

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.databinding.ActivityMainBinding


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
                R.id.client_fragment,
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
                        R.id.product_fragment, R.id.client_fragment, R.id.price_fragment, R.id.sales_fragment ->
                            setDeepLinkNavigation(
                                "android-app://com.clausfonseca.rosacha/home_fragment",
                                navController
                            )
                    }
                }
                R.id.client_fragment -> {
                    when (oldItemId) {
                        R.id.home_fragment, R.id.product_fragment, R.id.price_fragment, R.id.sales_fragment ->
                            setDeepLinkNavigation(
                                "android-app://com.clausfonseca.rosacha/client_fragment",
                                navController
                            )
                    }
                }
                R.id.price_fragment -> {
                    when (oldItemId) {
                        R.id.home_fragment, R.id.product_fragment, R.id.client_fragment, R.id.sales_fragment ->
                            setDeepLinkNavigation(
                                "android-app://com.clausfonseca.rosacha/price_fragment",
                                navController
                            )
                    }
                }
                R.id.product_fragment -> {
                    when (oldItemId) {
                        R.id.home_fragment, R.id.price_fragment, R.id.client_fragment, R.id.sales_fragment ->
                            setDeepLinkNavigation(
                                "android-app://com.clausfonseca.rosacha/product_fragment",
                                navController
                            )
                    }
                }
                R.id.sales_fragment -> {
                    when (oldItemId) {
                        R.id.home_fragment, R.id.product_fragment, R.id.price_fragment, R.id.client_fragment ->
                            setDeepLinkNavigation(
                                "android-app://com.clausfonseca.rosacha/sales_fragment",
                                navController
                            )
                    }
                }
                else -> {}
            }
            true
        }
    }

    private fun setDeepLinkNavigation(uriString: String, navController: NavController) {
        val uri =
            Uri.parse(uriString)
        navController.navigate(uri)
    }
}



