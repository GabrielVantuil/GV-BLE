package com.example.gvble

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.activity.ComponentActivity
import com.example.gvble.databinding.ActivityPovDisplayBinding

@SuppressLint("ClickableViewAccessibility")
class PovDisplayActivity(private var context: Context, var binding: ActivityPovDisplayBinding, private var main: ComponentActivity): ComponentActivity()  {
    private var povDisplay: PovDisplayManager
    lateinit var ledsRgbVal : ByteArray//(povDisplay.totalLeds*3)
    var r : Int = 255
    var g : Int = 255
    var b : Int = 255
    init{
        povDisplay = PovDisplayManager(context, main, binding)
        povDisplay.startBleScan()
        setupModeSelectorListeners()
        setupTextModeListeners()
        setupSingleLedModeListeners()
        setupColorChangerListeners()
        setupMotorPowerListeners()

        binding.powerOffBt.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    ledsRgbVal = ByteArray(povDisplay.totalLeds)
                    for (i in 0..<povDisplay.totalLeds) {
                        ledsRgbVal[i] = 0.toByte()
                    }
                    povDisplay.writeSetLedsCharacteristic(ledsRgbVal)
                }
            }
            true
        }
    }
    private fun setupModeSelectorListeners() {
        val items = context.resources.getStringArray(R.array.displayModes)
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.modeSpinner.adapter = adapter

        binding.modeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                binding.setTextGroup.visibility = View.GONE
                binding.setSingleLedGroup.visibility = View.GONE
                when (position) {
                    0 -> binding.setTextGroup.visibility = View.VISIBLE
                    1 -> binding.setSingleLedGroup.visibility = View.VISIBLE
                }
                povDisplay.writeModeCharacteristic(position)
                Log.i("GV", "position $position")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupMotorPowerListeners(){
        binding.powerMotorBt.setOnClickListener {
            val value = binding.desiredRpmTextBox.text.toString().toInt()
            povDisplay.writeSetParamsCharacteristic(byteArrayOf(0, value.toByte()))
        }
        binding.motorOffBt.setOnClickListener {
            povDisplay.writeSetParamsCharacteristic(byteArrayOf(0, 0))
        }
    }

    private fun setupTextModeListeners(){
        binding.sendTextBt.setOnClickListener {
            val text = binding.textToSendTextBox.text.toString()
            val x = byteArrayOf(r.toByte(), g.toByte(), b.toByte()) + text.toByteArray()
            povDisplay.writeSendTextCharacteristic(x)
        }
    }

    private fun setupColorChangerListeners() {
        val onRgbSeekBarChange = object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                updateColor()
                if(binding.modeSpinner.selectedItemPosition == 2)   setAllLeds()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar){}
            override fun onStopTrackingTouch(seekBar: SeekBar){}
        }
        binding.rSelector.setOnSeekBarChangeListener(onRgbSeekBarChange)
        binding.gSelector.setOnSeekBarChangeListener(onRgbSeekBarChange)
        binding.bSelector.setOnSeekBarChangeListener(onRgbSeekBarChange)
    }

    private fun setupSingleLedModeListeners(){
        binding.singleLedSelector.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onStartTrackingTouch(seekBar: SeekBar){}
            override fun onStopTrackingTouch(seekBar: SeekBar){}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val index = (binding.singleLedSelector.progress * (povDisplay.totalLeds-1) / binding.singleLedSelector.max)
                binding.ledIndex.text = index.toString()
                ledsRgbVal = ByteArray(povDisplay.totalLeds*3)
                for(i in 0..<povDisplay.totalLeds){
                    if(i != index) {
                        ledsRgbVal[i * 3 + 0] = 0
                        ledsRgbVal[i * 3 + 1] = 0
                        ledsRgbVal[i * 3 + 2] = 0
                    }
                    else{
                        ledsRgbVal[i * 3 + 0] = r.toByte()
                        ledsRgbVal[i * 3 + 1] = g.toByte()
                        ledsRgbVal[i * 3 + 2] = b.toByte()
                    }
                }
                povDisplay.writeSetLedsCharacteristic(ledsRgbVal)
                var buff = " "
                for(i in 0..<ledsRgbVal.size){
                    buff += ledsRgbVal[i].toString(16).padStart(2, '0')+" "
                }
                Log.i("GV", buff)
            }
        })
    }
    private fun setAllLeds(){
        ledsRgbVal = ByteArray(povDisplay.totalLeds*3)
        for(i in 0..<povDisplay.totalLeds){
            ledsRgbVal[i * 3 + 0] = r.toByte()
            ledsRgbVal[i * 3 + 1] = g.toByte()
            ledsRgbVal[i * 3 + 2] = b.toByte()
        }
        povDisplay.writeSetLedsCharacteristic(ledsRgbVal)
    }
    @SuppressLint("SetTextI18n")
    private fun updateColor(){
        r = (binding.rSelector.progress * 255 / binding.rSelector.max)
        g = (binding.gSelector.progress * 255 / binding.gSelector.max)
        b = (binding.bSelector.progress * 255 / binding.bSelector.max)
        val color = "#" +
                r.toString(16).padStart(2, '0') +
                g.toString(16).padStart(2, '0') +
                b.toString(16).padStart(2, '0')
        binding.singleColorLedVal.text = color
        binding.singleColorLedVal.setBackgroundColor(Color.parseColor(color))

    }

}