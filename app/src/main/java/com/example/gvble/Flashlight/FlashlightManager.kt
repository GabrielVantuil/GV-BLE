package com.example.gvble.Flashlight

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanFilter
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.os.ParcelUuid
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import com.example.gvble.BtManager
import com.example.gvble.FLASHLIGHT_LED_POWER_CHAR_UUID
import com.example.gvble.FLASHLIGHT_LED_PWM_CHAR_UUID
import com.example.gvble.FLASHLIGHT_LOCK_CHAR_UUID
import com.example.gvble.FLASHLIGHT_READ_LDR_CHAR_UUID
import com.example.gvble.FLASHLIGHT_SERVICE_UUID
import com.example.gvble.databinding.ActivityFlashlightBinding
import java.util.Locale


@SuppressLint("MissingPermission", "ClickableViewAccessibility")
class FlashlightManager() : BtManager(){
    private lateinit var binding: ActivityFlashlightBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlashlightBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FlashlightActivity(binding, this)
        startBleScan()

    }

    override var filters: ArrayList<ScanFilter> = arrayListOf(
        ScanFilter.Builder().setServiceUuid(ParcelUuid(FLASHLIGHT_SERVICE_UUID)).build())

    override fun onConnected(gatt: BluetoothGatt) {
        runOnUiThread {
            binding.connectionStatus.text = "Connected to ${gatt.device.name} (${gatt.device.address})"
        }
        bluetoothGatt = gatt
        bluetoothGatt.discoverServices()
        isConnected = true
    }
    private fun setBleButtonsEnabled(isEnabled: Boolean) {
        runOnUiThread {
            binding.pwmOn.isEnabled = isEnabled
            binding.powerOffBt.isEnabled = isEnabled
            binding.weakPowerOnBt.isEnabled = isEnabled
            binding.mediumPowerOnBt.isEnabled = isEnabled
            binding.fullPowerOnBt.isEnabled = isEnabled
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt) {
        Log.i(tag, "onServicesDiscovered _________________________")
        setBleButtonsEnabled(true)
    }
    var shouldPowerOff: Boolean = false
    override fun onDisconnected(gatt: BluetoothGatt) {
        runOnUiThread {
            binding.connectionStatus.text = "Disconnected - Scanning"
        }
        setBleButtonsEnabled(false)
        isConnected = false
    }
    override fun onConnectionError(gatt: BluetoothGatt, status: Int) {
        runOnUiThread {
            binding.connectionStatus.text = "Scanning"
        }
        isConnected = false
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
        when(characteristic.uuid){
            FLASHLIGHT_LOCK_CHAR_UUID -> {

            }
            FLASHLIGHT_LED_POWER_CHAR_UUID -> {
                if(shouldPowerOff){
                    writePowerCharacteristic(false)
                    shouldPowerOff = false
                }
            }
            FLASHLIGHT_LED_PWM_CHAR_UUID -> {
                if(shouldPowerOff){
                    writePowerCharacteristic(false)
                    shouldPowerOff = false
                }
            }
        }
    }
    private fun readLdrCharacteristic(){
        if(!isConnected)    return
        val getPayloadCharacteristic = bluetoothGatt.getService(FLASHLIGHT_SERVICE_UUID).getCharacteristic(
            FLASHLIGHT_READ_LDR_CHAR_UUID
        )
        bluetoothGatt.readCharacteristic(getPayloadCharacteristic)
    }
    fun writeLockCharacteristic(lock : Int){
        if(!isConnected)    return
        val writeChangePayloadCharacteristic = bluetoothGatt.getService(FLASHLIGHT_SERVICE_UUID).getCharacteristic(
            FLASHLIGHT_LOCK_CHAR_UUID
        )
        writeCharacteristic(writeChangePayloadCharacteristic, byteArrayOf(lock.toByte()))
    }
    fun writePowerCharacteristic(power : Boolean, timeout: Int = 0){
        if(!isConnected)    return
        val toSend = byteArrayOf(
            if(power) 1 else 0,
            (timeout shr 24).toByte(), (timeout shr 16).toByte(), (timeout shr 8).toByte(), timeout.toByte()
        )
        val writeChangePayloadCharacteristic = bluetoothGatt.getService(FLASHLIGHT_SERVICE_UUID).getCharacteristic(
            FLASHLIGHT_LED_POWER_CHAR_UUID
        )
        writeCharacteristic(writeChangePayloadCharacteristic, toSend)
    }
    fun writePwmCharacteristic(freqX1000 : Int, duty : Int, timeout: Int = 0){
        if(!isConnected)    return
        val toSend =  byteArrayOf(
            (freqX1000 shr 24).toByte(), (freqX1000 shr 16).toByte(), (freqX1000 shr 8).toByte(), freqX1000.toByte(),
            (duty shr 8).toByte(), duty.toByte(),
            (timeout shr 24).toByte(), (timeout shr 16).toByte(), (timeout shr 8).toByte(), timeout.toByte()
        )
        if(bluetoothGatt.getService(FLASHLIGHT_SERVICE_UUID) == null) {
            Log.i(tag, "writePwmCharacteristic: Service not found")
            return
        }
        val writeChangePayloadCharacteristic = bluetoothGatt.getService(FLASHLIGHT_SERVICE_UUID).getCharacteristic(
            FLASHLIGHT_LED_PWM_CHAR_UUID
        )
        writeCharacteristic(writeChangePayloadCharacteristic, toSend)
    }

    override fun onScanStatusUpdate(status: String) {
        runOnUiThread {
            binding.connectionStatus.text = status
        }
    }
}