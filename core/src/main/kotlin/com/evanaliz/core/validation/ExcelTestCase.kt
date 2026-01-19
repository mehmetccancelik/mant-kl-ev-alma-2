package com.evanaliz.core.validation

/**
 * Excel Test Senaryosu
 * 
 * Excel dosyasından alınan referans değerler ile Kotlin motorunun
 * karşılaştırılması için kullanılan test verisi modeli.
 * 
 * Her senaryo için:
 * - Girdiler (housePrice, estimatedMonthlyRent)
 * - Excel'den beklenen çıktılar
 */
data class ExcelTestCase(
    
    // ═══════════════════════════════════════════════════════════════════════════
    // SENARYO KİMLİĞİ
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Senaryo adı (tanımlayıcı)
     */
    val scenarioName: String,
    
    /**
     * Senaryo açıklaması (test amacı)
     */
    val description: String,
    
    // ═══════════════════════════════════════════════════════════════════════════
    // GİRDİLER
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Ev fiyatı (Excel girdisi)
     */
    val inputHousePrice: Double,
    
    /**
     * Tahmini aylık kira (Excel girdisi)
     */
    val inputMonthlyRent: Double,
    
    // ═══════════════════════════════════════════════════════════════════════════
    // EXCEL'DEN BEKLENEN ÇIKTILAR
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Beklenen satın alma masrafları (Tapu + Emlakçı)
     */
    val expectedPurchaseExpenses: Double,
    
    /**
     * Beklenen kredi tutarı
     */
    val expectedLoanAmount: Double,
    
    /**
     * Beklenen peşinat
     */
    val expectedDownPayment: Double,
    
    /**
     * Beklenen aylık taksit (PMT)
     * ⚠️ Kritik - Excel PMT formülü ile eşleşmeli
     */
    val expectedMonthlyInstallment: Double,
    
    /**
     * Beklenen toplam kredi geri ödemesi
     */
    val expectedTotalLoanRepayment: Double,
    
    /**
     * Beklenen gerçek toplam maliyet
     * ⚠️ Kritik - Ana maliyet metriği
     */
    val expectedRealTotalCost: Double,
    
    /**
     * Beklenen brüt yıllık kira
     */
    val expectedGrossAnnualRent: Double,
    
    /**
     * Beklenen yıllık vergi
     */
    val expectedAnnualTax: Double,
    
    /**
     * Beklenen net yıllık kira
     */
    val expectedNetAnnualRent: Double,
    
    /**
     * Beklenen amortisman süresi (yıl)
     * ⚠️ En kritik - Nihai KPI
     */
    val expectedAmortizationYears: Double
)
