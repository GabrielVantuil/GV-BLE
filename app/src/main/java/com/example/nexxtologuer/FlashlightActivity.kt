package com.example.nexxtologuer

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import androidx.activity.ComponentActivity
import com.example.gvble.TorchManager
import com.example.gvble.databinding.ActivityFlashlightBinding
import java.util.Locale

private const val TAG = "GVantuil"
private const val SAFE_PM: Int = 0
private const val PUSH_PM: Int = 1
private const val PERSISTENT_PM: Int = 2

private const val MEDIUM_POWER: Int = 70

private const val POWER_TIMEOUT: Int = 1500
private const val UPDATE_CONN_PARAMS_TIMER: Long = 100
private const val ONE_RPM: Float = 1F/60
class FlashlightActivity(private var context: Context, var flashlightBinding: ActivityFlashlightBinding, private var main: ComponentActivity): ComponentActivity()  {

    private lateinit var torch: TorchManager
    private var powerMode: Int = 0
    private var frequency: Float = 60F
    private var freqIncrBtCounter: Int = 0

    init{
        Log.i("GV", "Flashlight")
        torch = TorchManager(context, main, flashlightBinding)
        torch.startBleScan()

        updateFrequency()
        flashlightBinding.pwmOn.setOnClickListener{
            val freq = (flashlightBinding.frequencyTextBox.text.toString().toFloat() * 1000).toInt()
            val duty = flashlightBinding.dutyCycleTextBox.text.toString().toInt()
            torch.writePwmCharacteristic(freq, duty)
        }
        flashlightBinding.lockSwitch.setOnClickListener{
            torch.writeLockCharacteristic(if(flashlightBinding.lockSwitch.isChecked) 1 else 0)
        }
        flashlightBinding.powerOffBt.setOnClickListener{
            torch.writePowerCharacteristic(false)
        }
        flashlightBinding.weakPowerOnBt.setOnTouchListener { _, event ->
            if (event.action == ACTION_DOWN){
                if(powerMode == SAFE_PM)        weakPowerOnCountDownTimer.start()
                else    torch.writePwmCharacteristic(10000000, 1, 0)
            }
            if (event.action == ACTION_UP){
                if(powerMode != PERSISTENT_PM) torch.writePowerCharacteristic(false)
                torch.shouldPowerOff = (powerMode != PERSISTENT_PM)
                weakPowerOnCountDownTimer.cancel()
            }
            true
        }
        flashlightBinding.mediumPowerOnBt.setOnTouchListener { _, event ->
            if (event.action == ACTION_DOWN){
                if(powerMode == SAFE_PM)        mediumPowerOnCountDownTimer.start()
                else    torch.writePwmCharacteristic(10000000, MEDIUM_POWER, 0)
            }
            if (event.action == ACTION_UP){
                if(powerMode != PERSISTENT_PM) torch.writePowerCharacteristic(false)
                torch.shouldPowerOff = (powerMode != PERSISTENT_PM)
                mediumPowerOnCountDownTimer.cancel()
            }
            true
        }
        flashlightBinding.fullPowerOnBt.setOnTouchListener { _, event ->
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
        flashlightBinding.powerModes.setOnClickListener{
            nextPowerModes()
        }
        flashlightBinding.frequencyTextBox.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(s == null || s.toString() == "") return
                frequency = flashlightBinding.frequencyTextBox.text.toString().toFloat()
                updateFrequency()
            }
        })
        flashlightBinding.decreaseFrequency.setOnTouchListener { _, event ->
            if (event.action == ACTION_DOWN)    decreaseFrequencyCountDownTimer.start()
            if (event.action == ACTION_UP){
                decreaseFrequencyCountDownTimer.cancel()
                freqIncrBtCounter = 0
            }
            true
        }
        flashlightBinding.increaseFrequency.setOnTouchListener { _, event ->
            if (event.action == ACTION_DOWN)    increaseFrequencyCountDownTimer.start()
            if (event.action == ACTION_UP){
                increaseFrequencyCountDownTimer.cancel()
                freqIncrBtCounter = 0
            }
            true
        }
    }
    private fun updateFrequency(sendToDevice: Boolean = false){
        val text = "%.3f".format(Locale.ROOT, frequency) + "Hz (" + (frequency * 60).toInt() +"rpm)"
        flashlightBinding.freqRpmText.text = text

        val duty = flashlightBinding.dutyCycleTextBox.text.toString().toInt()
        if(sendToDevice) torch.writePwmCharacteristic((frequency * 1000).toInt(), duty)
    }
    private val weakPowerOnCountDownTimer: CountDownTimer = object : CountDownTimer(Long.MAX_VALUE, UPDATE_CONN_PARAMS_TIMER) {
        override fun onTick(l: Long) {
            torch.writePwmCharacteristic(10000000, 1, POWER_TIMEOUT)
        }
        override fun onFinish() {}
    }
    private val mediumPowerOnCountDownTimer: CountDownTimer = object : CountDownTimer(Long.MAX_VALUE, UPDATE_CONN_PARAMS_TIMER) {
        override fun onTick(l: Long) {
            torch.writePwmCharacteristic(10000000, MEDIUM_POWER, POWER_TIMEOUT)
        }
        override fun onFinish() {}
    }
    private val fullPowerOnCountDownTimer: CountDownTimer = object : CountDownTimer(Long.MAX_VALUE, UPDATE_CONN_PARAMS_TIMER) {
        override fun onTick(l: Long) {
            torch.writePowerCharacteristic(true, POWER_TIMEOUT)
        }
        override fun onFinish() {}
    }

    private val decreaseFrequencyCountDownTimer: CountDownTimer = object : CountDownTimer(Long.MAX_VALUE, 250) {
        override fun onTick(l: Long) {
            if(freqIncrBtCounter >= 20) frequency -= 100 * ONE_RPM
            else if(freqIncrBtCounter >= 10) frequency -= 10 * ONE_RPM
            else frequency -= ONE_RPM
            updateFrequency(true)
            flashlightBinding.frequencyTextBox.setText("%.3f".format(Locale.ROOT, frequency))
            freqIncrBtCounter++
        }
        override fun onFinish() {
        }
    }
    private val increaseFrequencyCountDownTimer: CountDownTimer = object : CountDownTimer(Long.MAX_VALUE, 250) {
        override fun onTick(l: Long) {
            if(freqIncrBtCounter >= 20) frequency += 100 * ONE_RPM
            else if(freqIncrBtCounter >= 10) frequency += 10 * ONE_RPM
            else frequency += ONE_RPM
            updateFrequency(true)
            flashlightBinding.frequencyTextBox.setText("%.3f".format(Locale.ROOT, frequency))
            freqIncrBtCounter++
        }
        override fun onFinish() {
        }
    }
    private fun nextPowerModes(){
        powerMode = (powerMode + 1) % 3
        when(powerMode){
            SAFE_PM->{
                flashlightBinding.powerModes.text = "Safe mode"
                flashlightBinding.powerModes.setBackgroundColor(0xFF28772B.toInt())
            }
            PUSH_PM->{
                flashlightBinding.powerModes.text = "Push mode"
                flashlightBinding.powerModes.setBackgroundColor(0xFFD3C446.toInt())
            }
            PERSISTENT_PM->{
                flashlightBinding.powerModes.text = "Persistent mode"
                flashlightBinding.powerModes.setBackgroundColor(0xFFED0C0C.toInt())
            }
        }
    }
    override fun onResume() {
        super.onResume()
    }

}