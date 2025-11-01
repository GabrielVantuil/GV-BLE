package com.example.gvble.Motor

import android.annotation.SuppressLint
import android.util.Log
import android.widget.SeekBar
import androidx.activity.ComponentActivity
import com.example.gvble.databinding.ActivityMotorBinding

@SuppressLint("ClickableViewAccessibility")
class MotorActivity(var motorBinding: ActivityMotorBinding, var motor: MotorManager): ComponentActivity()  {
    val noChange = 0x7F00

    init{
        Log.i("GV", "Motor")
        setupDutySelectorsListeners()
        //Motor A
        motorBinding.motorACw.setOnClickListener {
            val duty = motorBinding.dutyCycleATextBox.text.toString().toInt()
            val timeout = motorBinding.motorTimeoutTextBox.text.toString().toInt()
            motor.writeMotorCharacteristic(duty, noChange, noChange, timeout)
        }
        motorBinding.motorAStop.setOnClickListener {
            val timeout = motorBinding.motorTimeoutTextBox.text.toString().toInt()
            motor.writeMotorCharacteristic(0, noChange, noChange, timeout)
        }
        motorBinding.motorACcw.setOnClickListener {
            val duty = motorBinding.dutyCycleATextBox.text.toString().toInt()
            val timeout = motorBinding.motorTimeoutTextBox.text.toString().toInt()
            motor.writeMotorCharacteristic(-duty, noChange, noChange, timeout)
        }

        //Motor B
        motorBinding.motorBCw.setOnClickListener {
            val duty = motorBinding.dutyCycleBTextBox.text.toString().toInt()
            val timeout = motorBinding.motorTimeoutTextBox.text.toString().toInt()
            motor.writeMotorCharacteristic(noChange, duty, noChange, timeout)
        }
        motorBinding.motorBStop.setOnClickListener {
            val timeout = motorBinding.motorTimeoutTextBox.text.toString().toInt()
            motor.writeMotorCharacteristic(noChange, 0, noChange, timeout)
        }
        motorBinding.motorBCcw.setOnClickListener {
            val duty = motorBinding.dutyCycleBTextBox.text.toString().toInt()
            val timeout = motorBinding.motorTimeoutTextBox.text.toString().toInt()
            motor.writeMotorCharacteristic(noChange, -duty, noChange, timeout)
        }

        //Motor C
        motorBinding.motorCCw.setOnClickListener {
            val duty = motorBinding.dutyCycleCTextBox.text.toString().toInt()
            val timeout = motorBinding.motorTimeoutTextBox.text.toString().toInt()
            motor.writeMotorCharacteristic(noChange, noChange, duty, timeout)
        }
        motorBinding.motorCStop.setOnClickListener {
            val timeout = motorBinding.motorTimeoutTextBox.text.toString().toInt()
            motor.writeMotorCharacteristic(noChange, noChange, 0, timeout)
        }
        motorBinding.motorCCcw.setOnClickListener {
            val duty = motorBinding.dutyCycleCTextBox.text.toString().toInt()
            val timeout = motorBinding.motorTimeoutTextBox.text.toString().toInt()
            motor.writeMotorCharacteristic(noChange, noChange, -duty, timeout)
        }


        //All motors
        motorBinding.powerOffBt.setOnClickListener{
            val timeout = motorBinding.motorTimeoutTextBox.text.toString().toInt()
            motor.writeMotorCharacteristic(0, 0, 0, timeout)
        }

    }

    private fun setupDutySelectorsListeners() {
        val onSeekBarChange = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                updateDutyCicles()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar){}
            override fun onStopTrackingTouch(seekBar: SeekBar){}
        }
        motorBinding.MAdutySel.setOnSeekBarChangeListener(onSeekBarChange)
        motorBinding.MBdutySel.setOnSeekBarChangeListener(onSeekBarChange)
        motorBinding.MCdutySel.setOnSeekBarChangeListener(onSeekBarChange)
    }

    private fun updateDutyCicles(){
        val mA = (motorBinding.MAdutySel.progress * 100 / motorBinding.MAdutySel.max)
        val mB = (motorBinding.MBdutySel.progress * 100 / motorBinding.MBdutySel.max)
        val mC = (motorBinding.MCdutySel.progress * 100 / motorBinding.MCdutySel.max)
        motorBinding.dutyCycleATextBox.setText("$mA")
        motorBinding.dutyCycleBTextBox.setText("$mB")
        motorBinding.dutyCycleCTextBox.setText("$mC")

    }
    override fun onResume() {
        super.onResume()
    }


}