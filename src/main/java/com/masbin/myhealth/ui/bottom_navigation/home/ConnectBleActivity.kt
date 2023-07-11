package com.masbin.myhealth.ui.bottom_navigation.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.clj.fastble.scan.BleScanRuleConfig
import com.masbin.myhealth.MainAdapterActivity
import com.masbin.myhealth.R
import com.masbin.myhealth.databinding.ActivityConnectBleBinding

@Suppress("DEPRECATION")
class ConnectBleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConnectBleBinding
    private lateinit var textViewSmartband: TextView
    private lateinit var editTextBluetoothAddress: EditText
    private lateinit var recyclerViewDevices: RecyclerView
    private lateinit var deviceAdapter: BluetoothDeviceAdapter
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bleDevice: BleDevice? = null
    private var isConnected: Boolean = false
    private val deviceList: ArrayList<BluetoothDevice> = ArrayList()
    private val bleScanCallback: BleScanCallback = object : BleScanCallback() {
        override fun onScanStarted(success: Boolean) {
            // Callback when the scan starts
        }

        @SuppressLint("SetTextI18n")
        fun onLeScan(device: BluetoothDevice, rssi: Int, scanRecord: ByteArray?) {
            // Callback when a BLE device is found
            runOnUiThread {
                val bleDevice = BleDevice(device, rssi, scanRecord, System.currentTimeMillis())
                if (bleDevice.name?.contains("Xiaomi", true) == true) {
                    this@ConnectBleActivity.bleDevice = bleDevice
                    textViewSmartband.text =
                        "Smart band Name: ${bleDevice.name}\nMAC Address: ${bleDevice.mac}"
                }
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onScanning(bleDevice: BleDevice) {
            // Callback when the scan is ongoing
            runOnUiThread {
                // Add the scanned device to the list
                deviceList.add(bleDevice.device)
                deviceAdapter.notifyDataSetChanged()
            }
        }

        override fun onScanFinished(scanResultList: List<BleDevice>) {
            // Callback when the scan is finished
        }

        fun onScanFailed(bleException: BleException) {
            // Callback when the scan fails
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivityConnectBleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        this.textViewSmartband = findViewById(R.id.nameSmartband)
        this.editTextBluetoothAddress = findViewById(R.id.editTextBluetoothAddress)
        this.recyclerViewDevices = findViewById(R.id.recyclerViewDevices)
        this.deviceAdapter = BluetoothDeviceAdapter(deviceList)
        recyclerViewDevices.layoutManager = LinearLayoutManager(this)
        recyclerViewDevices.adapter = deviceAdapter
        this.binding.btnConnectSmartband.setOnClickListener { connectSmartband(bleDevice) }
        this.binding.btnDisconnectSmartband.setOnClickListener { disconnectSmartband() }
        this.binding.btnFindSmartband.setOnClickListener { findSmartband() }
        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val isConnected = sharedPreferences.getBoolean("isConnected", false)
        if (isConnected) {
            val smartbandName = sharedPreferences.getString("smartbandName", "")
            val smartbandMac = sharedPreferences.getString("smartbandMac", "")

            if (smartbandName != null && smartbandMac != null) {
                textViewSmartband.text = "Smartband Name: $smartbandName"
            }
        }

        // Initialize Bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

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
            .setConnectOverTime(20000).operateTimeout = 5000
    }

    // function to find the smartband
    @SuppressLint("NotifyDataSetChanged")
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
            BleManager.getInstance().scan(bleScanCallback)
            // Clear the device list before scanning starts
            deviceList.clear()
            deviceAdapter.notifyDataSetChanged()
        }
    }

    //function to connect to the smartband
    private fun connectSmartband(bleDevice: BleDevice?) {
        val bluetoothAddress = editTextBluetoothAddress.text.toString()
        if (bluetoothAddress.isNotEmpty()) {
            val device = bleDevice?.device ?: bluetoothAdapter.getRemoteDevice(bluetoothAddress)
            if (device != null) {
                // Connect to the smartband using BleManager
                BleManager.getInstance().connect(device.address, object : BleGattCallback() {
                    override fun onStartConnect() {
                        // Callback when the connection starts
                    }

                    override fun onConnectFail(bleDevice: BleDevice, exception: BleException) {
                        // Callback when the connection fails
                    }

                    override fun onConnectSuccess(
                        bleDevice: BleDevice,
                        gatt: BluetoothGatt,
                        status: Int
                    ) {
                        runOnUiThread {
                            Toast.makeText(this@ConnectBleActivity, "Smartband connected!", Toast.LENGTH_SHORT).show()
                            val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putString("smartbandName", bleDevice.name)
                            editor.putString("smartbandMac", bleDevice.mac)
                            editor.putBoolean("isConnected", true)
                            editor.apply()

                            textViewSmartband.text = "Smartband Name: ${bleDevice.name}\nMAC Address: ${bleDevice.mac}"
                        }

                        val intent = Intent(this@ConnectBleActivity, MainAdapterActivity::class.java)
                        startActivity(intent)
                        // Callback when the connection is successful
                        // You can start reading/writing data here


                    }

                    override fun onDisConnected(
                        isActiveDisConnected: Boolean,
                        device: BleDevice,
                        gatt: BluetoothGatt,
                        status: Int
                    ) {
                        runOnUiThread {
                            // Update the isConnected status
                            isConnected = false
                            // Save the isConnected status in Shared Preferences
                            val sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putBoolean("isConnected", isConnected)
                            editor.apply()

                            textViewSmartband.text = "Cari Smartband"
                        }
                    }
                })
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun disconnectSmartband() {
        // Panggil fungsi untuk putuskan koneksi dengan smartband

        // Hentikan Service

        // Set status koneksi ke false
        val sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isConnected", false)
        editor.apply()

        textViewSmartband.text = "Cari Smartband"
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
