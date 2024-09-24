package com.example.flashlight

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract.Colors
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.*

class MainActivity : AppCompatActivity() {
    private lateinit var codeScanner: CodeScanner
    private lateinit var scanResultTextView: TextView
    private lateinit var openLinkButton: Button
    private val CAMERA_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)
        scanResultTextView = findViewById(R.id.scan_result)
        openLinkButton = findViewById(R.id.open_link_button)

        // Check for camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
        } else {
            setupCodeScanner(scannerView)
        }


        openLinkButton.setOnClickListener {
            val uri = Uri.parse(scanResultTextView.text.toString())
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }

    private fun setupCodeScanner(scannerView: CodeScannerView) {
        codeScanner = CodeScanner(this, scannerView)
        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.ALL_FORMATS
        codeScanner.autoFocusMode = AutoFocusMode.SAFE
        codeScanner.scanMode = ScanMode.SINGLE
        codeScanner.isAutoFocusEnabled = true
        codeScanner.isFlashEnabled = false

        // Callbacks
        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                Toast.makeText(this, "Scan result: ${it.text}", Toast.LENGTH_LONG).show()
                scanResultTextView.text = it.text
                scanResultTextView.visibility = View.VISIBLE
                 if (isValidUrl(it.text)) {
                     openLinkButton.visibility = View.VISIBLE
                     scanResultTextView.setTextColor(Color.BLUE)
                } else {
                     openLinkButton.visibility = View.GONE
                     scanResultTextView.setTextColor(Color.BLACK)

                 }
            }
        }

        // Error callback
        codeScanner.errorCallback = ErrorCallback {
            runOnUiThread {
                Toast.makeText(applicationContext, "Camera initialization error: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }

        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    private fun isValidUrl(url: String): Boolean {
        return url.startsWith("http://") || url.startsWith("https://")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupCodeScanner(findViewById(R.id.scanner_view))
            } else {
                Toast.makeText(this, "Camera permission is required to use the scanner", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }
}