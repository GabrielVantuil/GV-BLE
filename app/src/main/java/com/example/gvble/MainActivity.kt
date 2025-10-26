package com.example.gvble

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.gvble.Flashlight.FlashlightActivity
import com.example.gvble.Flashlight.FlashlightManager
import com.example.gvble.PovDisplay.PovDisplayManager
import com.example.gvble.databinding.ActivityFlashlightBinding
import com.example.gvble.databinding.ActivityMainBinding
import com.example.gvble.databinding.ActivityPovDisplayBinding


class MainActivity : ComponentActivity() {
    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var flashlightBinding: ActivityFlashlightBinding
    private lateinit var povDisplayBinding: ActivityPovDisplayBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        mainBinding.flashlightAppBt.setOnClickListener{
            val intent = Intent(this, FlashlightManager::class.java)
            startActivity(intent)
        }
        mainBinding.povAppBt.setOnClickListener{
            val intent = Intent(this, PovDisplayManager::class.java)
            startActivity(intent)
        }
    }
}