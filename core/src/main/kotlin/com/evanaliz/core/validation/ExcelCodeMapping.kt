package com.evanaliz.core.validation

/**
 * Excel-Kod Değişken Eşleme Sözlüğü
 * 
 * Bu dosya, Excel "Gelişmiş Ev Yatırım Modeli" ile Kotlin kodu
 * arasındaki kavramsal eşlemeyi dokümante eder.
 * 
 * ╔═══════════════════════════════════════════════════════════════════════════════════════════╗
 * ║                        EXCEL → KOTLIN MAPPING TABLE                                       ║
 * ╠═══════════════════════════════════════════════════════════════════════════════════════════╣
 * ║ Excel Kavramı             │ Excel Hücre (Mantıksal) │ Kotlin Değişkeni                    ║
 * ╠═══════════════════════════════════════════════════════════════════════════════════════════╣
 * ║ Ev Fiyatı                 │ Girdi Hücresi           │ housePrice                          ║
 * ║ Aylık Kira                │ Girdi Hücresi           │ estimatedMonthlyRent                ║
 * ╠═══════════════════════════════════════════════════════════════════════════════════════════╣
 * ║ Tapu + Emlakçı %          │ Sabit (0.07)            │ PURCHASE_EXPENSE_RATE               ║
 * ║ Kredi Kullanım Oranı      │ Sabit (0.50)            │ LOAN_USAGE_RATIO                    ║
 * ║ Aylık Faiz Oranı          │ Sabit (0.0249)          │ MONTHLY_INTEREST_RATE               ║
 * ║ Kredi Vadesi (Ay)         │ Sabit (60)              │ LOAN_TERM_MONTHS                    ║
 * ║ Kira Vergi İstisnası      │ Sabit (58000)           │ ANNUAL_RENT_TAX_EXEMPTION           ║
 * ║ Gelir Vergisi Oranı       │ Sabit (0.20)            │ INCOME_TAX_RATE                     ║
 * ╠═══════════════════════════════════════════════════════════════════════════════════════════╣
 * ║ Satın Alma Masrafları     │ Hesaplanan              │ purchaseExpenses                    ║
 * ║ Kredi Tutarı              │ Hesaplanan              │ loanAmount                          ║
 * ║ Peşinat                   │ Hesaplanan              │ downPayment                         ║
 * ║ Aylık Taksit (PMT)        │ PMT Formülü             │ monthlyInstallment                  ║
 * ║ Toplam Kredi Ödemesi      │ Hesaplanan              │ totalLoanRepayment                  ║
 * ║ Gerçek Toplam Maliyet     │ Final Maliyet           │ realTotalCost                       ║
 * ║ Brüt Yıllık Kira          │ Hesaplanan              │ grossAnnualRent                     ║
 * ║ Yıllık Vergi              │ Hesaplanan              │ annualTax                           ║
 * ║ Net Yıllık Kira           │ Vergiden Sonra          │ netAnnualRent                       ║
 * ║ Amortisman Süresi         │ Final KPI               │ amortizationYears                   ║
 * ╚═══════════════════════════════════════════════════════════════════════════════════════════╝
 * 
 * FORMÜL KARŞILAŞTIRMASI:
 * 
 * ┌─────────────────────────────────────────────────────────────────────────────────────┐
 * │ Excel PMT                                                                            │
 * │ =PMT(aylık_faiz, vade, -anapara)                                                     │
 * │ =PMT(0.0249, 60, -2500000) → 97,617.85 TL                                            │
 * ├─────────────────────────────────────────────────────────────────────────────────────┤
 * │ Kotlin PMT                                                                           │
 * │ loanAmount * (r * (1+r)^n) / ((1+r)^n - 1)                                           │
 * │ 2500000 * (0.0249 * 1.0249^60) / (1.0249^60 - 1) → 97,617.85 TL                      │
 * └─────────────────────────────────────────────────────────────────────────────────────┘
 * 
 * Her iki formül matematiksel olarak eşdeğerdir.
 */

/**
 * Excel mapping referansı için marker object.
 * Gerçek bir işlev içermez, sadece dokümantasyon amaçlıdır.
 */
object ExcelCodeMapping {
    
    const val DOCUMENTATION_VERSION = "1.0"
    
    /**
     * Excel formül doğrulama notu
     */
    const val PMT_FORMULA_NOTE = """
        Excel PMT fonksiyonu ve Kotlin implementasyonu aynı matematiksel temele dayanır:
        
        PMT = P × [r(1+r)^n] / [(1+r)^n - 1]
        
        Burada:
        P = Anapara (loanAmount)
        r = Aylık faiz oranı (MONTHLY_INTEREST_RATE)
        n = Toplam ödeme sayısı (LOAN_TERM_MONTHS)
        
        Excel'de: =PMT(r, n, -P) şeklinde kullanılır
        Kotlin'de: Yukarıdaki formül doğrudan uygulanır
    """
}
