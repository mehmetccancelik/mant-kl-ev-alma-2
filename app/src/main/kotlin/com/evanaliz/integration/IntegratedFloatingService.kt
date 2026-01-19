package com.evanaliz.integration

import android.util.Log
import com.evanaliz.accessibility.ExtractionResult
import com.evanaliz.accessibility.ExtractionResultReceiver
import com.evanaliz.accessibility.ScreenDataAccessibilityService
import com.evanaliz.floating.FloatingOverlayService
import com.evanaliz.floating.FloatingUiState

/**
 * Concrete Floating Overlay Service
 * 
 * Abstract FloatingOverlayService'in gerçek implementasyonu.
 * Accessibility ve Calculation Engine'i birleştirir.
 */
abstract class IntegratedFloatingService : FloatingOverlayService() {

    private var extractionReceiver: ExtractionResultReceiver? = null
    private var lastResult: IntegrationResult? = null

    override fun onCreate() {
        super.onCreate()
        
        // Extraction receiver'ı kaydet
        extractionReceiver = ExtractionResultReceiver { result ->
            handleExtractionResult(result)
        }
        ExtractionResultReceiver.register(this, extractionReceiver!!)
    }

    override fun onDestroy() {
        // Receiver'ı kaldır
        extractionReceiver?.let {
            ExtractionResultReceiver.unregister(this, it)
        }
        super.onDestroy()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ABSTRACT IMPLEMENTASYONLARI
    // ═══════════════════════════════════════════════════════════════════════════

    override fun startDataExtraction() {
        Log.d("EvAnaliz", "startDataExtraction() called")
        
        // Accessibility servisini kontrol et
        val isRunning = ScreenDataAccessibilityService.isServiceRunning()
        Log.d("EvAnaliz", "AccessibilityService isRunning: $isRunning")
        
        if (!isRunning) {
            Log.e("EvAnaliz", "Accessibility service NOT running, showing error")
            handleError(ErrorHandling.accessibilityServiceNotRunning())
            return
        }
        
        Log.d("EvAnaliz", "Triggering extraction...")
        // Extraction'ı tetikle
        ScreenDataAccessibilityService.triggerExtraction()
    }

    override fun showResult() {
        Log.d("EvAnaliz", "showResult() called, lastResult = $lastResult")
        
        if (lastResult == null) {
            Log.e("EvAnaliz", "showResult() - lastResult is NULL!")
            showErrorToast("Sonuç bulunamadı, tekrar deneyin")
            onResultDismissed()
            return
        }
        
        lastResult?.let { result ->
            Log.d("EvAnaliz", "showResult() - displaying result: ${result::class.simpleName}")
            when (result) {
                is IntegrationResult.Success -> {
                    showSuccessDialog(result)
                }
                is IntegrationResult.PartialSuccess -> {
                    showPartialDialog(result)
                }
                is IntegrationResult.Error -> {
                    showErrorDialog(result)
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // EXTRACTION CALLBACK
    // ═══════════════════════════════════════════════════════════════════════════

    private fun handleExtractionResult(extractionResult: ExtractionResult) {
        Log.d("EvAnaliz", "handleExtractionResult() received: $extractionResult")
        
        // Entegrasyon pipeline'ını çalıştır
        val integrationResult = DomainIntegrationBridge.process(extractionResult)
        lastResult = integrationResult
        
        Log.d("EvAnaliz", "Integration result: ${integrationResult::class.simpleName}")
        
        when (integrationResult) {
            is IntegrationResult.Success -> {
                Log.d("EvAnaliz", "SUCCESS - calling onDataReceived()")
                // State machine'e başarı bildir
                onDataReceived()
            }
            is IntegrationResult.PartialSuccess -> {
                Log.d("EvAnaliz", "PARTIAL SUCCESS - calling onDataReceived()")
                // Kısmi başarı da SUCCESS state'ine geçsin
                onDataReceived()
            }
            is IntegrationResult.Error -> {
                Log.e("EvAnaliz", "ERROR - ${integrationResult.error}")
                // Hata durumunda IDLE'a dön
                val appError = ErrorHandling.toAppError(integrationResult.error)
                handleError(appError)
            }
        }
    }

    private fun handleError(error: ErrorHandling.AppError) {
        Log.e("EvAnaliz", "handleError() - ${error.userMessage}")
        // State machine'i sıfırla
        stateMachine.forceReset()
        
        // Hata UI'ını göster (subclass implemente etmeli)
        showErrorToast(error.userMessage)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SUBCLASS TARAFINDAN İMPLEMENTE EDİLECEK
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Başarılı sonuç dialogunu göster
     */
    protected abstract fun showSuccessDialog(result: IntegrationResult.Success)

    /**
     * Kısmi başarı dialogunu göster
     */
    protected abstract fun showPartialDialog(result: IntegrationResult.PartialSuccess)

    /**
     * Hata dialogunu göster
     */
    protected abstract fun showErrorDialog(result: IntegrationResult.Error)

    /**
     * Hata toast'ını göster
     */
    protected abstract fun showErrorToast(message: String)
}
