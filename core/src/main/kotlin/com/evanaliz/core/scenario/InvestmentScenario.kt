package com.evanaliz.core.scenario

/**
 * Yatırım Senaryosu
 * 
 * Farklı ekonomik koşullar altında yatırım analizini modelleyen senaryo tanımı.
 */
data class InvestmentScenario(
    
    /**
     * Senaryo adı
     */
    val name: String,
    
    /**
     * Senaryo açıklaması
     */
    val description: String,
    
    /**
     * Senaryo türü
     */
    val type: ScenarioType,
    
    // ═══════════════════════════════════════════════════════════════════════════
    // FAİZ PARAMETRELERİ
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Aylık faiz oranı
     */
    val monthlyInterestRate: Double,
    
    // ═══════════════════════════════════════════════════════════════════════════
    // ENFLASYON & DEĞER ARTIŞ
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Yıllık enflasyon oranı
     */
    val annualInflationRate: Double,
    
    /**
     * Yıllık gayrimenkul değer artışı
     */
    val annualPropertyAppreciation: Double,
    
    /**
     * Yıllık kira artış oranı
     */
    val annualRentGrowth: Double,
    
    // ═══════════════════════════════════════════════════════════════════════════
    // RİSK FAKTÖRLERİ
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Boşluk oranı (yılda kiracısız kalma yüzdesi)
     */
    val vacancyRate: Double,
    
    /**
     * Bakım ve beklenmeyen gider oranı (yıllık kira üzerinden)
     */
    val maintenanceCostRate: Double
)

/**
 * Senaryo Türleri
 */
enum class ScenarioType {
    /**
     * İyimser senaryo - Her şey yolunda gider
     */
    OPTIMISTIC,
    
    /**
     * Gerçekçi/baz senaryo - Normal koşullar
     */
    REALISTIC,
    
    /**
     * Kötümser senaryo - Olumsuz koşullar
     */
    PESSIMISTIC,
    
    /**
     * Aşırı stres senaryosu - En kötü durum
     */
    EXTREME_STRESS
}
