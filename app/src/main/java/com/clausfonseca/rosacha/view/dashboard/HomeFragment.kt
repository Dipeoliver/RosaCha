package com.clausfonseca.rosacha.view.dashboard

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.databinding.FragmentHomeBinding
import com.clausfonseca.rosacha.view.adapter.ViewPagerAdapter
import com.clausfonseca.rosacha.view.chart.BarChartFragment
import com.clausfonseca.rosacha.view.dashboard.product.ListProductFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var auth: FirebaseAuth
    private val viewModel by viewModels<DashboardViewModel>() // mudar valores o xml visivel
    private var verifyTest: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        verifyTest = getString(R.string.verifyTest).toString()
        initClicks()
        onBackPressed()
        if (verifyTest == "true")
            binding.viewPager.setBackgroundColor(resources.getColor(android.R.color.holo_orange_dark))
        configTabLayout()

    }

    private fun configTabLayout() {
        val adapter = ViewPagerAdapter(requireActivity())
        binding.viewPager.adapter = adapter

        adapter.addFragment(BarChartFragment(), getString(R.string.title_home))
//        adapter.addFragment(AddProductFragment(), getString(R.string.add_product))
//        adapter.addFragment(EditProductFragment(), getString(R.string.edit_product))

        binding.viewPager.offscreenPageLimit = adapter.itemCount

        TabLayoutMediator(
            binding.tabs, binding.viewPager
        ) { tab, position ->
            tab.text = adapter.getTitle(
                position
            )
        }.attach()
    }

    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        })
    }

    private fun initClicks() {
        binding.ibLogout.setOnClickListener {
            logoutApp()
        }
    }

    private fun logoutApp() {
        auth.signOut()
        findNavController().popBackStack()
        val uri = Uri.parse("android-app://com.clausfonseca.rosacha/login_fragment")
        findNavController().navigate(uri)
    }
}