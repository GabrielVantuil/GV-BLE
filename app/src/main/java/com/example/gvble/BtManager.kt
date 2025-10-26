package com.example.gvble

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@SuppressLint("MissingPermission")
abstract class BtManager : ComponentActivity() {
    protected val tag = "BtManager"

    lateinit var bluetoothGatt: BluetoothGatt
    private var isScanning = false
    var isConnected = false
    abstract var filters: ArrayList<ScanFilter>
    open val scanMode: Int = ScanSettings.SCAN_MODE_LOW_LATENCY

    abstract fun onConnected(gatt: BluetoothGatt)
    abstract fun onDisconnected(gatt: BluetoothGatt)
    abstract fun onConnectionError(gatt: BluetoothGatt, status: Int)
    abstract fun onServicesDiscovered(gatt: BluetoothGatt)
    abstract fun onCharacteristicReadCallback(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, payload: ByteArray, status: Int)
    abstract fun onCharacteristicWriteCallback(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int)
    abstract fun onScanStatusUpdate(status: String)

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            with(result.device) {
                val now = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault()).format(Instant.now())
                val data = "$now Found device: ${name ?: "Unnamed"}, ($address)"
                Log.i(tag, data)
                connectGatt(this@BtManager, false, gattCallback)
            }
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.w(tag, "Successfully connected to $deviceAddress")
                    bluetoothGatt = gatt
                    onConnected(gatt)
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.w(tag, "Successfully disconnected from $deviceAddress")
                    onDisconnected(gatt)
                    gatt.close()
                }
            } else {
                onConnectionError(gatt, status)
                Log.w(tag, "Error $status encountered for $deviceAddress! Disconnecting...")
                gatt.close()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            with(gatt) {
                Log.w(tag, "Discovered ${services.size} services for ${device.address}")
                onServicesDiscovered(gatt)
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, payload: ByteArray, status: Int) {
            super.onCharacteristicRead(gatt, characteristic, payload, status)
            onCharacteristicReadCallback(gatt, characteristic, payload, status)
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            with(characteristic) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        onCharacteristicWriteCallback(gatt, characteristic, status)
                    }

                    BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> {
                        Log.e(tag, "Write exceeded connection ATT MTU!")
                    }

                    BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> {
                        Log.e(tag, "Write not permitted for ${this.uuid}!")
                    }

                    else -> {
                        Log.e(tag, "Characteristic write failed for ${this.uuid}, error: $status")
                    }
                }
            }
        }
    }

    @Suppress("UNUSED")
    protected fun disconnect(gatt: BluetoothGatt) {
        gatt.disconnect()
    }

    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic, payload: ByteArray) {
        val writeType = when {
            characteristic.isWritable() -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            characteristic.isWritableWithoutResponse() -> BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            else -> error("Characteristic ${characteristic.uuid} cannot be written to")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bluetoothGatt.writeCharacteristic(characteristic, payload, writeType)
        } else {
            legacyCharacteristicWrite(characteristic, payload, writeType)
        }
    }

    private fun BluetoothGattCharacteristic.isWritable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)

    private fun BluetoothGattCharacteristic.isWritableWithoutResponse(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)

    private fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean {
        return properties and property != 0
    }

    @Suppress("DEPRECATION")
    private fun legacyCharacteristicWrite(
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        writeType: Int
    ) {
        characteristic.writeType = writeType
        characteristic.value = value
        bluetoothGatt.writeCharacteristic(characteristic)
    }

    private fun hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permissionType) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasRequiredBluetoothPermissions(): Boolean {
        return hasPermission(Manifest.permission.BLUETOOTH_SCAN) && hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
    }

    open fun startBleScan(): Boolean {
        if (!hasRequiredBluetoothPermissions()) {
            requestRelevantRuntimePermissions()
            return false
        }
        onScanStatusUpdate("Scanning")
        val scanSettings = ScanSettings.Builder().setScanMode(scanMode).build()
        bleScanner.startScan(filters, scanSettings, scanCallback)
        isScanning = true
        return true
    }

    @Suppress("UNUSED")
    fun stopBleScan() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }
        if (isScanning) {
            bleScanner.stopScan(scanCallback)
            isScanning = false
        }
    }

    private val permissionRequestLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted && hasRequiredBluetoothPermissions()) {
            startBleScan()
        } else {
            onScanStatusUpdate("Permission required")
        }
    }

    private fun requestRelevantRuntimePermissions() {
        val permissionsToRequest = arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        permissionRequestLauncher.launch(permissionsToRequest)
    }

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val bluetoothEnablingResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Bluetooth is enabled
        } else {
            promptEnableBluetooth()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!bluetoothAdapter.isEnabled) {
            promptEnableBluetooth()
        }
    }

    private fun promptEnableBluetooth() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            return
        }
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            bluetoothEnablingResult.launch(enableBtIntent)
        }
    }
}
