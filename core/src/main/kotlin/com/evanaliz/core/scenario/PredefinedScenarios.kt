package com.evanaliz.core.scenario

/**
 * Önceden Tanımlanmış Senaryolar
 * 
 * Türk gayrimenkul piyasası için 4 farklı ekonomik senaryo.
 */
object PredefinedScenarios {

    // ═══════════════════════════════════════════════════════════════════════════
    // SENARYO 1: İYİMSER
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * İyimser Senaryo
     * 
     * Ekonomi güçlü, faizler düşük, gayrimenkul değerleniyor.
     */
    val OPTIMISTIC = InvestmentScenario(
        name = "İyimser Senaryo",
        description = "Ekonomi güçlü büyüyor, faizler düşük, gayrimenkul talebi yüksek",
        type = ScenarioType.OPTIMISTIC,
        monthlyInterestRate = 0.0199,          // %1.99 aylık (düşük)
        annualInflationRate = 0.15,            // %15 yıllık
        annualPropertyAppreciation = 0.25,     // %25 değer artışı
        annualRentGrowth = 0.20,               // %20 kira artışı
        vacancyRate = 0.02,                    // %2 boşluk
        maintenanceCostRate = 0.03             // %3 bakım
    )

    // ═══════════════════════════════════════════════════════════════════════════
    // SENARYO 2: GERÇEKÇİ (BAZ)
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Gerçekçi (Baz) Senaryo
     * 
     * Mevcut Türkiye koşullarına yakın, orta yol.
     */
    val REALISTIC = InvestmentScenario(
        name = "Gerçekçi Senaryo",
        description = "Mevcut ekonomik koşullar devam ediyor",
        type = ScenarioType.REALISTIC,
        monthlyInterestRate = 0.0249,          // %2.49 aylık (mevcut)
        annualInflationRate = 0.45,            // %45 yıllık
        annualPropertyAppreciation = 0.35,     // %35 değer artışı
        annualRentGrowth = 0.30,               // %30 kira artışı
        vacancyRate = 0.05,                    // %5 boşluk
        maintenanceCostRate = 0.05             // %5 bakım
    )

    // ═══════════════════════════════════════════════════════════════════════════
    // SENARYO 3: KÖTÜMSER
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Kötümser Senaryo
     * 
     * Ekonomik durgunluk, faizler yüksek, talep düşük.
     */
    val PESSIMISTIC = InvestmentScenario(
        name = "Kötümser Senaryo",
        description = "Ekonomik durgunluk, yüksek faiz, düşük talep",
        type = ScenarioType.PESSIMISTIC,
        monthlyInterestRate = 0.0349,          // %3.49 aylık (yüksek)
        annualInflationRate = 0.60,            // %60 yıllık
        annualPropertyAppreciation = 0.15,     // %15 değer artışı (enflasyonun altında)
        annualRentGrowth = 0.15,               // %15 kira artışı
        vacancyRate = 0.10,                    // %10 boşluk
        maintenanceCostRate = 0.08             // %8 bakım
    )

    // ═══════════════════════════════════════════════════════════════════════════
    // SENARYO 4: AŞIRI STRES
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Aşırı Stres Senaryosu
     * 
     * Ekonomik kriz, çok yüksek faiz, gayrimenkul değer kaybı.
     */
    val EXTREME_STRESS = InvestmentScenario(
        name = "Aşırı Stres Senaryosu",
        description = "Ekonomik kriz, çok yüksek faiz, gayrimenkul piyasası çöküyor",
        type = ScenarioType.EXTREME_STRESS,
        monthlyInterestRate = 0.0499,          // %4.99 aylık (çok yüksek)
        annualInflationRate = 0.80,            // %80 yıllık
        annualPropertyAppreciation = 0.0,      // %0 değer artışı (stabil)
        annualRentGrowth = 0.10,               // %10 kira artışı
        vacancyRate = 0.20,                    // %20 boşluk
        maintenanceCostRate = 0.12             // %12 bakım
    )

    /**
     * Tüm senaryolar
     */
    val ALL = listOf(OPTIMISTIC, REALISTIC, PESSIMISTIC, EXTREME_STRESS)
}
