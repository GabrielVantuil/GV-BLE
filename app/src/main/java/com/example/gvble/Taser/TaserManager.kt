package com.example.gvble.Taser

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanFilter
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import com.example.gvble.BtManager
import com.example.gvble.TASER_SERVICE_UUID
import com.example.gvble.TASER_SET_TASER_CHAR_UUID
import com.example.gvble.databinding.ActivityTaserBinding


@SuppressLint("MissingPermission", "ClickableViewAccessibility")
class TaserManager() : BtManager(){
    private lateinit var binding: ActivityTaserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        TaserActivity(binding, this)
        startBleScan()

    }

    override var filters: ArrayList<ScanFilter> = arrayListOf(
        ScanFilter.Builder().setServiceUuid(ParcelUuid(TASER_SERVICE_UUID)).build())

    override fun onConnected(gatt: BluetoothGatt) {
        runOnUiThread {
            binding.connectionStatus.text = "Connected to ${gatt.device.name} (${gatt.device.address})"
        }
        bluetoothGatt = gatt
        bluetoothGatt.discoverServices()
        isConnected = true
    }
    private fun setBleButtonsEnabled(isEnabled: Boolean) {
        binding.taserOnBt.isEnabled = isEnabled
        binding.taserOffBt.isEnabled = isEnabled
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
    fun writeMotorCharacteristic(value : Int, timeout: Int = 0){
        if(!isConnected)    return
        val toSend =  byteArrayOf(
            value.toByte(),
            0, 0,
            0, 0,
            (timeout shr 24).toByte(), (timeout shr 16).toByte(), (timeout shr 8).toByte(), timeout.toByte()
        )
        val writeChangePayloadCharacteristic = bluetoothGatt.getService(TASER_SERVICE_UUID)
            .getCharacteristic(TASER_SET_TASER_CHAR_UUID)
        writeCharacteristic(writeChangePayloadCharacteristic, toSend)
    }

    override fun onScanStatusUpdate(status: String) {
        runOnUiThread {
            binding.connectionStatus.text = status
        }
    }
}