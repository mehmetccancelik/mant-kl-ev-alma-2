package com.evanaliz.accessibility

/**
 * Çekilen Veri Sonucu
 * 
 * Accessibility Service tarafından ekrandan çekilen ham veriler.
 * 
 * ⚠️ Bu model sadece HAM METİN içerir.
 * ⚠️ Sayıya dönüştürme, iş mantığı kararları YOKTUR.
 */
data class ExtractionResult(

    /**
     * Çekilen metin listesi
     * 
     * Para birimi veya büyük sayı içeren tüm textler.
     * Temizlenmiş ama parse edilmemiş ham stringler.
     */
    val extractedTexts: List<String>,

    /**
     * Çekim zaman damgası
     */
    val timestamp: Long,

    /**
     * Kaynak uygulama paket adı
     */
    val sourcePackage: String,

    /**
     * Çekim başarılı mı?
     */
    val isSuccessful: Boolean,

    /**
     * Hata mesajı (varsa)
     */
    val errorMessage: String? = null
) {
    /**
     * Veri bulundu mu?
     */
    val hasData: Boolean
        get() = extractedTexts.isNotEmpty()

    /**
     * Bulunan metin sayısı
     */
    val count: Int
        get() = extractedTexts.size

    companion object {
        /**
         * Boş/başarısız sonuç oluştur
         */
        fun empty(sourcePackage: String, errorMessage: String? = null): ExtractionResult {
            return ExtractionResult(
                extractedTexts = emptyList(),
                timestamp = System.currentTimeMillis(),
                sourcePackage = sourcePackage,
                isSuccessful = errorMessage == null,
                errorMessage = errorMessage
            )
        }
    }
}
