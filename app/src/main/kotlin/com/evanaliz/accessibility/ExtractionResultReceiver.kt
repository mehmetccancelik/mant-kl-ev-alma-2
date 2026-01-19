package com.evanaliz.accessibility

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager

/**
 * Extraction Sonuç Alıcısı
 * 
 * Accessibility Service'den gelen sonuçları dinleyen BroadcastReceiver.
 * Floating UI bu sınıfı kullanarak sonuçları alır.
 */
class ExtractionResultReceiver(
    private val onResult: (ExtractionResult) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("EvAnaliz", "ExtractionResultReceiver.onReceive() - action: ${intent?.action}")
        
        if (intent?.action != ScreenDataAccessibilityService.ACTION_EXTRACTION_RESULT) {
            return
        }

        val result = ExtractionResult(
            extractedTexts = intent.getStringArrayListExtra(
                ScreenDataAccessibilityService.EXTRA_EXTRACTED_TEXTS
            ) ?: emptyList(),
            timestamp = intent.getLongExtra(
                ScreenDataAccessibilityService.EXTRA_TIMESTAMP,
                System.currentTimeMillis()
            ),
            sourcePackage = intent.getStringExtra(
                ScreenDataAccessibilityService.EXTRA_SOURCE_PACKAGE
            ) ?: "unknown",
            isSuccessful = intent.getBooleanExtra(
                ScreenDataAccessibilityService.EXTRA_IS_SUCCESSFUL,
                false
            ),
            errorMessage = intent.getStringExtra(
                ScreenDataAccessibilityService.EXTRA_ERROR_MESSAGE
            )
        )

        onResult(result)
    }

    companion object {
        /**
         * Receiver'ı kaydet.
         */
        fun register(context: Context, receiver: ExtractionResultReceiver) {
            val filter = IntentFilter(ScreenDataAccessibilityService.ACTION_EXTRACTION_RESULT)
            LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter)
        }

        /**
         * Receiver'ı kaldır.
         */
        fun unregister(context: Context, receiver: ExtractionResultReceiver) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
        }
    }
}
