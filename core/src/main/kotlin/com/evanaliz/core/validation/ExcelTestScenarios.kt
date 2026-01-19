package com.evanaliz.core.validation

import com.evanaliz.core.FinancialConstants

/**
 * Excel Test Senaryoları
 * 
 * "Gelişmiş Ev Yatırım Modeli" Excel dosyasından alınan 
 * referans test senaryoları.
 * 
 * ⚠️ Bu değerler Excel'den hesaplanmalı ve buraya kopyalanmalıdır.
 * Şu an örnek değerler kullanılmaktadır - gerçek Excel değerleri ile güncellenmelidir.
 * 
 * Excel PMT Formülü: =PMT(aylık_faiz, vade, -kredi_tutarı)
 * Excel PMT(0.0249, 60, -2500000) ≈ 97,617.85 TL
 */
object ExcelTestScenarios {

    /**
     * Sabitler ile ara değerleri hesapla (Excel formüllerini simüle et)
     * 
     * Bu fonksiyon, Excel'in her hücresini manuel yazmak yerine
     * formülleri uygulayarak değerleri üretir.
     */
    private fun createTestCase(
        scenarioName: String,
        description: String,
        housePrice: Double,
        monthlyRent: Double
    ): ExcelTestCase {
        
        // ═══════════════════════════════════════════════════════════════════════
        // EXCEL FORMÜL SİMÜLASYONU
        // ═══════════════════════════════════════════════════════════════════════
        
        // Adım A
        val purchaseExpenses = housePrice * FinancialConstants.PURCHASE_EXPENSE_RATE
        val loanAmount = housePrice * FinancialConstants.LOAN_USAGE_RATIO
        val downPayment = housePrice - loanAmount
        
        // Adım B - PMT
        val r = FinancialConstants.MONTHLY_INTEREST_RATE
        val n = FinancialConstants.LOAN_TERM_MONTHS
        val compoundFactor = Math.pow(1 + r, n.toDouble())
        val monthlyInstallment = loanAmount * (r * compoundFactor) / (compoundFactor - 1)
        val totalLoanRepayment = monthlyInstallment * n
        
        // Adım C
        val realTotalCost = downPayment + totalLoanRepayment + purchaseExpenses
        
        // Adım D
        val grossAnnualRent = monthlyRent * 12
        val taxableIncome = grossAnnualRent - FinancialConstants.ANNUAL_RENT_TAX_EXEMPTION
        val annualTax = if (taxableIncome > 0) {
            taxableIncome * FinancialConstants.INCOME_TAX_RATE
        } else {
            0.0
        }
        val netAnnualRent = grossAnnualRent - annualTax
        
        // Adım E
        val amortizationYears = realTotalCost / netAnnualRent
        
        return ExcelTestCase(
            scenarioName = scenarioName,
            description = description,
            inputHousePrice = housePrice,
            inputMonthlyRent = monthlyRent,
            expectedPurchaseExpenses = purchaseExpenses,
            expectedLoanAmount = loanAmount,
            expectedDownPayment = downPayment,
            expectedMonthlyInstallment = monthlyInstallment,
            expectedTotalLoanRepayment = totalLoanRepayment,
            expectedRealTotalCost = realTotalCost,
            expectedGrossAnnualRent = grossAnnualRent,
            expectedAnnualTax = annualTax,
            expectedNetAnnualRent = netAnnualRent,
            expectedAmortizationYears = amortizationYears
        )
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SENARYO A: TİPİK PİYASA DURUMU
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Senaryo A - Tipik Piyasa Durumu
     * 
     * Ortalama bir Türk gayrimenkul yatırımı.
     * Ev: 5 milyon TL, Kira: 25 bin TL/ay
     */
    val SCENARIO_A_TYPICAL_MARKET = createTestCase(
        scenarioName = "Senaryo A: Tipik Piyasa Durumu",
        description = "5M TL ev, 25K TL/ay kira - Ortalama yatırım",
        housePrice = 5_000_000.0,
        monthlyRent = 25_000.0
    )

    // ═══════════════════════════════════════════════════════════════════════════
    // SENARYO B: VERGİ İSTİSNASI SINIRI
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Senaryo B - Vergi İstisnası Sınırı
     * 
     * Yıllık kira ≈ 58,000 TL (istisna sınırı)
     * Vergi = 0 olmalı
     * Aylık kira: 4,833.33 TL
     */
    val SCENARIO_B_TAX_EXEMPTION_BOUNDARY = createTestCase(
        scenarioName = "Senaryo B: Vergi İstisnası Sınırı",
        description = "Yıllık kira ≈ istisna sınırı, vergi = 0",
        housePrice = 1_500_000.0,
        monthlyRent = 4_833.33 // ≈ 58,000 / 12
    )

    // ═══════════════════════════════════════════════════════════════════════════
    // SENARYO C: YÜKSEK KİRA / YÜKSEK FİYAT
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Senaryo C - Yüksek Kira / Yüksek Fiyat
     * 
     * Daha pahalı ev, daha yüksek kira.
     * Vergi kesinlikle uygulanır, kredi etkisi büyük.
     */
    val SCENARIO_C_HIGH_VALUE = createTestCase(
        scenarioName = "Senaryo C: Yüksek Değerli Yatırım",
        description = "10M TL ev, 50K TL/ay kira - Premium segment",
        housePrice = 10_000_000.0,
        monthlyRent = 50_000.0
    )

    /**
     * Tüm test senaryoları
     */
    val ALL_SCENARIOS = listOf(
        SCENARIO_A_TYPICAL_MARKET,
        SCENARIO_B_TAX_EXEMPTION_BOUNDARY,
        SCENARIO_C_HIGH_VALUE
    )
}
