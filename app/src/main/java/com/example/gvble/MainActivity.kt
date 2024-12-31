package com.example.gvble

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.gvble.databinding.ActivityFlashlightBinding
import com.example.gvble.databinding.ActivityMainBinding
import com.example.gvble.databinding.ActivityPovDisplayBinding


class MainActivity : ComponentActivity() {
    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var flashlightBinding: ActivityFlashlightBinding
    private lateinit var povDisplayBinding: ActivityPovDisplayBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("GV", "start")
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        mainBinding.flashlightAppBt.setOnClickListener{
            flashlightBinding = ActivityFlashlightBinding.inflate(layoutInflater)
            setContentView(flashlightBinding.root)
            FlashlightActivity(this, flashlightBinding, this@MainActivity)
        }
        mainBinding.povAppBt.setOnClickListener{
            povDisplayBinding = ActivityPovDisplayBinding.inflate(layoutInflater)
            setContentView(povDisplayBinding.root)
            PovDisplayActivity(this, povDisplayBinding, this@MainActivity)
        }

    }
    override fun onResume() {
        super.onResume()
    }


}
