package com.evanaliz.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.localbroadcastmanager.content.LocalBroadcastManager

/**
 * Ekran Veri Çekme Accessibility Servisi
 * 
 * Ekrandaki metinleri okuyan read-only Accessibility Service.
 * 
 * ╔════════════════════════════════════════════════════════════════════════════╗
 * ║ TASARIM PRENSİPLERİ                                                        ║
 * ╠════════════════════════════════════════════════════════════════════════════╣
 * ║ 1. READ-ONLY - Sadece okur, müdahale etmez                                 ║
 * ║ 2. On-demand - Sürekli tarama yok, istek üzerine çalışır                   ║
 * ║ 3. Hafif - Main thread'i bloklamaz                                         ║
 * ║ 4. Güvenli - Crash olmaz, hata durumunda boş sonuç döner                   ║
 * ╚════════════════════════════════════════════════════════════════════════════╝
 */
class ScreenDataAccessibilityService : AccessibilityService() {

    companion object {
        /**
         * Extraction sonucu için broadcast action
         */
        const val ACTION_EXTRACTION_RESULT = "com.evanaliz.EXTRACTION_RESULT"

        /**
         * Extraction başlat için broadcast action
         */
        const val ACTION_START_EXTRACTION = "com.evanaliz.START_EXTRACTION"

        /**
         * Sonuç extra key'leri
         */
        const val EXTRA_EXTRACTED_TEXTS = "extracted_texts"
        const val EXTRA_SOURCE_PACKAGE = "source_package"
        const val EXTRA_TIMESTAMP = "timestamp"
        const val EXTRA_IS_SUCCESSFUL = "is_successful"
        const val EXTRA_ERROR_MESSAGE = "error_message"

        /**
         * Servis instance referansı (Singleton pattern)
         */
        private var instance: ScreenDataAccessibilityService? = null

        /**
         * Servis açık mı?
         */
        fun isServiceRunning(): Boolean = instance != null

        /**
         * Extraction tetikle (dışarıdan çağrılır)
         */
        fun triggerExtraction() {
            instance?.performExtraction()
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SERVİS YAŞAM DÖNGÜSÜ
    // ═══════════════════════════════════════════════════════════════════════════

    override fun onServiceConnected() {
        super.onServiceConnected()
        
        instance = this

        // Servis yapılandırması
        serviceInfo = serviceInfo?.apply {
            // Pencere içeriğini okuyabilir
            flags = flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            
            // Dinlenecek event türleri
            eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                        AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED

            // Tüm paketleri dinle (null = hepsi)
            packageNames = null

            // Feedback türü
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // EVENT İŞLEME
    // ═══════════════════════════════════════════════════════════════════════════

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Otomatik tarama yapmıyoruz
        // Sadece on-demand (triggerExtraction) ile çalışır
    }

    override fun onInterrupt() {
        // Servis kesintiye uğradı
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // EKSTRAKSİYON
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Ekran taraması yap ve sonucu broadcast et.
     */
    private fun performExtraction() {
        Log.d("EvAnaliz", "performExtraction() START")
        val result = try {
            // Kök düğümü al
            val rootNode = rootInActiveWindow
            Log.d("EvAnaliz", "performExtraction() - rootNode: $rootNode")

            if (rootNode == null) {
                ExtractionResult.empty(
                    sourcePackage = "unknown",
                    errorMessage = "Root node null - pencere içeriği alınamadı"
                )
            } else {
                // Kaynak paket adını al
                val sourcePackage = rootNode.packageName?.toString() ?: "unknown"

                // Ekranı tara
                val extractedTexts = ScreenTraverser.traverse(rootNode)

                // Kök düğümü geri dönüştür
                try {
                    rootNode.recycle()
                } catch (e: Exception) {
                    // Ignore
                }

                // Sonuç oluştur
                ExtractionResult(
                    extractedTexts = extractedTexts,
                    timestamp = System.currentTimeMillis(),
                    sourcePackage = sourcePackage,
                    isSuccessful = true
                )
            }
        } catch (e: Exception) {
            ExtractionResult.empty(
                sourcePackage = "unknown",
                errorMessage = "Extraction hatası: ${e.message}"
            )
        }

        // Sonucu broadcast et
        Log.d("EvAnaliz", "performExtraction() - broadcasting result: ${result.isSuccessful}, texts: ${result.extractedTexts.size}")
        broadcastResult(result)
    }

    /**
     * Sonucu LocalBroadcast ile yayınla.
     */
    private fun broadcastResult(result: ExtractionResult) {
        Log.d("EvAnaliz", "broadcastResult() - sending broadcast")
        val intent = Intent(ACTION_EXTRACTION_RESULT).apply {
            putStringArrayListExtra(
                EXTRA_EXTRACTED_TEXTS, 
                ArrayList(result.extractedTexts)
            )
            putExtra(EXTRA_SOURCE_PACKAGE, result.sourcePackage)
            putExtra(EXTRA_TIMESTAMP, result.timestamp)
            putExtra(EXTRA_IS_SUCCESSFUL, result.isSuccessful)
            putExtra(EXTRA_ERROR_MESSAGE, result.errorMessage)
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        Log.d("EvAnaliz", "broadcastResult() - broadcast sent")
    }
}
