package com.evanaliz.core

import kotlin.math.pow

/**
 * Yatırım Hesaplama Motoru
 * 
 * Bu sınıf, Türk gayrimenkul yatırım analizinin temel hesaplama motorudur.
 * "Gelişmiş Ev Yatırım Modeli" Excel tablosu ile birebir uyumludur.
 * 
 * ╔════════════════════════════════════════════════════════════════════════════╗
 * ║ TASARIM PRENSİPLERİ                                                        ║
 * ╠════════════════════════════════════════════════════════════════════════════╣
 * ║ 1. Saf Kotlin - Android bağımlılığı yok                                    ║
 * ║ 2. Deterministik - Aynı girdi = Aynı çıktı                                 ║
 * ║ 3. Excel Uyumlu - PMT formülü birebir eşleşir                              ║
 * ║ 4. Denetlenebilir - Her adım ayrı ayrı görülebilir                         ║
 * ║ 5. UI'dan Bağımsız - Karar vermez, hesaplar                                ║
 * ╚════════════════════════════════════════════════════════════════════════════╝
 */
object InvestmentCalculationEngine {

    /**
     * Ana hesaplama fonksiyonu.
     * 
     * Tüm finansal hesaplamaları sırasıyla gerçekleştirir ve
     * sonuçları tek bir [CalculationResult] nesnesi olarak döndürür.
     * 
     * @param housePrice Ev fiyatı (TL). Ham sayısal değer, temizlenmiş olmalı.
     * @param estimatedMonthlyRent Tahmini aylık kira geliri (TL). Ham sayısal değer.
     * @return Tüm ara değerler ve nihai amortisman yılını içeren [CalculationResult].
     * 
     * @throws IllegalArgumentException Eğer girdiler geçersizse (negatif veya sıfır).
     */
    fun calculate(
        housePrice: Double,
        estimatedMonthlyRent: Double
    ): CalculationResult {

        // ═══════════════════════════════════════════════════════════════════════
        // GİRDİ DOĞRULAMA
        // ═══════════════════════════════════════════════════════════════════════
        require(housePrice > 0) { 
            "Ev fiyatı sıfırdan büyük olmalıdır. Girilen: $housePrice" 
        }
        require(estimatedMonthlyRent > 0) { 
            "Tahmini aylık kira sıfırdan büyük olmalıdır. Girilen: $estimatedMonthlyRent" 
        }

        // ═══════════════════════════════════════════════════════════════════════
        // ADIM A: SATIN ALMA & SERMAYE YAPISI
        // ═══════════════════════════════════════════════════════════════════════
        
        // A1: Satın alma masrafları (tapu + emlakçı)
        // Excel: =evFiyati * 0.07
        val purchaseExpenses = housePrice * FinancialConstants.PURCHASE_EXPENSE_RATE

        // A2: Bankadan çekilecek kredi miktarı
        // Excel: =evFiyati * 0.50
        val loanAmount = housePrice * FinancialConstants.LOAN_USAGE_RATIO

        // A3: Cepten ödenecek peşinat
        // Excel: =evFiyati - krediTutari
        val downPayment = housePrice - loanAmount

        // ═══════════════════════════════════════════════════════════════════════
        // ADIM B: KREDİ AMORTİSMANI (PMT FORMÜLÜ)
        // ═══════════════════════════════════════════════════════════════════════
        
        // PMT Formülü: P * [r(1+r)^n] / [(1+r)^n - 1]
        // Excel: =PMT(aylikFaiz, vade, -krediTutari)
        
        val r = FinancialConstants.MONTHLY_INTEREST_RATE
        val n = FinancialConstants.LOAN_TERM_MONTHS

        // B1: (1 + r)^n hesapla - bileşik faiz çarpanı
        val compoundFactor = (1 + r).pow(n)

        // B2: Aylık taksit hesapla (PMT)
        // Formül: loanAmount * [r * compoundFactor] / [compoundFactor - 1]
        val monthlyInstallment = loanAmount * (r * compoundFactor) / (compoundFactor - 1)

        // B3: Toplam geri ödeme
        // Excel: =aylikTaksit * vade
        val totalLoanRepayment = monthlyInstallment * n

        // ═══════════════════════════════════════════════════════════════════════
        // ADIM C: GERÇEK TOPLAM MALİYET
        // ═══════════════════════════════════════════════════════════════════════
        
        // Cebinizden çıkacak GERÇEK para:
        // - Peşinat (banka dışı)
        // - Tüm kredi ödemeleri (anapara + faiz)
        // - Satın alma masrafları
        // Excel: =pesinat + toplamGeriOdeme + masraflar
        val realTotalCost = downPayment + totalLoanRepayment + purchaseExpenses

        // ═══════════════════════════════════════════════════════════════════════
        // ADIM D: VERGİ SONRASI KİRA GELİRİ
        // ═══════════════════════════════════════════════════════════════════════
        
        // D1: Brüt yıllık kira
        // Excel: =aylikKira * 12
        val grossAnnualRent = estimatedMonthlyRent * 12

        // D2: Vergiye tabi gelir (istisna sonrası)
        // Excel: =MAX(0, brutYillikKira - istisna)
        val taxableIncome = grossAnnualRent - FinancialConstants.ANNUAL_RENT_TAX_EXEMPTION

        // D3: Yıllık vergi
        // Excel: =EĞER(vergilendirilecekGelir > 0, vergilendirilecekGelir * vergiOrani, 0)
        val annualTax = if (taxableIncome > 0) {
            taxableIncome * FinancialConstants.INCOME_TAX_RATE
        } else {
            0.0
        }

        // D4: Net yıllık kira (elde edilen gerçek gelir)
        // Excel: =brutYillikKira - yillikVergi
        val netAnnualRent = grossAnnualRent - annualTax

        // ═══════════════════════════════════════════════════════════════════════
        // ADIM E: AMORTİSMAN SÜRESİ (NİHAİ KPI)
        // ═══════════════════════════════════════════════════════════════════════
        
        // Yatırımın kendini kaç yılda amorti edeceği
        // Excel: =gercekToplamMaliyet / netYillikKira
        val amortizationYears = realTotalCost / netAnnualRent

        // ═══════════════════════════════════════════════════════════════════════
        // SONUÇ NESNESİ OLUŞTUR
        // ═══════════════════════════════════════════════════════════════════════
        
        return CalculationResult(
            // Girdiler
            housePrice = housePrice,
            estimatedMonthlyRent = estimatedMonthlyRent,
            // Adım A
            purchaseExpenses = purchaseExpenses,
            loanAmount = loanAmount,
            downPayment = downPayment,
            // Adım B
            monthlyInstallment = monthlyInstallment,
            totalLoanRepayment = totalLoanRepayment,
            // Adım C
            realTotalCost = realTotalCost,
            // Adım D
            grossAnnualRent = grossAnnualRent,
            annualTax = annualTax,
            netAnnualRent = netAnnualRent,
            // Adım E
            amortizationYears = amortizationYears
        )
    }
}
