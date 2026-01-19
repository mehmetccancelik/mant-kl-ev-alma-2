package com.evanaliz.core.validation

/**
 * Alan Doğrulama Sonucu
 * 
 * Tek bir alanın (field) doğrulama sonucu.
 */
data class FieldValidationResult(
    /**
     * Alan adı
     */
    val fieldName: String,
    
    /**
     * Excel'den beklenen değer
     */
    val excelValue: Double,
    
    /**
     * Kotlin motorundan hesaplanan değer
     */
    val kotlinValue: Double,
    
    /**
     * Mutlak fark
     */
    val absoluteDifference: Double,
    
    /**
     * PASS veya FAIL
     */
    val passed: Boolean
) {
    /**
     * İnsan okunabilir durum sembolü
     */
    val statusSymbol: String
        get() = if (passed) "✅ PASS" else "❌ FAIL"
}

/**
 * Senaryo Doğrulama Sonucu
 * 
 * Bir test senaryosunun tüm alanlarının doğrulama sonucu.
 */
data class ScenarioValidationResult(
    /**
     * Senaryo adı
     */
    val scenarioName: String,
    
    /**
     * Senaryo açıklaması
     */
    val description: String,
    
    /**
     * Tüm alan sonuçları
     */
    val fieldResults: List<FieldValidationResult>,
    
    /**
     * Genel sonuç: Tüm alanlar PASS ise true
     */
    val overallPassed: Boolean
) {
    /**
     * Başarısız alan sayısı
     */
    val failedCount: Int
        get() = fieldResults.count { !it.passed }
    
    /**
     * Başarılı alan sayısı
     */
    val passedCount: Int
        get() = fieldResults.count { it.passed }
}

/**
 * Tam Doğrulama Raporu
 * 
 * Tüm test senaryolarının toplu sonucu.
 */
data class FullValidationReport(
    /**
     * Tüm senaryo sonuçları
     */
    val scenarioResults: List<ScenarioValidationResult>,
    
    /**
     * Genel sonuç: Tüm senaryolar PASS ise true
     */
    val overallPassed: Boolean,
    
    /**
     * Doğrulama tarihi/zamanı
     */
    val validationTimestamp: String
) {
    /**
     * Başarılı senaryo sayısı
     */
    val passedScenarioCount: Int
        get() = scenarioResults.count { it.overallPassed }
    
    /**
     * Toplam senaryo sayısı
     */
    val totalScenarioCount: Int
        get() = scenarioResults.size
}
