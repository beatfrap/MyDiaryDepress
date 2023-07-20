package com.masbin.myhealth.ui.bottom_navigation.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
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
import java.util.UUID
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException


@Suppress("DEPRECATION")
class ConnectBleActivity : AppCompatActivity() {

    private lateinit var user_id: String
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
        user_id = sharedPreferences.getString("user_id", "") ?: ""
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
                            // Ambil data detak jantung
                            val heartRate = readHeartRateData(gatt)

                            // Ambil data waktu tidur
                            val sleepData = readSleepData(gatt)

                            // Ambil data tingkat stres
                            val stressLevel = readStressLevel(gatt)

                            sendHeartRateData(user_id, heartRate)
                            sendSleepData(user_id, sleepData)
                            sendStressData(user_id, stressLevel)

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

                            textViewSmartband.text = "Tidak Ada Smartband Yang Terhubung"
                        }
                    }
                })
            }
        }
    }

    private fun sendHeartRateData(user_id: String, heartRate: Int) {
        val url = "http://your-server-url/post/heartrate/$user_id"
        val requestBody = FormBody.Builder()
            .add("heartrate", heartRate.toString())
            .build()
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    // Tanggapi respon sukses dari server
                    val responseBody = response.body?.string()
                    Log.d("HeartRatePost", responseBody ?: "")
                } else {
                    // Tanggapi respon gagal dari server
                    Log.d("HeartRatePost", "Gagal mengirim data detak jantung")
                }
            }
        })
    }

    private fun sendSleepData(user_id: String, sleepData: String) {
        val url = "http://your-server-url/post/sleep/$user_id"
        val requestBody = FormBody.Builder()
            .add("sleep", sleepData)
            .build()
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    // Tanggapi respon sukses dari server
                    val responseBody = response.body?.string()
                    Log.d("SleepDataPost", responseBody ?: "")
                } else {
                    // Tanggapi respon gagal dari server
                    Log.d("SleepDataPost", "Gagal mengirim data waktu tidur")
                }
            }
        })
    }

    private fun sendStressData(user_id: String, stressLevel: String) {
        val url = "http://your-server-url/post/stress/$user_id"
        val requestBody = FormBody.Builder()
            .add("stress", stressLevel)
            .build()
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    // Tanggapi respon sukses dari server
                    val responseBody = response.body?.string()
                    Log.d("StressDataPost", responseBody ?: "")
                } else {
                    // Tanggapi respon gagal dari server
                    Log.d("StressDataPost", "Gagal mengirim data tingkat stres")
                }
            }
        })
    }

    // Fungsi untuk mengambil data detak jantung
    private fun readHeartRateData(gatt: BluetoothGatt): Int {
        // Implementasikan kode untuk membaca data detak jantung dari smartband menggunakan perpustakaan Huami (Mi Fit)
        // Pastikan Anda telah menginisialisasi dan menghubungkan perpustakaan Huami (Mi Fit) dengan benar

        // Contoh kode sederhana untuk mendapatkan data detak jantung
        val HEART_RATE_SERVICE_UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
        val HEART_RATE_MEASUREMENT_UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
        val heartRateCharacteristic = gatt.getService(HEART_RATE_SERVICE_UUID)?.getCharacteristic(HEART_RATE_MEASUREMENT_UUID)
        val heartRateValue = heartRateCharacteristic?.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1) ?: -1

        Log.d("HeartRate", "Detak Jantung: $heartRateValue")

        return heartRateValue
    }

    // Fungsi untuk mengambil data waktu tidur
    private fun readSleepData(gatt: BluetoothGatt): String {
        // Implementasikan kode untuk membaca data waktu tidur dari smartband menggunakan perpustakaan Huami (Mi Fit)
        // Pastikan Anda telah menginisialisasi dan menghubungkan perpustakaan Huami (Mi Fit) dengan benar

        // Contoh kode sederhana untuk mendapatkan data waktu tidur
        val SLEEP_SERVICE_UUID = UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb")
        val SLEEP_DATA_UUID = UUID.fromString("00000009-0000-3512-2118-0009af100700")
        val sleepCharacteristic = gatt.getService(SLEEP_SERVICE_UUID)?.getCharacteristic(SLEEP_DATA_UUID)
        val sleepValue = sleepCharacteristic?.getStringValue(0)

        Log.d("Sleep", "Waktu Tidur: $sleepValue")
        return sleepValue ?: ""
    }

    // Fungsi untuk mengambil data tingkat stres
    private fun readStressLevel(gatt: BluetoothGatt): String {
        // Implementasikan kode untuk membaca data tingkat stres dari smartband menggunakan perpustakaan Huami (Mi Fit)
        // Pastikan Anda telah menginisialisasi dan menghubungkan perpustakaan Huami (Mi Fit) dengan benar

        // Contoh kode sederhana untuk mendapatkan data tingkat stres
        val STRESS_SERVICE_UUID = UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb")
        val STRESS_LEVEL_UUID = UUID.fromString("00000008-0000-3512-2118-0009af100700")
        val stressCharacteristic = gatt.getService(STRESS_SERVICE_UUID)?.getCharacteristic(STRESS_LEVEL_UUID)
        val stressValue = stressCharacteristic?.getStringValue(0)

        Log.d("Stress", "Tingkat Stres: $stressValue")

        return stressValue ?: ""
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

        textViewSmartband.text = "Tidak Ada Smartband Yang Terhubung"
    }

    override fun onDestroy() {
        super.onDestroy()
        BleManager.getInstance().disconnectAllDevice()
        BleManager.getInstance().destroy()
    }

    override fun onBackPressed() {
        // Hapus pemanggilan super.onBackPressed()
        // super.onBackPressed()

        // Kembali ke tampilan sebelumnya, misalnya MainActivity
        val intent = Intent(this, MainAdapterActivity::class.java)
        startActivity(intent)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Hapus pemanggilan super.onKeyDown() jika ada
            // super.onKeyDown(keyCode, event)

            // Kembali ke tampilan sebelumnya, misalnya MainActivity
            val intent = Intent(this, MainAdapterActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_ENABLE_BT = 2
    }
}
