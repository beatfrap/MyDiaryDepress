package com.masbin.myhealth.ui.bottom_navigation.home

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.masbin.myhealth.R

class BluetoothDeviceAdapter(private val deviceList: List<BluetoothDevice>) :
    RecyclerView.Adapter<BluetoothDeviceAdapter.DeviceViewHolder>() {

    class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceName: TextView = itemView.findViewById(R.id.deviceName)
        val deviceAddress: TextView = itemView.findViewById(R.id.deviceAddress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_bluetooth_device, parent, false)
        return DeviceViewHolder(itemView)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = deviceList[position]
        holder.deviceName.text = device.name
        holder.deviceAddress.text = device.address
    }

    override fun getItemCount(): Int {
        return deviceList.size
    }
}
