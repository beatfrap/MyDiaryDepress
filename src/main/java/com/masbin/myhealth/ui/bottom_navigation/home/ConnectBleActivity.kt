package com.masbin.myhealth.ui.bottom_navigation.home

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.masbin.myhealth.R

class ConnectBleActivity : AppCompatActivity() {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private val permissionsRequestCode: Int = 200
    private val REQUEST_ENABLE_BT = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect_ble)

        // Inisialisasi Bluetooth Adapter
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        // Cek dan minta izin Bluetooth
        checkBluetoothPermission()
    }

    private fun checkBluetoothPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                permissionsRequestCode
            )
        } else {
            initializeBluetooth()
        }
    }

    private fun initializeBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            // Meminta pengguna untuk mengaktifkan Bluetooth
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else {
            // Bluetooth sudah diaktifkan
            setupBluetoothScanner()
        }
    }

    private fun setupBluetoothScanner() {
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

        // Mulai mencari perangkat Bluetooth LE
        startScanning()
    }

    private fun startScanning() {
        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)

                // Mendapatkan informasi perangkat Bluetooth yang ditemukan
                val device: BluetoothDevice? = result?.device
                val deviceName: String? = device?.name
                val deviceAddress: String? = device?.address

                // Lakukan tindakan sesuai dengan perangkat yang ditemukan
                // Misalnya, memeriksa apakah perangkat adalah smartband Xiaomi dan menghubungkannya

                // ...

            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                // Penanganan kesalahan saat pemindaian
            }
        }

        // Mulai pemindaian BLE
        bluetoothLeScanner.startScan(scanCallback)
    }
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)

            // Mendapatkan informasi perangkat Bluetooth yang ditemukan
            val device: BluetoothDevice? = result?.device
            val deviceName: String? = device?.name
            val deviceAddress: String? = device?.address

            // Lakukan tindakan sesuai dengan perangkat yang ditemukan
            // Misalnya, memeriksa apakah perangkat adalah smartband Xiaomi dan menghubungkannya

            // ...
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            // Penanganan kesalahan saat pemindaian
        }
    }


    private fun stopScanning() {
        bluetoothLeScanner.stopScan(scanCallback)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            permissionsRequestCode -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initializeBluetooth()
                } else {
                    // Izin ditolak, penanganan kasus izin tidak diberikan
                }
            }
        }
    }
}