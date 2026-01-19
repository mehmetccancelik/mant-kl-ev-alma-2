package com.evanaliz.integration

/**
 * Parse Edilmiş Ekran Verisi
 * 
 * Accessibility'den gelen ham metinlerin parse edilmiş hali.
 * Ev fiyatı ve kira ayrı ayrı tanımlanır.
 */
data class ParsedScreenData(
    
    /**
     * Tespit edilen ev fiyatı (TL)
     * Null ise fiyat bulunamadı.
     */
    val housePrice: Double?,
    
    /**
     * Tespit edilen aylık kira (TL)
     * Null ise kira bulunamadı.
     */
    val estimatedMonthlyRent: Double?,
    
    /**
     * Tüm bulunan sayısal değerler (sıralı)
     */
    val allDetectedValues: List<Double>,
    
    /**
     * Kaynak uygulama
     */
    /**
     * Kaynak uygulama
     */
    val sourcePackage: String,

    /**
     * Enlem (Latitude) - varsa
     */
    val latitude: Double? = null,

    /**
     * Boylam (Longitude) - varsa
     */
    val longitude: Double? = null
) {
    /**
     * Parse başarılı mı?
     * En azından fiyat+kira çifti VEYA koordinat çifti bulunmuş olmalı.
     */
    val isComplete: Boolean
        get() = (housePrice != null && estimatedMonthlyRent != null) || 
                (latitude != null && longitude != null)
        
    companion object {
        fun empty(sourcePackage: String = "unknown") = ParsedScreenData(
            housePrice = null,
            estimatedMonthlyRent = null,
            allDetectedValues = emptyList(),
            sourcePackage = sourcePackage,
            latitude = null,
            longitude = null
        )
    }
}

/**
 * Parse Hatası
 */
sealed class ParseError {
    object NoDataFound : ParseError()
    object InsufficientData : ParseError()
    object InvalidFormat : ParseError()
    data class UnexpectedError(val message: String) : ParseError()
}

/**
 * Parse Sonucu
 */
sealed class ParseResult {
    data class Success(val data: ParsedScreenData) : ParseResult()
    data class Failure(val error: ParseError, val rawTexts: List<String>) : ParseResult()
}
