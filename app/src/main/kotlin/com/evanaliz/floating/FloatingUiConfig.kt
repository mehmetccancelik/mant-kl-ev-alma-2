package com.evanaliz.floating

/**
 * Yüzen Arayüz Yapılandırması
 * 
 * Floating widget'ın davranış ve görünüm sabitleri.
 */
object FloatingUiConfig {

    // ═══════════════════════════════════════════════════════════════════════════
    // DOKUNMA ALGILAMA
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Sürükleme Eşik Mesafesi (piksel)
     * 
     * Dokunma hareketi bu mesafeyi aşarsa → SÜRÜKLEME
     * Dokunma hareketi bu mesafenin altındaysa → TIKLA
     * 
     * ⚠️ Bu değer piksel mesafesi bazlıdır, zaman bazlı DEĞİLDİR.
     */
    const val DRAG_THRESHOLD_PIXELS: Int = 10

    /**
     * Maksimum Tıklama Süresi (ms)
     * 
     * Opsiyonel ek güvenlik kontrolü.
     * Bu süreyi aşan dokunmalar tıklama olarak kabul edilmez.
     */
    const val MAX_CLICK_DURATION_MS: Long = 300

    // ═══════════════════════════════════════════════════════════════════════════
    // BOYUTLAR
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * FAB Boyutu (dp)
     */
    const val FAB_SIZE_DP: Int = 56

    /**
     * Spinner Genişliği (dp)
     * FAB etrafındaki progress indicator kalınlığı.
     */
    const val SPINNER_WIDTH_DP: Int = 4

    /**
     * Etiket metin boyutu (sp)
     */
    const val LABEL_TEXT_SIZE_SP: Float = 12f

    // ═══════════════════════════════════════════════════════════════════════════
    // RENKLER (ARGB)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * IDLE durumu rengi - Nötr gri
     */
    const val COLOR_IDLE: Int = 0xFF757575.toInt()

    /**
     * PROCESSING durumu rengi - Sarı/Turuncu
     */
    const val COLOR_PROCESSING: Int = 0xFFFFA000.toInt()

    /**
     * SUCCESS durumu rengi - Mavi
     */
    const val COLOR_SUCCESS: Int = 0xFF2196F3.toInt()

    /**
     * Spinner rengi
     */
    const val COLOR_SPINNER: Int = 0xFFFFFFFF.toInt()

    // ═══════════════════════════════════════════════════════════════════════════
    // METİNLER
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * SUCCESS durumunda görünen etiket metni
     */
    const val LABEL_SUCCESS: String = "GÖR"

    // ═══════════════════════════════════════════════════════════════════════════
    // ZAMAN AŞIMI
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * PROCESSING durumunda maksimum bekleme süresi (ms)
     * 
     * Bu süre aşılırsa otomatik olarak IDLE'a dönülür.
     * UI'ın takılı kalmasını önler.
     */
    const val PROCESSING_TIMEOUT_MS: Long = 10_000

    /**
     * RESULT_DISPLAY sonrası IDLE'a dönüş gecikmesi (ms)
     */
    const val RESULT_DISMISS_DELAY_MS: Long = 500
}
