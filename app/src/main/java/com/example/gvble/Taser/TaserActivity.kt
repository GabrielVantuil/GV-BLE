package com.example.gvble.Taser

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.ComponentActivity
import com.example.gvble.databinding.ActivityTaserBinding
import java.time.LocalTime
import java.time.Duration
import java.time.format.DateTimeFormatter

@SuppressLint("ClickableViewAccessibility")
class TaserActivity(var taserBinding: ActivityTaserBinding, var manager: TaserManager): ComponentActivity()  {

    init{
        Log.i("GV", "Taser")

        taserBinding.taserOnBt.setOnClickListener {
            sendCommand(1)
        }
        taserBinding.taserOffBt.setOnClickListener {
            sendCommand(0)
        }

        //All motors
        taserBinding.powerOffBt.setOnClickListener{
            manager.writeMotorCharacteristic(0,  0)
        }
    }
    private fun sendCommand(on: Int){
//        val timeInMs =  getDelayMs()
//        val dirText = if(dir==1) "Open" else "Close"
//
//        val sdf = SimpleDateFormat("HH:mm:ss")
//        val date = sdf.format(Date().time + (timeInMs.toString().toInt()))
//
//        taserBinding.windowLastCmd.text = "Last command: $dirText (to run at ~$date, in ${timeInMs/1000}s"
        manager.writeMotorCharacteristic(on,  0)
    }
    private fun getDelayMs(): Int {
        if(taserBinding.windowTimeoutTextBox.text.toString().isNotEmpty()) {
            return runCatching {
                val targetText = taserBinding.windowTimeoutTextBox.text.toString()
                val target = LocalTime.parse(targetText, DateTimeFormatter.ofPattern("H:mm"))

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
    override fun onResume() {
        super.onResume()
    }


}