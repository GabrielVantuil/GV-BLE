package com.example.gvble.Window

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
import com.example.gvble.databinding.ActivityWindowBinding


@SuppressLint("MissingPermission", "ClickableViewAccessibility")
class WindowManager() : BtManager(){
    private lateinit var binding: ActivityWindowBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWindowBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowActivity(binding, this)
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
        binding.windowOpenBt.isEnabled = isEnabled
        binding.windowCloseBt.isEnabled = isEnabled
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
    override fun onCharacteristicReadCallback(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, payload: ByteArray, status: Int) {}
    override fun onCharacteristicWriteCallback(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {}
    fun writeMotorCharacteristic(motorA : Int, timeout: Int = 0){
        if(!isConnected)    return
        val toSend =  byteArrayOf(
            (motorA shr 8).toByte(), motorA.toByte(),
            0, 0,
            0, 0,
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