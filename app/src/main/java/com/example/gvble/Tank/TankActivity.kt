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
    val NCF = 99F   //no change float

    init{
        Log.i("GV", "Tank")
        setupDutySelectorsListeners()
        //https://www.toptal.com/designers/htmlarrows/arrows/
        tankBinding.tankN.setOnClickListener    {tankCtrlOnButton(1F, 1F)}      //↑
        tankBinding.tankE.setOnClickListener    {tankCtrlOnButton(0.5F, -0.5F)} //→ ↻
        tankBinding.tankS.setOnClickListener    {tankCtrlOnButton(-1F, -1F)}    //↓
        tankBinding.tankW.setOnClickListener    {tankCtrlOnButton(-0.5F, 0.5F)} //← ↺
        tankBinding.tankNE.setOnClickListener   {tankCtrlOnButton(1F, 0.5F)}    //↗
        tankBinding.tankSE.setOnClickListener   {tankCtrlOnButton(-1F, -0.5F)}  //↘
        tankBinding.tankSW.setOnClickListener   {tankCtrlOnButton(-0.5F, -1F)}  //↙
        tankBinding.tankNW.setOnClickListener   {tankCtrlOnButton(0.5F, 1F)}    //↖
        tankBinding.tankStop.setOnClickListener {tankCtrlOnButton(0F, 0F)}      //⚬

        tankBinding.tankNMA.setOnClickListener    {tankCtrlOnButton( 1F, NCF)}  //MA↑
        tankBinding.tankStopMA.setOnClickListener {tankCtrlOnButton( 0F, NCF)}  //MA⚬
        tankBinding.tankSMA.setOnClickListener    {tankCtrlOnButton(-1F, NCF)}  //MA↓
        tankBinding.tankNMB.setOnClickListener    {tankCtrlOnButton(NCF, 1F)}   //MB↑
        tankBinding.tankStopMB.setOnClickListener {tankCtrlOnButton(NCF, 0F)}   //MB⚬
        tankBinding.tankSMB.setOnClickListener    {tankCtrlOnButton(NCF, -1F)}  //MB↓

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

    private val powerOffCountDownTimer: CountDownTimer = object : CountDownTimer(Long.MAX_VALUE, 10) {
        override fun onTick(l: Long) {
            tankBinding.powerOffProgressBar.progress++
        }
        override fun onFinish() {
        }
    }
    fun tankCtrlOnButton(mA : Float, mB : Float){
        val duty = tankBinding.tankDutySel.progress
        if(mA == NCF)   tank.writeMotorCharacteristic(noChange, (duty*mB).toInt(), noChange)
        else if(mB == NCF) tank.writeMotorCharacteristic((duty*mA).toInt(), noChange, noChange)
        else tank.writeMotorCharacteristic((duty*mA).toInt(), (duty*mB).toInt(), noChange)
    }
    private fun setupDutySelectorsListeners() {
        val onSeekBarChange = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                updateMovementDutyCicles()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar){}
            override fun onStopTrackingTouch(seekBar: SeekBar){}
        }
        tankBinding.tankDutySel.setOnSeekBarChangeListener(onSeekBarChange)

        val onMCDutySelChange = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                updateMCDutyCicle()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar){}
            override fun onStopTrackingTouch(seekBar: SeekBar){
                updateMCDutyCicle()
            }
        }
        tankBinding.tankMCdutySel.setOnSeekBarChangeListener(onMCDutySelChange)
    }
    private fun updateMCDutyCicle(){
        var mC = tankBinding.tankMCdutySel.progress
        mC = if(tankBinding.tankMCdutyCheckBox.isChecked) -mC else mC
        val text = (mC/100).toString() + "%"
        tankBinding.tankCDutyPercentage.text = text
        tank.writeMotorCharacteristic(noChange, noChange, mC)
    }
    private fun updateMovementDutyCicles(){
        val duty = tankBinding.tankDutySel.progress
        val text = (duty/100).toString() + "%"
        tankBinding.tankDutyPercentage.text = text
    }
    override fun onResume() {
        super.onResume()
    }


}