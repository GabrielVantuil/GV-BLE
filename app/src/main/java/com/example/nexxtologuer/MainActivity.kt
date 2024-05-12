package com.example.gvble

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.widget.Switch
import androidx.activity.ComponentActivity
import com.example.gvble.databinding.ActivityMainBinding
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


private const val TAG = "GVantuil"
class MainActivity : ComponentActivity() {
    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var torch: TorchManager
    private val POWER_TIMEOUT: Int = 1500
    private val UPDATE_CONN_PARAMS_TIMER: Long = 100

    private var powerMode: Int = 0
    private val SAFE_PM: Int = 0
    private val PUSH_PM: Int = 1
    private val PERSISTENT_PM: Int = 2


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        torch = TorchManager(this, this@MainActivity, mainBinding)
        torch.startBleScan()

        mainBinding.pwmOn.setOnClickListener{
            var freq = mainBinding.frequencyTextBox.text.toString().toInt()
            var duty = mainBinding.dutyCycleTextBox.text.toString().toInt()
            torch.writePwmCharacteristic(freq, duty)
        }
        mainBinding.lockSwitch.setOnClickListener{
            torch.writeLockCharacteristic(if(mainBinding.lockSwitch.isChecked) 1 else 0)
        }
        mainBinding.powerOffBt.setOnClickListener{
            torch.writePowerCharacteristic(false)
        }
        mainBinding.weakPowerOnBt.setOnTouchListener { _, event ->
            if (event.action == ACTION_DOWN){
                if(powerMode == SAFE_PM)        weakPowerOnCountDownTimer.start()
                else    torch.writePwmCharacteristic(1000, 1, 0)
            }
            if (event.action == ACTION_UP){
                if(powerMode != PERSISTENT_PM) torch.writePowerCharacteristic(false)
                torch.shouldPowerOff = (powerMode != PERSISTENT_PM)
                weakPowerOnCountDownTimer.cancel()
            }
            true
        }
        mainBinding.fullPowerOnBt.setOnTouchListener { _, event ->
            if (event.action == ACTION_DOWN){
                if(powerMode == SAFE_PM)    fullPowerOnCountDownTimer.start()
                else    torch.writePowerCharacteristic(true, 0)
            }
            if (event.action == ACTION_UP) {
                if(powerMode != PERSISTENT_PM) torch.writePowerCharacteristic(false)
                torch.shouldPowerOff = (powerMode != PERSISTENT_PM)
                fullPowerOnCountDownTimer.cancel()
            }
            true
        }
        mainBinding.powerModes.setOnClickListener{
            nextPowerModes()
        }
    }
    private val weakPowerOnCountDownTimer: CountDownTimer = object : CountDownTimer(Long.MAX_VALUE, UPDATE_CONN_PARAMS_TIMER) {
        override fun onTick(l: Long) {
            torch.writePwmCharacteristic(1000, 1, POWER_TIMEOUT)
        }
        override fun onFinish() {
        }
    }
    private val fullPowerOnCountDownTimer: CountDownTimer = object : CountDownTimer(Long.MAX_VALUE, UPDATE_CONN_PARAMS_TIMER) {
        override fun onTick(l: Long) {
            torch.writePowerCharacteristic(true, POWER_TIMEOUT)
        }
        override fun onFinish() {
        }
    }
    private fun nextPowerModes(){
        powerMode = (powerMode + 1) % 3

        when(powerMode){
            SAFE_PM->{
                mainBinding.powerModes.text = "Safe mode"
                mainBinding.powerModes.setBackgroundColor(0xFF28772B.toInt())
            }
            PUSH_PM->{
                mainBinding.powerModes.text = "Push mode"
                mainBinding.powerModes.setBackgroundColor(0xFFD3C446.toInt())
            }
            PERSISTENT_PM->{
                mainBinding.powerModes.text = "Persistent mode"
                mainBinding.powerModes.setBackgroundColor(0xFFED0C0C.toInt())
            }
        }
    }
    override fun onResume() {
        super.onResume()
    }


}
