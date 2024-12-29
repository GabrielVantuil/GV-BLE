package com.example.gvble

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.gvble.databinding.ActivityFlashlightBinding
import com.example.gvble.databinding.ActivityMainBinding
import com.example.nexxtologuer.FlashlightActivity
import java.util.Locale



class MainActivity : ComponentActivity() {
    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var flashlightBinding: ActivityFlashlightBinding

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
            Toast.makeText(this, "Not ready", Toast.LENGTH_SHORT).show()
        }

    }
    override fun onResume() {
        super.onResume()
    }


}
