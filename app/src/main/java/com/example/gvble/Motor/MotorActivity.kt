package com.example.gvble.Motor

import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import com.example.gvble.databinding.ActivityMotorBinding
import java.util.Locale

@SuppressLint("ClickableViewAccessibility")
class MotorActivity(var motorBinding: ActivityMotorBinding, var motor: MotorManager): ComponentActivity()  {
    private val TAG = "GVantuil"

    init{
        Log.i("GV", "Flashlight")
        //Motor A
        motorBinding.motorACw.setOnClickListener {
            val freq = (motorBinding.frequencyATextBox.text.toString().toFloat() * 1000).toInt()
            val duty = motorBinding.dutyCycleATextBox.text.toString().toInt()
            motor.writeMotorCharacteristic(1,freq, duty)
        }
        motorBinding.motorAStop.setOnClickListener {
            motor.writeMotorCharacteristic(1,0, 0)
        }
        motorBinding.motorACcw.setOnClickListener {
            val freq = (motorBinding.frequencyATextBox.text.toString().toFloat() * 1000).toInt()
            val duty = motorBinding.dutyCycleATextBox.text.toString().toInt()
            motor.writeMotorCharacteristic(1,freq, -duty)
        }


        //Motor B
        motorBinding.motorBCw.setOnClickListener {
            val freq = (motorBinding.frequencyBTextBox.text.toString().toFloat() * 1000).toInt()
            val duty = motorBinding.dutyCycleBTextBox.text.toString().toInt()
            motor.writeMotorCharacteristic(2,freq, duty)
        }
        motorBinding.motorBStop.setOnClickListener {
            motor.writeMotorCharacteristic(2,0, 0)
        }
        motorBinding.motorBCcw.setOnClickListener {
            val freq = (motorBinding.frequencyBTextBox.text.toString().toFloat() * 1000).toInt()
            val duty = motorBinding.dutyCycleBTextBox.text.toString().toInt()
            motor.writeMotorCharacteristic(2,freq, -duty)
        }
        //Both motors
        motorBinding.powerOffBt.setOnClickListener{
            motor.writeMotorCharacteristic(1,0, 0)
            motor.writeMotorCharacteristic(2,0, 0)
        }
    }
    private fun nextPowerModes(){
//        powerMode = (powerMode + 1) % 3
//        when(powerMode){
//            SAFE_PM ->{
//                motorBinding.powerModes.text = "Safe mode"
//                motorBinding.powerModes.setBackgroundColor(0xFF28772B.toInt())
//            }
//            PUSH_PM ->{
//                motorBinding.powerModes.text = "Push mode"
//                motorBinding.powerModes.setBackgroundColor(0xFFD3C446.toInt())
//            }
//            PERSISTENT_PM ->{
//                motorBinding.powerModes.text = "Persistent mode"
//                motorBinding.powerModes.setBackgroundColor(0xFFED0C0C.toInt())
//            }
//        }
    }
    override fun onResume() {
        super.onResume()
    }


}