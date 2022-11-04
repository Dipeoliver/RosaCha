package com.clausfonseca.rosacha.view.dashboard.sales

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.databinding.FragmentSalesBinding
import com.clausfonseca.rosacha.view.adapter.ViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SalesFragment : Fragment() {

    private var _binding: FragmentSalesBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSalesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        initClicks()
        configTabLayout()
        initClicks()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun configTabLayout() {
        val adapter = ViewPagerAdapter(requireActivity())
        binding.viewPager.adapter = adapter

        adapter.addFragment(AddSalesFragment(), getString(R.string.add_sales))
        adapter.addFragment(ListSalesFragment(), getString(R.string.list_sales))
        adapter.addFragment(EditSalesFragment(), getString(R.string.edit_sales))

        binding.viewPager.offscreenPageLimit = adapter.itemCount

        TabLayoutMediator(
            binding.tabs, binding.viewPager
        ) { tab, position ->
            tab.text = adapter.getTitle(
                position
            )
        }.attach()
    }

    private fun initClicks() {
        binding.ibLogout.setOnClickListener {
            logoutApp()
        }
    }

    private fun logoutApp() {
        auth.signOut()
        findNavController().navigate(R.id.action_sales_fragment_to_authentication)
    }
}