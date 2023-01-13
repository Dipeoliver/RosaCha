package com.clausfonseca.rosacha.view.dashboard.client

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.br.jafapps.bdfirestore.util.DialogProgress
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.databinding.FragmentClientBinding
import com.clausfonseca.rosacha.databinding.FragmentProductBinding
import com.clausfonseca.rosacha.model.Client
import com.clausfonseca.rosacha.view.adapter.ClientAdapter
import com.clausfonseca.rosacha.view.adapter.ViewPagerAdapter
import com.clausfonseca.rosacha.view.dashboard.product.AddProductFragment
import com.clausfonseca.rosacha.view.dashboard.product.EditProductFragment
import com.clausfonseca.rosacha.view.dashboard.product.ListProductFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.FirebaseFirestore


class ClientFragment : Fragment() {

    private lateinit var binding: FragmentClientBinding
    private val clientlist = mutableListOf<Client>()
    var db: FirebaseFirestore? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentClientBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configTabLayout()
        db = FirebaseFirestore.getInstance()

    }

    private fun configTabLayout() {
        val adapter = ViewPagerAdapter(requireActivity())
        binding.viewPager.adapter = adapter

        adapter.addFragment(ListClientFragment(), getString(R.string.list_client))
//        adapter.addFragment(AddClientFragment(), getString(R.string.add_client))
        adapter.addFragment(EditClientFragment(), getString(R.string.edit_client))

        binding.viewPager.offscreenPageLimit = adapter.itemCount

        TabLayoutMediator(
            binding.tabs, binding.viewPager
        ) { tab, position ->
            tab.text = adapter.getTitle(
                position
            )

        }.attach()
    }
}