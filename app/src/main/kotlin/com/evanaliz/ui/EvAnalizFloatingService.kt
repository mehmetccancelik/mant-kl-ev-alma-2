package com.evanaliz.ui

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.evanaliz.MainActivity
import com.evanaliz.R
import com.evanaliz.floating.FloatingUiConfig
import com.evanaliz.floating.FloatingUiState
import com.evanaliz.integration.IntegratedFloatingService
import com.evanaliz.integration.IntegrationResult

/**
 * Ev Analiz Floating Service
 * 
 * Gerçek UI implementasyonu ile floating overlay service.
 */
class EvAnalizFloatingService : IntegratedFloatingService() {

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "ev_analiz_floating"
    }

    // UI bileşenleri
    private lateinit var fabContainer: FrameLayout
    private lateinit var fabIcon: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var labelText: TextView

    override fun onCreate() {
        Log.d("EvAnaliz", "EvAnalizFloatingService onCreate() called")
        super.onCreate()
        startForeground(NOTIFICATION_ID, createNotification())
        Log.d("EvAnaliz", "EvAnalizFloatingService onCreate() completed, started foreground")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FLOATING WIDGET OLUŞTURMA
    // ═══════════════════════════════════════════════════════════════════════════

    override fun createFloatingWidget() {
        Log.d("EvAnaliz", "createFloatingWidget() called")
        
        // Layout inflate
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatingView = inflater.inflate(R.layout.floating_widget, null)
        Log.d("EvAnaliz", "floatingView inflated: $floatingView")

        // UI referansları
        fabContainer = floatingView.findViewById(R.id.fab_container)
        fabIcon = floatingView.findViewById(R.id.fab_icon)
        progressBar = floatingView.findViewById(R.id.progress_bar)
        labelText = floatingView.findViewById(R.id.label_text)

        // Touch listener
        floatingView.setOnTouchListener { view, event ->
            Log.d("EvAnaliz", "Touch event received: action=${event.action}, x=${event.rawX}, y=${event.rawY}")
            handleTouch(event)
        }
        Log.d("EvAnaliz", "Touch listener set on floatingView")

        // Başlangıç durumu
        updateUI(FloatingUiState.IDLE)
        Log.d("EvAnaliz", "createFloatingWidget() completed")
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DURUM DEĞİŞİKLİĞİ
    // ═══════════════════════════════════════════════════════════════════════════

    override fun onStateChanged(oldState: FloatingUiState, newState: FloatingUiState) {
        updateUI(newState)
    }

    private fun updateUI(state: FloatingUiState) {
        when (state) {
            FloatingUiState.IDLE -> {
                fabContainer.setBackgroundColor(FloatingUiConfig.COLOR_IDLE)
                progressBar.visibility = View.GONE
                labelText.visibility = View.GONE
            }

            FloatingUiState.PROCESSING -> {
                fabContainer.setBackgroundColor(FloatingUiConfig.COLOR_PROCESSING)
                progressBar.visibility = View.VISIBLE
                labelText.visibility = View.GONE
            }

            FloatingUiState.SUCCESS -> {
                fabContainer.setBackgroundColor(FloatingUiConfig.COLOR_SUCCESS)
                progressBar.visibility = View.GONE
                labelText.visibility = View.VISIBLE
                labelText.text = FloatingUiConfig.LABEL_SUCCESS
            }

            FloatingUiState.RESULT_DISPLAY -> {
                // Dialog gösterilirken UI değişmez
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DİALOG GÖSTERİMİ
    // ═══════════════════════════════════════════════════════════════════════════

    override fun showSuccessDialog(result: IntegrationResult.Success) {
        Log.d("EvAnaliz", "showSuccessDialog() called - launching TabbedAnalysisActivity")
        
        try {
            val intent = Intent(this, TabbedAnalysisActivity::class.java).apply {
                putExtra(TabbedAnalysisActivity.EXTRA_HOUSE_PRICE, result.calculationResult.housePrice)
                putExtra(TabbedAnalysisActivity.EXTRA_MONTHLY_RENT, result.calculationResult.estimatedMonthlyRent)
                // Konum bilgisi - şimdilik sourcePackage kullanılıyor, ileride extraction güncellenecek
                putExtra(TabbedAnalysisActivity.EXTRA_LOCATION, result.parsedData.sourcePackage)
                // TODO: Koordinatları çekmek için extraction güncellenecek
                putExtra(TabbedAnalysisActivity.EXTRA_LATITUDE, 0.0)
                putExtra(TabbedAnalysisActivity.EXTRA_LONGITUDE, 0.0)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            onResultDismissed() // Reset state
        } catch (e: Exception) {
            Log.e("EvAnaliz", "Failed to launch TabbedAnalysisActivity: ${e.message}", e)
            // Fallback to AlertDialog
            val message = buildSuccessMessage(result)
            showAlertDialog("Analiz Sonucu", message)
        }
    }

    override fun showPartialDialog(result: IntegrationResult.PartialSuccess) {
        Log.d("EvAnaliz", "showPartialDialog() called")
        showAlertDialog("Kısmi Sonuç", result.message)
    }

    override fun showErrorDialog(result: IntegrationResult.Error) {
        Log.d("EvAnaliz", "showErrorDialog() called: ${result.userMessage}")
        showAlertDialog("Hata", result.userMessage)
    }

    override fun showErrorToast(message: String) {
        Log.d("EvAnaliz", "showErrorToast() called: $message")
        // Toast yerine AlertDialog kullan (toast'lar engellenmiş olabilir)
        showAlertDialog("Hata", message)
    }

    private fun showAlertDialog(title: String, message: String) {
        Log.d("EvAnaliz", "showAlertDialog() - title: $title, message: $message")
        
        // AlertDialog'u main thread'de göster
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            try {
                val dialogView = android.view.LayoutInflater.from(this)
                    .inflate(android.R.layout.simple_list_item_1, null)
                
                // Custom dialog view oluştur
                val textView = android.widget.TextView(this).apply {
                    text = message
                    setPadding(48, 32, 48, 32)
                    textSize = 16f
                    setTextColor(android.graphics.Color.BLACK)
                }
                
                val scrollView = android.widget.ScrollView(this).apply {
                    addView(textView)
                }
                
                val dialog = android.app.AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
                    .setTitle(title)
                    .setView(scrollView)
                    .setPositiveButton("Tamam") { dialog, _ ->
                        dialog.dismiss()
                        onResultDismissed()
                    }
                    .setOnDismissListener {
                        onResultDismissed()
                    }
                    .create()
                
                // Dialog window tipini ayarla (overlay için gerekli)
                dialog.window?.setType(android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
                
                dialog.show()
                Log.d("EvAnaliz", "AlertDialog shown successfully")
            } catch (e: Exception) {
                Log.e("EvAnaliz", "Failed to show AlertDialog: ${e.message}", e)
                onResultDismissed()
            }
        }
    }

    private fun buildSuccessMessage(result: IntegrationResult.Success): String {
        val verdict = result.verdict
        val calc = result.calculationResult
        val amortYears = "%.1f".format(calc.amortizationYears)
        
        return """
📊 ${verdict.statusText}

━━━ SATIN ALMA ━━━
• Ev Fiyatı: ${formatCurrency(calc.housePrice)}
• Alım Masrafı (7%): ${formatCurrency(calc.purchaseExpenses)}

━━━ KREDİ ━━━
• Kredi Tutarı (50%): ${formatCurrency(calc.loanAmount)}
• Peşinat: ${formatCurrency(calc.downPayment)}
• Aylık Taksit: ${formatCurrency(calc.monthlyInstallment)}
• Toplam Geri Ödeme: ${formatCurrency(calc.totalLoanRepayment)}

━━━ KİRA ━━━
• Aylık Kira: ${formatCurrency(calc.estimatedMonthlyRent)}
• Net Yıllık Kira: ${formatCurrency(calc.netAnnualRent)}

━━━ SONUÇ ━━━
• Toplam Maliyet: ${formatCurrency(calc.realTotalCost)}
• Amortisman: $amortYears yıl
        """.trimIndent()
    }

    private fun formatCurrency(value: Double): String {
        return "%,.0f TL".format(value)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // BİLDİRİM
    // ═══════════════════════════════════════════════════════════════════════════

    private fun createNotification(): Notification {
        createNotificationChannel()

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Ev Analiz")
            .setContentText("Ekran analizi aktif")
            .setSmallIcon(R.drawable.ic_home)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Floating Widget",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ev analiz floating widget servisi"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
