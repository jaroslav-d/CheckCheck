package ru.jaroslavd.checkcheck

import android.Manifest.permission.CAMERA
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import java.util.concurrent.Executors

class CheckActivity : AppCompatActivity() {

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1
        private const val WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 2
    }

    private var executor = Executors.newSingleThreadExecutor()
    private val previewView get() = findViewById<PreviewView>(R.id.viewFinder)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check)
        executor.shutdown()
        executor = Executors.newSingleThreadExecutor()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
            }
            if (checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(WRITE_EXTERNAL_STORAGE),
                    WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun startSearchBarcode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val cameraController = LifecycleCameraController(applicationContext)
            val scanner = BarcodeScanning.getClient(
                BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .build()
            )
            val analyzer = MlKitAnalyzer(
                listOf(scanner),
                0,
                executor
            ) {
                it.getValue(scanner)?.find { barcode ->
                    barcode.displayValue?.isNotBlank() ?: false
                }?.displayValue?.let { text ->
                    if (text.isNotBlank()) {
                        previewView.post { Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show() }
                        val i = Intent(this, SearchResultActivity::class.java)
                        i.putExtra("query", text)
                        startActivity(i)
                        scanner.close()
                    }
                }
            }

            cameraController.setImageAnalysisAnalyzer(executor, analyzer)
            cameraController.bindToLifecycle(this)
            previewView.controller = cameraController
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.first() == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(applicationContext, "It have granted", Toast.LENGTH_SHORT).show()
                } else {
                    finish()
                }
            }
            WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.first() == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(applicationContext, "It have granted", Toast.LENGTH_SHORT).show()
                } else {
                    finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startSearchBarcode()
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
    }
}