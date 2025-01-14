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
import androidx.core.widget.doAfterTextChanged
import com.example.gvble.databinding.ActivityPovDisplayBinding
import java.nio.ByteBuffer

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
        setupSincronizedListener()
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
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupMotorPowerListeners(){
        binding.powerMotorBt.setOnClickListener {
            val value = binding.desiredRpmTextBox.text.toString().toInt()
            povDisplay.setMotorRpm(value.toByte())
        }
        binding.motorOffBt.setOnClickListener {
            povDisplay.setMotorRpm(0)
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
                ledsRgbVal = byteArrayOf(r.toByte(), g.toByte(), b.toByte(), index.toByte())

                povDisplay.writeSetLedsCharacteristic(ledsRgbVal)
            }
        })
    }
    private fun setAllLeds(){
        ledsRgbVal = byteArrayOf(r.toByte(), g.toByte(), b.toByte())
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
    private fun setupSincronizedListener(){
        binding.synchronizeCheckbox.setOnClickListener {
            onSincChanged()
        }
        binding.offsetTextbox.doAfterTextChanged {
            onSincChanged()
        }
    }
    private fun onSincChanged(){
        if(!binding.synchronizeCheckbox.isChecked)    povDisplay.setSynchronized(0xFF.toByte())
        else {
            val offset =
                if(!binding.offsetTextbox.text.isEmpty())
                    binding.offsetTextbox.text.toString().toInt() * 255 / 360
                else 0
            povDisplay.setSynchronized(offset.toByte())
        }
    }
}