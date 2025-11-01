package com.example.gvble.Tank

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanFilter
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import com.example.gvble.BtManager
import com.example.gvble.TANK_SERVICE_UUID
import com.example.gvble.TANK_SET_CONFIG_CHAR_UUID
import com.example.gvble.TANK_SET_MOTOR_CHAR_UUID
import com.example.gvble.databinding.ActivityTankBinding


@SuppressLint("MissingPermission", "ClickableViewAccessibility")
class TankManager() : BtManager(){
    private lateinit var binding: ActivityTankBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTankBinding.inflate(layoutInflater)
        setContentView(binding.root)
        TankActivity(binding, this)
        startBleScan()

    }

    override var filters: ArrayList<ScanFilter> = arrayListOf(
        ScanFilter.Builder().setServiceUuid(ParcelUuid(TANK_SERVICE_UUID)).build())

    override fun onConnected(gatt: BluetoothGatt) {
        runOnUiThread {
            binding.connectionStatus.text = "Connected to ${gatt.device.name} (${gatt.device.address})"
        }
        bluetoothGatt = gatt
        bluetoothGatt.discoverServices()
        isConnected = true
    }

    fun setBleButtonsEnabled(isEnabled: Boolean) {
        binding.tankN.isEnabled = isEnabled
        binding.tankE.isEnabled = isEnabled
        binding.tankS.isEnabled = isEnabled
        binding.tankW.isEnabled = isEnabled
        binding.tankNE.isEnabled = isEnabled
        binding.tankSE.isEnabled = isEnabled
        binding.tankSW.isEnabled = isEnabled
        binding.tankNW.isEnabled = isEnabled
        binding.tankStop.isEnabled = isEnabled
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
    fun writeMotorCharacteristic(motorA : Int, motorB : Int, motorC : Int, timeout: Int = 0){
        if(!isConnected)    return
        val toSend =  byteArrayOf(
            (motorA shr 8).toByte(), motorA.toByte(),
            (motorB shr 8).toByte(), motorB.toByte(),
            (motorC shr 8).toByte(), motorC.toByte(),
            (timeout shr 24).toByte(), (timeout shr 16).toByte(), (timeout shr 8).toByte(), timeout.toByte()
        )
        val writeChangePayloadCharacteristic = bluetoothGatt.getService(TANK_SERVICE_UUID)
            .getCharacteristic(TANK_SET_MOTOR_CHAR_UUID)
        writeCharacteristic(writeChangePayloadCharacteristic, toSend)
    }
    fun writeConfigCharacteristic(powerOff: Boolean = false){
        if(!isConnected)    return
        val toSend =  byteArrayOf(
            0.toByte(), 0.toByte(), //LED duty
            0.toByte(), 0.toByte(), //P
            0.toByte(), 0.toByte(), //I
            0.toByte(), 0.toByte(), //D
            if(powerOff) 1.toByte() else 0.toByte(),
        )
        val writeChangePayloadCharacteristic = bluetoothGatt.getService(TANK_SERVICE_UUID)
            .getCharacteristic(TANK_SET_CONFIG_CHAR_UUID)
        writeCharacteristic(writeChangePayloadCharacteristic, toSend)
    }

    override fun onScanStatusUpdate(status: String) {
        runOnUiThread {
            binding.connectionStatus.text = status
        }
    }
}