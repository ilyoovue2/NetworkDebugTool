package com.ilyoovue2.networkdebugtool

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ilyoovue2.networkdebugtool.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.serverButton.setOnClickListener {
            startActivity(Intent(this, ServerActivity::class.java))
        }

        binding.clientButton.setOnClickListener {
            startActivity(Intent(this, ClientActivity::class.java))
        }
    }
}



