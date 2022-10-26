package com.clausfonseca.rosacha.ui.onboarding

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.clausfonseca.rosacha.databinding.ActivityMainBinding


class RosaChaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}