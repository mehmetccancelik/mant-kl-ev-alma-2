package com.evanaliz.core

/**
 * Hesaplama Sonuç Modeli
 * 
 * Bu immutable (değiştirilemez) data class, finansal hesaplama motorunun
 * tüm çıktılarını tek bir yapıda toplar.
 * 
 * Tüm ara değerler ve nihai metrik bu sınıfta yer alır.
 * Excel denetimine uygun şekilde her değer ayrı ayrı erişilebilir.
 * 
 * ⚠️ Bu sınıf karar (verdict) içermez. Sadece sayısal gerçekleri taşır.
 */
data class CalculationResult(

    // ═══════════════════════════════════════════════════════════════════════════
    // GİRDİLER (Referans İçin)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Ev Fiyatı (TL)
     * Kullanıcı tarafından girilen ilan fiyatı.
     */
    val housePrice: Double,

    /**
     * Tahmini Aylık Kira (TL)
     * Kullanıcı tarafından girilen beklenen kira geliri.
     */
    val estimatedMonthlyRent: Double,

    // ═══════════════════════════════════════════════════════════════════════════
    // ADIM A: SATIN ALMA & SERMAYE YAPISI
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Satın Alma Masrafları (TL)
     * Tapu harcı + emlakçı komisyonu.
     * 
     * Formül: housePrice * PURCHASE_EXPENSE_RATE
     */
    val purchaseExpenses: Double,

    /**
     * Kredi Tutarı (TL)
     * Bankadan çekilecek kredi miktarı.
     * 
     * Formül: housePrice * LOAN_USAGE_RATIO
     */
    val loanAmount: Double,

    /**
     * Peşinat (TL)
     * Cepten ödenecek ilk tutar.
     * 
     * Formül: housePrice - loanAmount
     */
    val downPayment: Double,

    // ═══════════════════════════════════════════════════════════════════════════
    // ADIM B: KREDİ AMORTİSMANI
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Aylık Taksit (TL)
     * Her ay bankaya ödenecek sabit tutar.
     * 
     * Formül: PMT = P * [r(1+r)^n] / [(1+r)^n - 1]
     * P = loanAmount, r = MONTHLY_INTEREST_RATE, n = LOAN_TERM_MONTHS
     */
    val monthlyInstallment: Double,

    /**
     * Toplam Kredi Geri Ödemesi (TL)
     * Kredi vadesi boyunca bankaya ödenen toplam tutar.
     * 
     * Formül: monthlyInstallment * LOAN_TERM_MONTHS
     */
    val totalLoanRepayment: Double,

    // ═══════════════════════════════════════════════════════════════════════════
    // ADIM C: GERÇEK TOPLAM MALİYET
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Gerçek Toplam Maliyet (TL)
     * 
     * ⭐ KRİTİK METRİK ⭐
     * Evi satın almak için cebinizden çıkacak GERÇEK toplam para.
     * İlan fiyatı DEĞİL, tüm maliyetler dahil gerçek maliyet.
     * 
     * Formül: downPayment + totalLoanRepayment + purchaseExpenses
     */
    val realTotalCost: Double,

    // ═══════════════════════════════════════════════════════════════════════════
    // ADIM D: VERGİ SONRASI KİRA GELİRİ
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Brüt Yıllık Kira (TL)
     * Vergi öncesi toplam yıllık kira geliri.
     * 
     * Formül: estimatedMonthlyRent * 12
     */
    val grossAnnualRent: Double,

    /**
     * Yıllık Vergi (TL)
     * İstisna sonrası kira geliri üzerinden hesaplanan vergi.
     * 
     * Formül: max(0, grossAnnualRent - ANNUAL_RENT_TAX_EXEMPTION) * INCOME_TAX_RATE
     */
    val annualTax: Double,

    /**
     * Net Yıllık Kira (TL)
     * Vergi sonrası elde edilen gerçek kira geliri.
     * 
     * Formül: grossAnnualRent - annualTax
     */
    val netAnnualRent: Double,

    // ═══════════════════════════════════════════════════════════════════════════
    // ADIM E: AMORTİSMAN SÜRESİ
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Amortisman Yılı
     * 
     * ⭐ NİHAİ KPI ⭐
     * Yatırımın kira gelirleri ile kendini kaç yılda amorti edeceği.
     * Bu, tüm sistemin en önemli çıktısıdır.
     * 
     * Formül: realTotalCost / netAnnualRent
     */
    val amortizationYears: Double
)
