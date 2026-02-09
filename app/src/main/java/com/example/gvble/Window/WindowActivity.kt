package com.example.gvble.Window

import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.util.Log
import android.widget.EditText
import android.widget.SeekBar
import androidx.activity.ComponentActivity
import androidx.compose.ui.text.style.TextDirection
import androidx.core.widget.doOnTextChanged
import com.example.gvble.R
import com.example.gvble.databinding.ActivityWindowBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Timer
import java.util.TimerTask
import java.time.LocalTime
import java.time.Duration
import java.time.format.DateTimeFormatter

@SuppressLint("ClickableViewAccessibility")
class WindowActivity(var windowBinding: ActivityWindowBinding, var motor: WindowManager): ComponentActivity()  {
    val noChange = 0x7F00

    init{
        Log.i("GV", "Motor")
        setupDutySelectorsListeners()

        val timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val timeInMs = getDelayMs()
                if(timeInMs!=0) {
                    val baseText = "Execute at ~"
                    val sdf = SimpleDateFormat("HH:mm:ss")
                    val date = sdf.format(Date().time + (timeInMs.toString().toInt()))
                    if(timeInMs!=0)
                        windowBinding.timeoutText.text = "$baseText${date}"
                    else
                        windowBinding.timeoutText.text = "$baseText${date} (now)"
                    return
                }
            }
            override fun onFinish() {}
        }

        timer.start() // Start the countdown

        //Motor A
        windowBinding.windowOpenBt.setOnClickListener {
            sendCommand(1)
        }
        windowBinding.windowCloseBt.setOnClickListener {
            sendCommand(-1)
        }

        //All motors
        windowBinding.powerOffBt.setOnClickListener{
            motor.writeMotorCharacteristic(0,  0)
        }
    }
    private fun sendCommand(dir: Int){
        val duty = windowBinding.dutyCycleTextBox.text.toString().toInt() * dir
        val timeInMs =  getDelayMs()
        val dirText = if(dir==1) "Open" else "Close"

        val sdf = SimpleDateFormat("HH:mm:ss")
        val date = sdf.format(Date().time + (timeInMs.toString().toInt()))

        windowBinding.windowLastCmd.text = "Last command: $dirText (to run at ~$date, in ${timeInMs/1000}s"
        motor.writeMotorCharacteristic(duty,  timeInMs)
    }
    private fun setupDutySelectorsListeners() {
        val onSeekBarChange = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                updateDutyCicles()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar){}
            override fun onStopTrackingTouch(seekBar: SeekBar){}
        }
        windowBinding.windowDutySel.setOnSeekBarChangeListener(onSeekBarChange)
    }
    private fun getDelayMs(): Int {
        if(windowBinding.windowTimeoutTextBox.text.toString().isNotEmpty()) {
            return runCatching {
                val targetText = windowBinding.windowTimeoutTextBox.text.toString()
                val target = LocalTime.parse(targetText, DateTimeFormatter.ofPattern("HH:mm"))

                var timeUntil = Duration.between(LocalTime.now(), target)
                if (timeUntil.isNegative || timeUntil.isZero)   timeUntil = timeUntil.plusDays(1)
                val timeInMs = timeUntil.toMillis().toInt()
                //val timeInMs = (windowBinding.windowTimeoutTextBox.text.toString().toFloat() * 60000)
                timeInMs

            }.getOrElse {
                0
            }
        }
        return 0
    }

    private fun updateDutyCicles(){
        val progress = (windowBinding.windowDutySel.progress * 100 / windowBinding.windowDutySel.max)
        windowBinding.dutyCycleTextBox.setText("$progress")

    }
    override fun onResume() {
        super.onResume()
    }


}