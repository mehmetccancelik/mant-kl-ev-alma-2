package com.evanaliz

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.evanaliz.accessibility.ScreenDataAccessibilityService
import com.evanaliz.ui.EvAnalizFloatingService

/**
 * Ana Aktivite
 * 
 * İzin kontrolü ve servis başlatma.
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val OVERLAY_PERMISSION_REQUEST = 1000
        private const val TAG = "EvAnaliz"
    }

    private lateinit var statusText: TextView
    private lateinit var startButton: Button
    private lateinit var accessibilityButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.status_text)
        startButton = findViewById(R.id.start_button)
        accessibilityButton = findViewById(R.id.accessibility_button)

        startButton.setOnClickListener {
            Log.d(TAG, "BAŞLAT button clicked")
            checkPermissionsAndStart()
        }

        accessibilityButton.setOnClickListener {
            openAccessibilitySettings()
        }

        findViewById<Button>(R.id.exit_button).setOnClickListener {
            finishAffinity()
            System.exit(0)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "MainActivity onResume()")
        updateStatus()
    }

    private fun updateStatus() {
        val overlayOk = Settings.canDrawOverlays(this)
        val accessibilityOk = ScreenDataAccessibilityService.isServiceRunning()
        
        Log.d(TAG, "updateStatus() - overlayOk: $overlayOk, accessibilityOk: $accessibilityOk")

        val status = buildString {
            appendLine("📱 Durum Kontrolü")
            appendLine()
            appendLine(if (overlayOk) "✅ Overlay izni: Verildi" else "❌ Overlay izni: Gerekli")
            appendLine(if (accessibilityOk) "✅ Erişilebilirlik: Aktif" else "❌ Erişilebilirlik: Kapalı")
            appendLine()
            if (overlayOk && accessibilityOk) {
                appendLine("🟢 Hazır! BAŞLAT'a tıklayın.")
            } else {
                appendLine("🔴 İzinleri verin.")
            }
        }

        statusText.text = status

        val buttonEnabled = overlayOk && accessibilityOk
        startButton.isEnabled = buttonEnabled
        Log.d(TAG, "Button enabled: $buttonEnabled")
    }

    private fun checkPermissionsAndStart() {
        // Overlay izni kontrolü
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST)
            return
        }

        // Accessibility kontrolü
        if (!ScreenDataAccessibilityService.isServiceRunning()) {
            openAccessibilitySettings()
            return
        }

        // Servisi başlat
        val intent = Intent(this, EvAnalizFloatingService::class.java)
        startForegroundService(intent)
        
        // Ana ekrana dön
        moveTaskToBack(true)
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_PERMISSION_REQUEST) {
            updateStatus()
        }
    }
}
