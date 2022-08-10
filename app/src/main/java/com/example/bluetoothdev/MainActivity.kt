package com.example.bluetoothdev

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.InputDevice.getDevice
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.widget.NestedScrollView


class MainActivity : AppCompatActivity() {

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bstatusTv: TextView = findViewById(R.id.btStatusTv)
        val btImg: ImageView = findViewById(R.id.btImg)
        val onBtn: Button = findViewById(R.id.turnOnBtn)
        val offBtn: Button = findViewById(R.id.turnOffBtn)
        val discoverBtn: Button = findViewById(R.id.discoverBtn)
        val bleLayout: LinearLayout = findViewById(R.id.bleLayout)
        val scanBtn: ImageButton = findViewById(R.id.scanBtn)
        val scroller: NestedScrollView = findViewById(R.id.scroller)
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter



        val requestDiscoverable  = registerForActivityResult(
            StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Device is discoverable", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Could not make device discoverable", Toast.LENGTH_SHORT).show()
            }
        }

        // request Bluetooth to be turned on via registerForActivityResult
        val requestBluetooth  = registerForActivityResult(
            StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                btImg.setImageResource(R.drawable.ic_bluetooth)
                Toast.makeText(this, "Bluetooth enabled", Toast.LENGTH_SHORT).show()
                bstatusTv.setText(R.string.bluetooth_Av)
            } else {
                Toast.makeText(this, "Could not enable Bluetooth", Toast.LENGTH_SHORT).show()
            }
        }

        val requestMultiplePermissions =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                permissions.entries.forEach {
                    Log.d("test006", "${it.key} = ${it.value}")
                }
            }

        val blescancallback = object :ScanCallback() {
            @SuppressLint("SetTextI18n")
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                val hashedId = result.device?.address.toString().hashCode()
                val isAppendedTv = bleLayout.findViewById<TextView>(hashedId)
                if (result.device?.name.toString() != "null" && isAppendedTv == null) {
                    val newDeviceTv = TextView(this@MainActivity)
                    newDeviceTv.id = hashedId
                    newDeviceTv.setTextColor(Color.BLACK)
                    bleLayout.addView(newDeviceTv)
                    Log.d("Log", "onScanResult")
                    Log.d("Log","onScanResult: ${result.device?.address} - ${result.device?.name}")
                }
                if (isAppendedTv != null){
                    isAppendedTv.text = result.device?.name.toString() + "\n" +
                            result.device?.address.toString() +
                            "                              RSSI: " +
                            result.rssi.toString() + "\n"

                }
                else{
                    Log.d("Log","onScanResult: REEEEEE")
                }

            }

            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                super.onBatchScanResults(results)
                Log.d("Log", "SonBatchScanResults")
                Log.d("Log","onBatchScanResults:${results.toString()}")
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                Log.d("Log", "onScanFailed")
                Log.d("Log", "onScanFailed: $errorCode")
            }
        }


        //Set header image if Bluetooth depending on its current state
        if (bluetoothAdapter?.isEnabled == false) {
            btImg.setImageResource(R.drawable.ic_bluetooth_off)
            bstatusTv.setText(R.string.bluetooth_nAv)
        } else {
            btImg.setImageResource(R.drawable.ic_bluetooth)
            bstatusTv.setText(R.string.bluetooth_Av)
        }

        //Button to enable Bluetooth, requests permissions if not already enabled
        onBtn.setOnClickListener {
            if (bluetoothAdapter?.isEnabled == true) {
                Toast.makeText(this, "Bluetooth already enabled", Toast.LENGTH_SHORT).show()
            }
            else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                    == PackageManager.PERMISSION_DENIED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_DENIED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            requestMultiplePermissions.launch(
                                arrayOf(
                                    Manifest.permission.BLUETOOTH_SCAN,
                                    Manifest.permission.BLUETOOTH_CONNECT
                                )
                            )
                    }
                }
                else {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    requestBluetooth.launch(enableBtIntent)
                }
            }
        }


        //Disable bluetooth button
        offBtn.setOnClickListener {
            if (bluetoothAdapter?.isEnabled == false) {
                Toast.makeText(this, "Bluetooth already disabled", Toast.LENGTH_SHORT).show()
            }
            else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                    == PackageManager.PERMISSION_DENIED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_DENIED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        requestMultiplePermissions.launch(
                            arrayOf(
                                Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.BLUETOOTH_CONNECT
                            )
                        )
                    }
                }
                else {
                    bluetoothAdapter?.disable()
                    btImg.setImageResource(R.drawable.ic_bluetooth_off)
                    bstatusTv.setText(R.string.bluetooth_nAv)
                    Toast.makeText(this, "Bluetooth disabled", Toast.LENGTH_SHORT).show()
                }
            }
        }


        // Make Device Discoverable button listener
        discoverBtn.setOnClickListener {
            if (bluetoothAdapter?.isEnabled == false) {
                Toast.makeText(this, "Bluetooth is disabled", Toast.LENGTH_SHORT).show()
            }else{
                if (bluetoothAdapter?.isDiscovering == false) {
                    Toast.makeText(this, "Making device discoverable", Toast.LENGTH_SHORT).show()
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                        == PackageManager.PERMISSION_DENIED ||
                        ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                        == PackageManager.PERMISSION_DENIED) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            requestMultiplePermissions.launch(
                                arrayOf(
                                    Manifest.permission.BLUETOOTH_SCAN,
                                    Manifest.permission.BLUETOOTH_CONNECT
                                )
                            )
                        }
                    }
                    else {
                        val enableBtDiscoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
                        requestDiscoverable.launch(enableBtDiscoverableIntent)
                    }
                }
            }
        }


        //Scan button listener
        scanBtn.setOnClickListener {
            if (bluetoothAdapter?.isEnabled == false) {
                Toast.makeText(this, "Please enable Bluetooth", Toast.LENGTH_SHORT).show()
            }
            else{
                if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        0
                    )
                }else{
                    Handler(Looper.getMainLooper()).postDelayed(
                        { bluetoothAdapter?.bluetoothLeScanner?.stopScan(blescancallback)},
                        20000)

                    bluetoothAdapter?.bluetoothLeScanner?.startScan(blescancallback)
                }
            }
        }
    }
}