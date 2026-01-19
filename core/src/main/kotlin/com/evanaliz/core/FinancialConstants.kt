package com.evanaliz.core

/**
 * Finansal Sabitler
 * 
 * Bu sabitler Türk gayrimenkul yatırım modelinin temel parametrelerini tanımlar.
 * Değerler Excel modeli "Gelişmiş Ev Yatırım Modeli" ile birebir uyumludur.
 * 
 * ⚠️ Bu değerler çalışma zamanında değiştirilemez.
 * Değişiklik gerekiyorsa, bu dosyada manuel güncelleme yapılmalıdır.
 */
object FinancialConstants {

    // ═══════════════════════════════════════════════════════════════════════════
    // SATIN ALMA MALİYETLERİ
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Satın Alma Masraf Oranı (%7)
     * 
     * İçerir:
     * - Tapu harcı: %4
     * - Emlakçı komisyonu: %3
     * 
     * Formül: purchaseExpenses = housePrice * PURCHASE_EXPENSE_RATE
     */
    const val PURCHASE_EXPENSE_RATE: Double = 0.07

    // ═══════════════════════════════════════════════════════════════════════════
    // KREDİ PARAMETRELERİ
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Kredi Kullanım Oranı (%50)
     * 
     * Ev fiyatının ne kadarının banka kredisi ile karşılanacağını belirler.
     * Kalan kısım peşinat (down payment) olarak ödenir.
     * 
     * Formül: loanAmount = housePrice * LOAN_USAGE_RATIO
     */
    const val LOAN_USAGE_RATIO: Double = 0.50

    /**
     * Aylık Faiz Oranı (%2.49)
     * 
     * Konut kredisi aylık faiz oranı.
     * PMT (Payment) formülünde 'r' parametresi olarak kullanılır.
     * 
     * NOT: Bu aylık oran, yıllık değil!
     */
    const val MONTHLY_INTEREST_RATE: Double = 0.0249

    /**
     * Kredi Vadesi (60 ay = 5 yıl)
     * 
     * Toplam taksit sayısı.
     * PMT formülünde 'n' parametresi olarak kullanılır.
     */
    const val LOAN_TERM_MONTHS: Int = 60

    // ═══════════════════════════════════════════════════════════════════════════
    // KİRA GELİRİ VERGİSİ
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Yıllık Kira Geliri Vergisi İstisnası (58.000 TL)
     * 
     * Yıllık kira gelirinin bu tutarı aşan kısmı vergiye tabidir.
     * 2024/2025 Türkiye Gelir Vergisi Kanunu'na göre.
     * 
     * Formül: taxableIncome = grossAnnualRent - ANNUAL_RENT_TAX_EXEMPTION
     */
    const val ANNUAL_RENT_TAX_EXEMPTION: Double = 58_000.0

    /**
     * Gelir Vergisi Oranı (%20)
     * 
     * İstisna sonrası kalan kira geliri için uygulanan ortalama vergi oranı.
     * Gerçekte kademeli vergi dilimleri var, ancak %20 makul bir ortalamadır.
     * 
     * Formül: annualTax = taxableIncome * INCOME_TAX_RATE
     */
    const val INCOME_TAX_RATE: Double = 0.20
}
