package com.example.gvble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanFilter
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import androidx.activity.ComponentActivity
import com.example.gvble.databinding.ActivityPovDisplayBinding

private const val LABEL_SEARCHING = "Scanning"

@SuppressLint("MissingPermission")
class PovDisplayManager(context: Context, activity: ComponentActivity, var view: ActivityPovDisplayBinding) : BtManager(context, activity){
    val totalLeds = 44

    override var filters: ArrayList<ScanFilter> = arrayListOf(ScanFilter.Builder().setServiceUuid(ParcelUuid(POV_DISPLAY_SERVICE_UUID)).build())
    override fun onConnected(gatt: BluetoothGatt) {
        setConnectionStatus("Connected to ${gatt.device.name} (${gatt.device.address})", true, null)
        bluetoothGatt = gatt
        isConnected = true
    }
    override fun onDisconnected(gatt: BluetoothGatt) {
        setConnectionStatus("Disconnected - $LABEL_SEARCHING", false, null)
        isConnected = false
    }
    override fun onConnectionError(gatt: BluetoothGatt, status: Int) {
        setConnectionStatus(LABEL_SEARCHING,  false, null)
        isConnected = false
    }
    override fun onServicesDiscovered(gatt: BluetoothGatt) {
    }
    override fun onCharacteristicReadCallback(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, payload: ByteArray, status: Int) {
        when (status) {
            BluetoothGatt.GATT_SUCCESS -> {
                when(characteristic.uuid){
                    TORCH_READ_LDR_CHAR_UUID -> {
                        Log.i(TAG, "Read LDR: " + (payload[0] * 256 + payload[1]))
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
        }
    }

    fun writeModeCharacteristic(mode: Int){
        if(!isConnected)    return
        val writeChangePayloadCharacteristic = bluetoothGatt.getService(POV_DISPLAY_SERVICE_UUID).getCharacteristic(POV_DISPLAY_MODE_CHAR_UUID)
        writeCharacteristic(writeChangePayloadCharacteristic, byteArrayOf(mode.toByte()))
    }
    fun writeSendTextCharacteristic(text : ByteArray){
        if(!isConnected)    return
        val writeChangePayloadCharacteristic = bluetoothGatt.getService(POV_DISPLAY_SERVICE_UUID).getCharacteristic(POV_DISPLAY_SET_TEXT_CHAR_UUID)
        writeCharacteristic(writeChangePayloadCharacteristic, text)
    }
    fun writeSetLedsCharacteristic(leds: ByteArray){
        if(!isConnected)    return
        val writeChangePayloadCharacteristic = bluetoothGatt.getService(POV_DISPLAY_SERVICE_UUID).getCharacteristic(POV_DISPLAY_SET_LEDS_CHAR_UUID)
        writeCharacteristic(writeChangePayloadCharacteristic, leds)
    }
    fun writeSetParamsCharacteristic(params: ByteArray){
        if(!isConnected)    return
        val writeChangePayloadCharacteristic = bluetoothGatt.getService(POV_DISPLAY_SERVICE_UUID).getCharacteristic(POV_DISPLAY_SET_PARAMS_UUID)
        writeCharacteristic(writeChangePayloadCharacteristic, params)
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
}