package com.example.gvble.Tank

import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.util.Log
import android.view.MotionEvent
import android.widget.SeekBar
import androidx.activity.ComponentActivity
import com.example.gvble.databinding.ActivityMotorBinding
import com.example.gvble.databinding.ActivityTankBinding
import java.util.Locale

@SuppressLint("ClickableViewAccessibility")
class TankActivity(var tankBinding: ActivityTankBinding, var tank: TankManager): ComponentActivity()  {
    val noChange = 0x7F00

    init{
        Log.i("GV", "Tank")
        setupDutySelectorsListeners()
        //https://www.toptal.com/designers/htmlarrows/arrows/
        tankBinding.tankN.setOnClickListener    {tankCtrlOnButton(1F, 1F)}      //↑
        tankBinding.tankE.setOnClickListener    {tankCtrlOnButton(1F, -1F)}     //→ ↻
        tankBinding.tankS.setOnClickListener    {tankCtrlOnButton(-1F, -1F)}    //↓
        tankBinding.tankW.setOnClickListener    {tankCtrlOnButton(-1F, 1F)}     //← ↺
        tankBinding.tankNE.setOnClickListener   {tankCtrlOnButton(1F, 0.5F)}    //↗
        tankBinding.tankSE.setOnClickListener   {tankCtrlOnButton(-1F, -0.5F)}  //↘
        tankBinding.tankSW.setOnClickListener   {tankCtrlOnButton(-0.5F, -1F)}  //↙
        tankBinding.tankNW.setOnClickListener   {tankCtrlOnButton(0.5F, 1F)}    //↖
        tankBinding.tankStop.setOnClickListener {tankCtrlOnButton(0F, 0F)}      //⚬


        //Power off
        tankBinding.powerOffBt.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN){
                powerOffCountDownTimer.start()
                true
            }
            if (event.action == MotionEvent.ACTION_UP){
                powerOffCountDownTimer.cancel()
                if(tankBinding.powerOffProgressBar.progress >= tankBinding.powerOffProgressBar.max) {
                    tank.writeConfigCharacteristic(true)
                }
                tankBinding.powerOffProgressBar.progress = 0
                true
            }
            false
        }

    }

    private val powerOffCountDownTimer: CountDownTimer = object : CountDownTimer(Long.MAX_VALUE, 25) {
        override fun onTick(l: Long) {
            tankBinding.powerOffProgressBar.progress++
        }
        override fun onFinish() {
        }
    }
    fun tankCtrlOnButton(mA : Float, mB : Float){
        val duty = tankBinding.tankDutyTextBox.text.toString().toInt()*100
        tank.writeMotorCharacteristic((duty*mA).toInt(), (duty*mB).toInt(), noChange)
    }
    private fun setupDutySelectorsListeners() {
        val onSeekBarChange = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                updateDutyCicles()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar){}
            override fun onStopTrackingTouch(seekBar: SeekBar){}
        }
        tankBinding.tankDutySel.setOnSeekBarChangeListener(onSeekBarChange)

        val onMCDutySelChange = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                updateDutyCicles()
                val duty = tankBinding.tankDutyCTextBox.text.toString().toInt()*100
                tank.writeMotorCharacteristic(noChange, noChange, duty)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar){}
            override fun onStopTrackingTouch(seekBar: SeekBar){
                val duty = tankBinding.tankDutyCTextBox.text.toString().toInt()*100
                tank.writeMotorCharacteristic(noChange, noChange, duty)
            }
        }
        tankBinding.tankMCdutySel.setOnSeekBarChangeListener(onMCDutySelChange)
    }

    private fun updateDutyCicles(){
        val tankDuty = tankBinding.tankDutySel.progress
        val mC = tankBinding.tankMCdutySel.progress - 100
        tankBinding.tankDutyTextBox.setText("$tankDuty")
        tankBinding.tankDutyCTextBox.setText("$mC")

    }
    override fun onResume() {
        super.onResume()
    }


}