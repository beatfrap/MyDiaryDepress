package com.masbin.myhealth.ui.bottom_navigation.home

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.clj.fastble.scan.BleScanRuleConfig
import com.masbin.myhealth.R
import com.masbin.myhealth.databinding.ActivityConnectBleBinding

class ConnectBleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConnectBleBinding
    private lateinit var textViewSmartband: TextView
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bleDevice: BleDevice? = null
    private val bleScanCallback: BleScanCallback = object : BleScanCallback() {
        override fun onScanStarted(success: Boolean) {
            // Callback when the scan starts
        }

        fun onLeScan(device: BluetoothDevice, rssi: Int, scanRecord: ByteArray?) {
            // Callback when a BLE device is found
            runOnUiThread {
                val bleDevice = BleDevice(device, rssi, scanRecord, System.currentTimeMillis())
                if (bleDevice.name?.contains("Xiaomi", true) == true) {
                    this@ConnectBleActivity.bleDevice = bleDevice
                    textViewSmartband.text = "Smartband Name: ${bleDevice.name}\nMAC Address: ${bleDevice.mac}"
                }
            }
        }

        override fun onScanning(bleDevice: BleDevice) {
            // Callback when the scan is ongoing
        }

        override fun onScanFinished(scanResultList: List<BleDevice>) {
            // Callback when the scan is finished
        }

        fun onScanFailed(bleException: BleException) {
            // Callback when the scan fails
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivityConnectBleBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_connect_ble)
        supportActionBar?.hide()
        this.textViewSmartband = findViewById(R.id.nameSmartband)
        this.binding.btnConnectSmartband.setOnClickListener { connectSmartband(bleDevice) }
        this.binding.btnFindSmartband.setOnClickListener { findSmartband() }

        // Initialize Bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        // Check if Bluetooth is supported on the device
        if (bluetoothAdapter == null) {
            // Bluetooth is not supported
            // Handle the case accordingly
        }

        // Check for necessary permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        }

        // Initialize BleManager
        BleManager.getInstance().init(application)
        BleManager.getInstance()
            .enableLog(true)
            .setReConnectCount(1, 5000)
            .setConnectOverTime(20000)
            .setOperateTimeout(5000)
    }

    private fun findSmartband() {
        // Check if Bluetooth is enabled
        if (!bluetoothAdapter.isEnabled) {
            // Bluetooth is not enabled, request to enable it
            // Handle the result using onActivityResult
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else {
            // Bluetooth is enabled, start scanning
            val scanRuleConfig = BleScanRuleConfig.Builder()
                .setAutoConnect(false)
                .setScanTimeOut(10000)
                .build()
            BleManager.getInstance().initScanRule(scanRuleConfig)
            BleManager.getInstance().scan(object : BleScanCallback() {
                override fun onScanStarted(success: Boolean) {
                    // Callback when the scan starts
                }

                override fun onScanning(bleDevice: BleDevice) {
                    // Callback when the scan is ongoing
                    runOnUiThread {
                        textViewSmartband.text = "Smartband Name: ${bleDevice.name}\nMAC Address: ${bleDevice.mac}"
                    }
                }

                override fun onScanFinished(scanResultList: List<BleDevice>) {
                    // Callback when the scan is finished
                }

                fun onScanFailed(bleException: BleException) {
                    // Callback when the scan fails
                }
            })
        }
    }


    private fun connectSmartband(bleDevice: BleDevice?) {
        if (bleDevice != null) {
            // Connect to the smartband using BleManager
            BleManager.getInstance().connect(bleDevice, object : BleGattCallback() {
                override fun onStartConnect() {
                    // Callback when the connection starts
                }

                override fun onConnectFail(bleDevice: BleDevice, exception: BleException) {
                    // Callback when the connection fails
                }

                override fun onConnectSuccess(bleDevice: BleDevice, gatt: BluetoothGatt, status: Int) {
                    // Callback when the connection is successful
                    // You can start reading/writing data here
                }

                override fun onDisConnected(
                    isActiveDisConnected: Boolean,
                    device: BleDevice,
                    gatt: BluetoothGatt,
                    status: Int
                ) {
                    // Callback when the device is disconnected
                    // You can handle reconnection or other actions here
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        BleManager.getInstance().disconnectAllDevice()
        BleManager.getInstance().destroy()
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_ENABLE_BT = 2
    }
}
