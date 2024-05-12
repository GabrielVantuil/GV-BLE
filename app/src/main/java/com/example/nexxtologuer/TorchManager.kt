package com.example.gvble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanFilter
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import com.example.gvble.databinding.ActivityMainBinding
import com.example.nexxtologuer.utils.byteArrayToLong
import com.example.nexxtologuer.utils.intToByteArray
import java.util.UUID

const val LABEL_SEARCHING = "Scanning"
const val UUID_BASE = "4756-616e-7475-696c5f424c45"//"4756-616e-7475-696c5f424c45" = {0x47, 0x56, 0x61, 0x6e, 0x74, 0x75, 0x69, 0x6c, 0x5f, 0x42, 0x4c, 0x45}= "GVantuil_BLE"
val TORCH_SERVICE_UUID: UUID =                UUID.fromString("00000000-$UUID_BASE")
val TORCH_LOCK_CHAR_UUID: UUID =              UUID.fromString("00000001-$UUID_BASE")
val TORCH_LED_POWER_CHAR_UUID: UUID =         UUID.fromString("00000002-$UUID_BASE")
val TORCH_LED_PWM_CHAR_UUID: UUID =           UUID.fromString("00000003-$UUID_BASE")
val TORCH_READ_LDR_CHAR_UUID: UUID =          UUID.fromString("00000006-$UUID_BASE")

@SuppressLint("MissingPermission")
class TorchManager(context: Context, activity: ComponentActivity, var view: ActivityMainBinding) : BtManager(context, activity){
    override var filters: ArrayList<ScanFilter> = arrayListOf(ScanFilter.Builder().setServiceUuid(ParcelUuid(TORCH_SERVICE_UUID)).build())
    override fun onConnected(gatt: BluetoothGatt) {
        setConnectionStatus("Connected to ${gatt.device.name} (${gatt.device.address})", true, null)
        bluetoothGatt = gatt
        isConnected = true
    }
    var shouldPowerOff: Boolean = false
    override fun onDisconnected(gatt: BluetoothGatt) {
        setConnectionStatus("Disconnected - $LABEL_SEARCHING", false, null)
        isConnected = false
    }
    override fun onConnectionError(gatt: BluetoothGatt, status: Int) {
        setConnectionStatus(LABEL_SEARCHING,  false, null)
        //"Error $status encountered for ${gatt.device.address}! Disconnecting..."
        isConnected = false
    }
    override fun onServicesDiscovered(gatt: BluetoothGatt) {
    }
    override fun onCharacteristicReadCallback(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, payload: ByteArray, status: Int) {
        when (status) {
            BluetoothGatt.GATT_SUCCESS -> {
                when(characteristic.uuid){
                    TORCH_READ_LDR_CHAR_UUID -> {
                        Log.i(TAG, "Read LDR: " + byteArrayToLong(payload, 0, 2))
                    }
                }
            }
            BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                Log.e("BluetoothGattCallback", "Read not permitted for ${characteristic.uuid}!")
            }
            else -> {
                Log.e("BluetoothGattCallback", "Characteristic read failed for ${characteristic.uuid}, error: $status")
            }
        }
    }
    override fun onCharacteristicWriteCallback(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
        when(characteristic.uuid){
            TORCH_LOCK_CHAR_UUID -> {

            }
            TORCH_LED_POWER_CHAR_UUID -> {
                if(shouldPowerOff){
                    writePowerCharacteristic(false)
                    shouldPowerOff = false
                }
            }
            TORCH_LED_PWM_CHAR_UUID -> {
                if(shouldPowerOff){
                    writePowerCharacteristic(false)
                    shouldPowerOff = false
                }
            }
        }
    }
    private fun readLdrCharacteristic(){
        if(!isConnected)    return
        var getPayloadCharacteristic = bluetoothGatt.getService(TORCH_SERVICE_UUID).getCharacteristic(TORCH_READ_LDR_CHAR_UUID)
        bluetoothGatt.readCharacteristic(getPayloadCharacteristic)
    }
    fun writeLockCharacteristic(lock : Int){
        if(!isConnected)    return
        var writeChangePayloadCharacteristic = bluetoothGatt.getService(TORCH_SERVICE_UUID).getCharacteristic(TORCH_LOCK_CHAR_UUID)
        writeCharacteristic(writeChangePayloadCharacteristic, byteArrayOf(lock.toByte()))
    }
    fun writePowerCharacteristic(power : Boolean, timeout: Int = 0){
        if(!isConnected)    return
        var toSend = byteArrayOf(
            if(power) 1 else 0,
            (timeout shr 24).toByte(), (timeout shr 16).toByte(), (timeout shr 8).toByte(), timeout.toByte()
        )
        var writeChangePayloadCharacteristic = bluetoothGatt.getService(TORCH_SERVICE_UUID).getCharacteristic(TORCH_LED_POWER_CHAR_UUID)
        writeCharacteristic(writeChangePayloadCharacteristic, toSend)
    }
    fun writePwmCharacteristic(freq : Int, duty : Int, timeout: Int = 0){
        if(!isConnected)    return
        var toSend = byteArrayOf(
            (freq shr 8).toByte(), freq.toByte(),
            (duty shr 8).toByte(), duty.toByte(),
            (timeout shr 24).toByte(), (timeout shr 16).toByte(), (timeout shr 8).toByte(), timeout.toByte()
        )
        var writeChangePayloadCharacteristic = bluetoothGatt.getService(TORCH_SERVICE_UUID).getCharacteristic(TORCH_LED_PWM_CHAR_UUID)
        writeCharacteristic(writeChangePayloadCharacteristic, toSend)
    }

    override fun startBleScan() : Boolean{
        return if(super.startBleScan()) {
            setConnectionStatus(LABEL_SEARCHING, null, null)
            true
        }
        else{
            setConnectionStatus("Permission required", null, null)
            false
        }
    }

    fun setConnectionStatus(status: String?, connected: Boolean?, rssi: Int?){
        runOnUiThread {
            if (status != null)     view.connectionStatus.text = status
//            when(connected){
//                true -> view.connectionParams.text = params
//            }
//            if (rssi != null)       view.readValues.text = read
            activity.setContentView(view.root)
        }
    }


//    fun updateLdrValue(newVal: Int){
//        runOnUiThread {
//            view.ldrVal.text = newVal
//        }
//    }
}