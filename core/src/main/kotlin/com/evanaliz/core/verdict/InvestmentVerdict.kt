package com.evanaliz.core.verdict

/**
 * Yatırım Kararı Sonuç Modeli
 * 
 * Finansal hesaplama sonucunun yorumlanmış hali.
 * Bu immutable data class, UI katmanına aktarılacak tüm karar bilgilerini içerir.
 * 
 * ⚠️ Bu sınıf finansal hesaplama YAPMAZ, sadece yorumlar.
 */
data class InvestmentVerdict(

    // ═══════════════════════════════════════════════════════════════════════════
    // ANA METRİK (Hesaplama Motorundan Alınır)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Amortisman Süresi (Yıl)
     * 
     * Hesaplama motorundan (Prompt 1) alınan değer.
     * Karar bu değere göre verilir.
     * Yeniden hesaplanmaz, sadece referans olarak tutulur.
     */
    val amortizationYears: Double,

    // ═══════════════════════════════════════════════════════════════════════════
    // KARAR BİLGİLERİ
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Durum Metni
     * 
     * Kullanıcıya gösterilecek kısa karar cümlesi.
     * Örnek: "MANTIKLI YATIRIM ✅" veya "MANTIKSIZ / PAHALI ❌"
     */
    val statusText: String,

    /**
     * Yatırım Kategorisi
     * 
     * Programatik karar kategorisi.
     * UI ve iş mantığı tarafından kullanılır.
     */
    val category: InvestmentCategory,

    /**
     * Renk İpucu
     * 
     * UI katmanına semantik renk önerisi.
     * Gerçek renk kodu içermez.
     */
    val colorHint: ColorHint,

    /**
     * Özet Açıklama
     * 
     * Kullanıcıya gösterilecek detaylı açıklama metni.
     * Kararın nedenini insan dilinde açıklar.
     * Formül veya teknik detay içermez.
     */
    val summaryExplanation: String
)
