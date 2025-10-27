package com.example.gvble.PovDisplay

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanFilter
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.core.widget.doAfterTextChanged
import com.example.gvble.BtManager
import com.example.gvble.FLASHLIGHT_READ_LDR_CHAR_UUID
import com.example.gvble.POV_DISPLAY_MODE_CHAR_UUID
import com.example.gvble.POV_DISPLAY_SERVICE_UUID
import com.example.gvble.POV_DISPLAY_SET_LEDS_CHAR_UUID
import com.example.gvble.POV_DISPLAY_SET_PARAMS_UUID
import com.example.gvble.POV_DISPLAY_SET_TEXT_CHAR_UUID
import com.example.gvble.R
import com.example.gvble.databinding.ActivityPovDisplayBinding

@SuppressLint("MissingPermission", "ClickableViewAccessibility")
class PovDisplayManager() : BtManager(){
    private lateinit var binding: ActivityPovDisplayBinding
    val totalLeds = 44
    private var synchronized : Byte = 0.toByte()
    private var rpm : Byte = 0.toByte()

    lateinit var ledsRgbVal : ByteArray
    var r : Int = 255
    var g : Int = 255
    var b : Int = 255

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPovDisplayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startBleScan()
        setupModeSelectorListeners()
        setupTextModeListeners()
        setupSingleLedModeListeners()
        setupColorChangerListeners()
        setupMotorPowerListeners()
        setupSynchronizedListener()
        binding.powerOffBt.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    ledsRgbVal = ByteArray(totalLeds)
                    for (i in 0..<totalLeds) {
                        ledsRgbVal[i] = 0.toByte()
                    }
                    writeSetLedsCharacteristic(ledsRgbVal)
                }
            }
            true
        }
    }

    private fun setBleButtonsEnabled(isEnabled: Boolean) {
        runOnUiThread {
            binding.powerOffBt.isEnabled = isEnabled
            binding.motorOffBt.isEnabled = isEnabled
            binding.powerMotorBt.isEnabled = isEnabled
            binding.sendTextBt.isEnabled = isEnabled
        }
    }

    override var filters: ArrayList<ScanFilter> = arrayListOf(
        ScanFilter.Builder().setServiceUuid(ParcelUuid(POV_DISPLAY_SERVICE_UUID)).build())
    override fun onConnected(gatt: BluetoothGatt) {
        runOnUiThread {
            binding.connectionStatus.text = "Connected to ${gatt.device.name} (${gatt.device.address})"
        }
        bluetoothGatt = gatt
        bluetoothGatt.discoverServices()
        isConnected = true
    }
    override fun onDisconnected(gatt: BluetoothGatt) {
        runOnUiThread {
            binding.connectionStatus.text = "Disconnected - Scanning"
            setBleButtonsEnabled(false)
        }
        isConnected = false
    }
    override fun onConnectionError(gatt: BluetoothGatt, status: Int) {
        runOnUiThread {
            binding.connectionStatus.text = "Scanning"
            setBleButtonsEnabled(false)
        }
        isConnected = false
    }
    override fun onServicesDiscovered(gatt: BluetoothGatt) {
        runOnUiThread {
            setBleButtonsEnabled(true)
        }
    }
    override fun onCharacteristicReadCallback(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, payload: ByteArray, status: Int) {
        when (status) {
            BluetoothGatt.GATT_SUCCESS -> {
                when(characteristic.uuid){
                    FLASHLIGHT_READ_LDR_CHAR_UUID -> {
                        Log.i(tag, "Read LDR: " + (payload[0] * 256 + payload[1]))
                    }
                }
            }
            BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                Log.e("BluetoothGattCallback", "Read not permitted for ${characteristic.uuid}!")
            }
            else -> {
                Log.e(
                    "BluetoothGattCallback",
                    "Characteristic read failed for ${characteristic.uuid}, error: $status"
                )
            }
        }
    }
    override fun onCharacteristicWriteCallback(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
//        when(characteristic.uuid){
//            TORCH_LOCK_CHAR_UUID -> {
//
//            }
//        }
    }

    fun writeModeCharacteristic(mode: Int){
        if(!isConnected)    return
        val writeChangePayloadCharacteristic = bluetoothGatt.getService(POV_DISPLAY_SERVICE_UUID).getCharacteristic(
            POV_DISPLAY_MODE_CHAR_UUID
        )
        writeCharacteristic(writeChangePayloadCharacteristic, byteArrayOf(mode.toByte()))
    }
    fun writeSendTextCharacteristic(text : ByteArray){
        if(!isConnected)    return
        val writeChangePayloadCharacteristic = bluetoothGatt.getService(POV_DISPLAY_SERVICE_UUID).getCharacteristic(
            POV_DISPLAY_SET_TEXT_CHAR_UUID
        )
        writeCharacteristic(writeChangePayloadCharacteristic, text)
    }
    fun writeSetLedsCharacteristic(leds: ByteArray){
        if(!isConnected)    return
        val writeChangePayloadCharacteristic = bluetoothGatt.getService(POV_DISPLAY_SERVICE_UUID).getCharacteristic(
            POV_DISPLAY_SET_LEDS_CHAR_UUID
        )
        writeCharacteristic(writeChangePayloadCharacteristic, leds)
    }
    fun writeSetParamsCharacteristic(){
        if(!isConnected)    return
        val params = byteArrayOf(0, rpm, synchronized)
        val writeChangePayloadCharacteristic = bluetoothGatt.getService(POV_DISPLAY_SERVICE_UUID).getCharacteristic(
            POV_DISPLAY_SET_PARAMS_UUID
        )
        writeCharacteristic(writeChangePayloadCharacteristic, params)
    }
    fun setSynchronized(synchronized: Byte){
        this.synchronized = synchronized
        writeSetParamsCharacteristic()
    }
    fun setMotorRpm(motorRpm: Byte){
        this.rpm = motorRpm
        writeSetParamsCharacteristic()
    }

    override fun onScanStatusUpdate(status: String) {
        runOnUiThread {
            binding.connectionStatus.text = status
        }
    }

    private fun setupModeSelectorListeners() {
        val items = resources.getStringArray(R.array.displayModes)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
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
                writeModeCharacteristic(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupMotorPowerListeners(){
        binding.powerMotorBt.setOnClickListener {
            val value = binding.desiredRpmTextBox.text.toString().toInt()
            setMotorRpm(value.toByte())
        }
        binding.motorOffBt.setOnClickListener {
            setMotorRpm(0)
        }
    }

    private fun setupTextModeListeners(){
        binding.sendTextBt.setOnClickListener {
            val text = binding.textToSendTextBox.text.toString()
            val x = byteArrayOf(r.toByte(), g.toByte(), b.toByte()) + text.toByteArray()
            writeSendTextCharacteristic(x)
        }
    }

    private fun setupColorChangerListeners() {
        val onRgbSeekBarChange = object : SeekBar.OnSeekBarChangeListener {
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
        binding.singleLedSelector.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar){}
            override fun onStopTrackingTouch(seekBar: SeekBar){}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val index = (binding.singleLedSelector.progress * (totalLeds-1) / binding.singleLedSelector.max)
                binding.ledIndex.text = index.toString()
                ledsRgbVal = byteArrayOf(r.toByte(), g.toByte(), b.toByte(), index.toByte())

                writeSetLedsCharacteristic(ledsRgbVal)
            }
        })
    }
    private fun setAllLeds(){
        ledsRgbVal = byteArrayOf(r.toByte(), g.toByte(), b.toByte())
        writeSetLedsCharacteristic(ledsRgbVal)
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
    private fun setupSynchronizedListener(){
        binding.synchronizeCheckbox.setOnClickListener {
            onSyncChanged()
        }
        binding.offsetTextbox.doAfterTextChanged {
            onSyncChanged()
        }
    }
    private fun onSyncChanged(){
        if(!binding.synchronizeCheckbox.isChecked)    setSynchronized(0xFF.toByte())
        else {
            val offset =
                if(!binding.offsetTextbox.text.isEmpty())
                    binding.offsetTextbox.text.toString().toInt() * 255 / 360
                else 0
            setSynchronized(offset.toByte())
        }
    }
}