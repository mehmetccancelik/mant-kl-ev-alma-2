package com.evanaliz.floating

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager

/**
 * Yüzen Overlay Servisi
 * 
 * WindowManager kullanarak ekranın üzerinde yüzen widget'ı yöneten Foreground Service.
 * 
 * ╔════════════════════════════════════════════════════════════════════════════╗
 * ║ SERVİS YAŞAM DÖNGÜSÜ                                                       ║
 * ╠════════════════════════════════════════════════════════════════════════════╣
 * ║ onCreate()      → Widget oluşturulur, WindowManager'a eklenir              ║
 * ║ onStartCommand()→ Servis başlatılır                                        ║
 * ║ onDestroy()     → Widget kaldırılır, kaynaklar temizlenir                  ║
 * ╚════════════════════════════════════════════════════════════════════════════╝
 */
abstract class FloatingOverlayService : Service() {

    // ═══════════════════════════════════════════════════════════════════════════
    // BAĞIMLILIKLAR
    // ═══════════════════════════════════════════════════════════════════════════
    
    protected lateinit var windowManager: WindowManager
    protected lateinit var floatingView: View
    protected lateinit var layoutParams: WindowManager.LayoutParams
    
    protected val stateMachine = FloatingUiStateMachine()
    protected val touchDiscriminator = TouchDiscriminator()
    
    private val mainHandler = Handler(Looper.getMainLooper())
    private var processingTimeoutRunnable: Runnable? = null

    // ═══════════════════════════════════════════════════════════════════════════
    // SERVİS YAŞAM DÖNGÜSÜ
    // ═══════════════════════════════════════════════════════════════════════════

    override fun onCreate() {
        super.onCreate()
        
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        // State machine callback
        stateMachine.onStateChanged = { oldState, newState ->
            onStateChanged(oldState, newState)
        }
        
        // Widget'ı oluştur
        createFloatingWidget()
        
        // WindowManager'a ekle
        addToWindowManager()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Foreground service notification burada başlatılmalı
        // (Prompt 7'de detaylandırılacak)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        
        // Timeout runnable'ı iptal et
        processingTimeoutRunnable?.let { mainHandler.removeCallbacks(it) }
        
        // Widget'ı kaldır
        try {
            windowManager.removeView(floatingView)
        } catch (e: Exception) {
            // View zaten kaldırılmış olabilir
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ═══════════════════════════════════════════════════════════════════════════
    // SOYUT METODLAR (Alt sınıflar implemente etmeli)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Floating widget view'ını oluştur.
     * Alt sınıf kendi UI'ını burada oluşturmalı.
     */
    protected abstract fun createFloatingWidget()

    /**
     * Durum değiştiğinde çağrılır.
     * Alt sınıf UI güncellemelerini burada yapmalı.
     */
    protected abstract fun onStateChanged(oldState: FloatingUiState, newState: FloatingUiState)

    /**
     * Sonuç gösterilecek.
     * Alt sınıf dialog/toast göstermeyi burada yapmalı.
     */
    protected abstract fun showResult()

    /**
     * Veri çekme işlemini başlat.
     * Alt sınıf Accessibility çağrısını burada yapmalı.
     */
    protected abstract fun startDataExtraction()

    // ═══════════════════════════════════════════════════════════════════════════
    // WINDOWMANAGER
    // ═══════════════════════════════════════════════════════════════════════════

    private fun addToWindowManager() {
        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 200
        }

        windowManager.addView(floatingView, layoutParams)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DOKUNMA YÖNETİMİ
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Widget'a dokunma olaylarını işle.
     * Bu metodu floatingView'ın onTouchListener'ına bağlayın.
     */
    protected fun handleTouch(event: MotionEvent): Boolean {
        val rawX = event.rawX
        val rawY = event.rawY

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchDiscriminator.onTouchDown(rawX, rawY)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                touchDiscriminator.onTouchMove(rawX, rawY)
                
                // Sürükleme modundaysa widget'ı hareket ettir
                if (touchDiscriminator.isDragging() && 
                    stateMachine.currentState == FloatingUiState.IDLE) {
                    
                    layoutParams.x = (rawX - floatingView.width / 2).toInt()
                    layoutParams.y = (rawY - floatingView.height / 2).toInt()
                    windowManager.updateViewLayout(floatingView, layoutParams)
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                val result = touchDiscriminator.onTouchUp(rawX, rawY)
                Log.d("EvAnaliz", "Touch UP - Type: ${result.type}, Distance: ${result.totalDistance}")
                
                if (result.type == TouchType.CLICK) {
                    Log.d("EvAnaliz", "CLICK detected, calling handleClick()")
                    handleClick()
                } else {
                    Log.d("EvAnaliz", "DRAG detected, not handling as click")
                }
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                touchDiscriminator.cancel()
                return true
            }
        }
        return false
    }

    /**
     * Tıklama olayını işle.
     */
    private fun handleClick() {
        Log.d("EvAnaliz", "handleClick() called, currentState: ${stateMachine.currentState}")
        
        when (stateMachine.currentState) {
            FloatingUiState.IDLE -> {
                Log.d("EvAnaliz", "State is IDLE, starting data extraction")
                // Veri çekme başlat
                stateMachine.processEvent(FloatingUiEvent.Click)
                startProcessingTimeout()
                startDataExtraction()
            }
            
            FloatingUiState.SUCCESS -> {
                Log.d("EvAnaliz", "State is SUCCESS, showing result")
                // Sonucu göster
                stateMachine.processEvent(FloatingUiEvent.Click)
                showResult()
            }
            
            else -> {
                Log.d("EvAnaliz", "State is ${stateMachine.currentState}, ignoring click")
                // Diğer durumlarda tıklama işlenmez
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TIMEOUT YÖNETİMİ
    // ═══════════════════════════════════════════════════════════════════════════

    private fun startProcessingTimeout() {
        // Önceki timeout'u iptal et
        processingTimeoutRunnable?.let { mainHandler.removeCallbacks(it) }
        
        // Yeni timeout başlat
        processingTimeoutRunnable = Runnable {
            if (stateMachine.currentState == FloatingUiState.PROCESSING) {
                stateMachine.processEvent(FloatingUiEvent.Error)
            }
        }
        
        mainHandler.postDelayed(
            processingTimeoutRunnable!!, 
            FloatingUiConfig.PROCESSING_TIMEOUT_MS
        )
    }

    /**
     * Veri başarıyla alındığında çağrılır.
     * Alt sınıf bu metodu Accessibility callback'inden çağırmalı.
     */
    protected fun onDataReceived() {
        // Timeout'u iptal et
        processingTimeoutRunnable?.let { mainHandler.removeCallbacks(it) }
        
        // State geçişi
        stateMachine.processEvent(FloatingUiEvent.DataReceived)
    }

    /**
     * Sonuç gösterimi kapatıldığında çağrılır.
     */
    protected fun onResultDismissed() {
        mainHandler.postDelayed({
            stateMachine.processEvent(FloatingUiEvent.ResultDismissed)
        }, FloatingUiConfig.RESULT_DISMISS_DELAY_MS)
    }
}
