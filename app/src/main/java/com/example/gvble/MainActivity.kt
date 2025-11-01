package com.example.gvble

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.gvble.Flashlight.FlashlightManager
import com.example.gvble.Motor.MotorManager
import com.example.gvble.PovDisplay.PovDisplayManager
import com.example.gvble.Tank.TankManager
import com.example.gvble.databinding.ActivityMainBinding


class MainActivity : ComponentActivity() {
    private lateinit var mainBinding: ActivityMainBinding

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
        mainBinding.motorAppBt.setOnClickListener{
            val intent = Intent(this, MotorManager::class.java)
            startActivity(intent)
        }
        mainBinding.tankAppBt.setOnClickListener{
            val intent = Intent(this, TankManager::class.java)
            startActivity(intent)
        }
    }
}