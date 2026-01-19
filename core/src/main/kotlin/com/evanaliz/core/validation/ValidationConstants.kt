package com.evanaliz.core.validation

/**
 * Doğrulama Sabitleri
 * 
 * Excel ile Kotlin karşılaştırması için tolerans ve eşik değerleri.
 */
object ValidationConstants {

    /**
     * Maksimum İzin Verilen Fark
     * 
     * Excel ve Kotlin arasındaki sayısal fark bu değerin altındaysa PASS kabul edilir.
     * Bu değer, yuvarlama kaynaklı minimal farklılıkları tolere eder.
     * 
     * Birim: TL veya yıl (bağlama göre)
     */
    const val MAX_ALLOWED_DIFFERENCE: Double = 0.01

    /**
     * Yüzde Bazlı Maksimum Fark
     * 
     * Büyük sayılar için mutlak fark yerine yüzde bazlı tolerans.
     * Örn: 10 milyon TL için 0.01 TL fark önemsiz ama
     * yüzde bazlı kontrol daha anlamlı olabilir.
     */
    const val MAX_ALLOWED_PERCENTAGE_DIFFERENCE: Double = 0.0001 // %0.01
}
