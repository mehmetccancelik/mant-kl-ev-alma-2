package com.evanaliz.floating

import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Dokunma Türü
 */
enum class TouchType {
    CLICK,
    DRAG
}

/**
 * Dokunma Sonucu
 */
data class TouchResult(
    val type: TouchType,
    val totalDistance: Float
)

/**
 * Dokunma Ayrıştırıcı
 * 
 * Sürükleme ve tıklama arasındaki farkı piksel mesafesi bazında belirler.
 * 
 * ╔════════════════════════════════════════════════════════════════════════════╗
 * ║ KURAL                                                                      ║
 * ╠════════════════════════════════════════════════════════════════════════════╣
 * ║ Hareket mesafesi > DRAG_THRESHOLD_PIXELS → SÜRÜKLEME                       ║
 * ║ Hareket mesafesi ≤ DRAG_THRESHOLD_PIXELS → TIKLA                           ║
 * ║                                                                            ║
 * ║ ⚠️ Bu mantık ZAMAN bazlı DEĞİL, MESafe bazlıdır.                           ║
 * ╚════════════════════════════════════════════════════════════════════════════╝
 */
class TouchDiscriminator {

    // Dokunma başlangıç koordinatları
    private var startX: Float = 0f
    private var startY: Float = 0f
    
    // Dokunma başlangıç zamanı
    private var startTime: Long = 0L
    
    // Dokunma aktif mi?
    private var isTracking: Boolean = false
    
    // Şu ana kadar hareket edilen toplam mesafe
    private var totalDistance: Float = 0f
    
    // En son bilinen koordinatlar
    private var lastX: Float = 0f
    private var lastY: Float = 0f

    /**
     * Dokunma başlangıcını kaydet.
     * 
     * @param x Başlangıç X koordinatı
     * @param y Başlangıç Y koordinatı
     */
    fun onTouchDown(x: Float, y: Float) {
        startX = x
        startY = y
        lastX = x
        lastY = y
        startTime = System.currentTimeMillis()
        isTracking = true
        totalDistance = 0f
    }

    /**
     * Dokunma hareketini kaydet.
     * 
     * @param x Mevcut X koordinatı
     * @param y Mevcut Y koordinatı
     * @return Şu ana kadar toplam hareket mesafesi
     */
    fun onTouchMove(x: Float, y: Float): Float {
        if (!isTracking) return 0f
        
        // Son noktadan bu noktaya mesafe
        val dx = x - lastX
        val dy = y - lastY
        val segmentDistance = sqrt(dx * dx + dy * dy)
        
        totalDistance += segmentDistance
        
        lastX = x
        lastY = y
        
        return totalDistance
    }

    /**
     * Dokunma bitişini değerlendir.
     * 
     * @param x Bitiş X koordinatı
     * @param y Bitiş Y koordinatı
     * @return Dokunma sonucu (CLICK veya DRAG)
     */
    fun onTouchUp(x: Float, y: Float): TouchResult {
        if (!isTracking) {
            return TouchResult(TouchType.CLICK, 0f)
        }
        
        // Son hareket mesafesini ekle
        val dx = x - lastX
        val dy = y - lastY
        totalDistance += sqrt(dx * dx + dy * dy)
        
        // Başlangıçtan bitiş noktasına doğrudan mesafe (opsiyonel kontrol)
        val directDistance = sqrt(
            (x - startX) * (x - startX) + 
            (y - startY) * (y - startY)
        )
        
        // Süre kontrolü
        val duration = System.currentTimeMillis() - startTime
        
        // Sonucu belirle
        val type = when {
            // Toplam hareket mesafesi eşiği aşıyorsa → DRAG
            totalDistance > FloatingUiConfig.DRAG_THRESHOLD_PIXELS -> TouchType.DRAG
            
            // Doğrudan mesafe eşiği aşıyorsa → DRAG
            directDistance > FloatingUiConfig.DRAG_THRESHOLD_PIXELS -> TouchType.DRAG
            
            // Süre çok uzunsa → DRAG (opsiyonel güvenlik)
            duration > FloatingUiConfig.MAX_CLICK_DURATION_MS -> TouchType.DRAG
            
            // Aksi halde → CLICK
            else -> TouchType.CLICK
        }
        
        isTracking = false
        
        return TouchResult(type, totalDistance)
    }

    /**
     * Dokunma izlemeyi iptal et.
     */
    fun cancel() {
        isTracking = false
        totalDistance = 0f
    }

    /**
     * Mevcut hareketin sürükleme olup olmadığını kontrol et.
     * Hareket devam ederken kullanılır.
     */
    fun isDragging(): Boolean {
        return isTracking && totalDistance > FloatingUiConfig.DRAG_THRESHOLD_PIXELS
    }
}
