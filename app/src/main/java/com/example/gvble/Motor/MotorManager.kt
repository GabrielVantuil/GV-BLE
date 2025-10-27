package com.example.gvble.Motor

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanFilter
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import com.example.gvble.BtManager
import com.example.gvble.MOTOR_SERVICE_UUID
import com.example.gvble.MOTOR_SET_MOTOR_CHAR_UUID
import com.example.gvble.databinding.ActivityMotorBinding


@SuppressLint("MissingPermission", "ClickableViewAccessibility")
class MotorManager() : BtManager(){
    private lateinit var binding: ActivityMotorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMotorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        MotorActivity(binding, this)
        startBleScan()

    }

    override var filters: ArrayList<ScanFilter> = arrayListOf(
        ScanFilter.Builder().setServiceUuid(ParcelUuid(MOTOR_SERVICE_UUID)).build())

    override fun onConnected(gatt: BluetoothGatt) {
        runOnUiThread {
            binding.connectionStatus.text = "Connected to ${gatt.device.name} (${gatt.device.address})"
        }
        bluetoothGatt = gatt
        bluetoothGatt.discoverServices()
        isConnected = true
    }
    private fun setBleButtonsEnabled(isEnabled: Boolean) {
        binding.motorACw.isEnabled = isEnabled
        binding.motorACcw.isEnabled = isEnabled
        binding.motorAStop.isEnabled = isEnabled
        binding.motorBCw.isEnabled = isEnabled
        binding.motorBCcw.isEnabled = isEnabled
        binding.motorBStop.isEnabled = isEnabled
        binding.powerOffBt.isEnabled = isEnabled
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt) {
        Log.i(tag, "onServicesDiscovered _________________________")
        runOnUiThread {
            setBleButtonsEnabled(true)
        }
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
    override fun onCharacteristicReadCallback(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, payload: ByteArray, status: Int) {
//        when (status) {
//            BluetoothGatt.GATT_SUCCESS -> {
//                when(characteristic.uuid){
//                    FLASHLIGHT_READ_LDR_CHAR_UUID -> {
//                        Log.i(tag, "Read LDR: " + (payload[0] * 256 + payload[1]))
//                    }
//                }
//            }
//            BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
//                Log.e("BluetoothGattCallback", "Read not permitted for ${characteristic.uuid}!")
//            }
//            else -> {
//                Log.e(
//                    "BluetoothGattCallback",
//                    "Characteristic read failed for ${characteristic.uuid}, error: $status"
//                )
//            }
//        }
    }
    override fun onCharacteristicWriteCallback(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
//        when(characteristic.uuid){
//            FLASHLIGHT_LOCK_CHAR_UUID -> {
//
//            }
//            FLASHLIGHT_LED_POWER_CHAR_UUID -> {
//                if(shouldPowerOff){
//                    writePowerCharacteristic(false)
//                    shouldPowerOff = false
//                }
//            }
//            FLASHLIGHT_LED_PWM_CHAR_UUID -> {
//                if(shouldPowerOff){
//                    writePowerCharacteristic(false)
//                    shouldPowerOff = false
//                }
//            }
//        }
    }
    fun writeMotorCharacteristic(motorNum : Int, freqX1000 : Int, duty : Int, timeout: Int = 0){
        if(!isConnected)    return
        val toSend =  byteArrayOf(
            motorNum.toByte(),
            (freqX1000 shr 24).toByte(), (freqX1000 shr 16).toByte(), (freqX1000 shr 8).toByte(), freqX1000.toByte(),
            (duty shr 8).toByte(), duty.toByte(),
            (timeout shr 24).toByte(), (timeout shr 16).toByte(), (timeout shr 8).toByte(), timeout.toByte()
        )
        val writeChangePayloadCharacteristic = bluetoothGatt.getService(MOTOR_SERVICE_UUID)
            .getCharacteristic(MOTOR_SET_MOTOR_CHAR_UUID)
        writeCharacteristic(writeChangePayloadCharacteristic, toSend)
    }

    override fun onScanStatusUpdate(status: String) {
        runOnUiThread {
            binding.connectionStatus.text = status
        }
    }
}