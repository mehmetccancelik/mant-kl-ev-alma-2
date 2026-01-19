package com.evanaliz.floating

/**
 * Yüzen Arayüz Durumları
 * 
 * Floating widget'ın olabileceği tüm durumlar.
 * Kesin olarak 4 durum tanımlanmıştır - daha fazlası YASAKTIR.
 * 
 * ╔════════════════════════════════════════════════════════════════════════════╗
 * ║ DURUM GEÇİŞ DİYAGRAMI                                                      ║
 * ╠════════════════════════════════════════════════════════════════════════════╣
 * ║                                                                            ║
 * ║    ┌──────────┐    click    ┌────────────┐   data    ┌─────────┐          ║
 * ║    │   IDLE   │ ──────────▶ │ PROCESSING │ ────────▶ │ SUCCESS │          ║
 * ║    └──────────┘             └────────────┘           └─────────┘          ║
 * ║         ▲                                                  │              ║
 * ║         │                                              click              ║
 * ║         │         ┌────────────────┐                      │              ║
 * ║         └──────── │ RESULT_DISPLAY │ ◀────────────────────┘              ║
 * ║          dismiss  └────────────────┘                                      ║
 * ║                                                                            ║
 * ╚════════════════════════════════════════════════════════════════════════════╝
 */
enum class FloatingUiState {

    /**
     * 🟢 IDLE - Bekleme Durumu
     * 
     * Varsayılan durum.
     * - Küçük dairesel FAB
     * - Sürüklenebilir
     * - Spinner YOK
     * - Metin etiketi YOK
     * - Nötr renk
     */
    IDLE,

    /**
     * 🟡 PROCESSING - İşleniyor
     * 
     * CLICK ile tetiklenir (sürükleme ile DEĞİL).
     * - FAB yerinde kilitli
     * - FAB etrafında dairesel spinner görünür
     * - Kullanıcı girdisi geçici olarak devre dışı
     * - Arka plan sonucu bekleniyor
     */
    PROCESSING,

    /**
     * 🔵 SUCCESS - Başarılı
     * 
     * Geçerli veri alındığında tetiklenir.
     * - Spinner gizlenir
     * - FAB rengi MAVİ olur
     * - FAB altında "GÖR" etiketi görünür
     * - Widget tekrar tıklanabilir hale gelir
     */
    SUCCESS,

    /**
     * 🧾 RESULT_DISPLAY - Sonuç Gösterimi
     * 
     * Mavi FAB veya "GÖR" tıklandığında tetiklenir.
     * - Overlay dialog veya Toast gösterilir
     * - Floating widget geçici olarak pasif
     * - Kapatıldıktan sonra → IDLE'a döner
     */
    RESULT_DISPLAY
}
