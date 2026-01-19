package com.evanaliz.core.verdict

/**
 * Karar Eşik Değerleri
 * 
 * Yatırım kararını belirleyen sabit eşik değerler.
 * Bu değerler iş mantığı tarafından belirlenmiştir ve kod içinde merkezi olarak tutulur.
 * 
 * ⚠️ Bu değerler finansal hesaplama değil, karar mantığı parametreleridir.
 */
object VerdictThresholds {

    /**
     * Maksimum Kabul Edilebilir Amortisman Süresi (Yıl)
     * 
     * Eğer yatırım bu süreden KISA sürede amorti oluyorsa → MANTIKLI
     * Eğer yatırım bu süreden UZUN sürede amorti oluyorsa → MANTIKSIZ
     * 
     * İş Mantığı: 14 yıl, Türkiye gayrimenkul piyasası için kabul edilebilir üst sınır.
     */
    const val MAX_ACCEPTABLE_AMORTIZATION_YEARS: Double = 14.0
}
