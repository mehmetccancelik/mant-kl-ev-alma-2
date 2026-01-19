package com.evanaliz.core

/**
 * Hesaplama Motoru Test Sınıfı
 * 
 * Bu dosya, InvestmentCalculationEngine'in doğru çalıştığını
 * Excel modeli ile karşılaştırarak doğrular.
 * 
 * Çalıştırmak için: kotlinc ile derleyip java ile çalıştırın
 * veya IDE'de main fonksiyonunu çalıştırın.
 */

fun main() {
    println("═══════════════════════════════════════════════════════════════════════════")
    println("              YATIRIM HESAPLAMA MOTORU - DOĞRULAMA TESTİ                   ")
    println("═══════════════════════════════════════════════════════════════════════════")
    println()

    // Test girdileri
    val testHousePrice = 5_000_000.0        // 5 milyon TL ev
    val testMonthlyRent = 25_000.0          // 25 bin TL aylık kira

    println("📊 TEST GİRDİLERİ:")
    println("   Ev Fiyatı:        ${formatCurrency(testHousePrice)}")
    println("   Aylık Kira:       ${formatCurrency(testMonthlyRent)}")
    println()

    // Hesaplamayı çalıştır
    val result = InvestmentCalculationEngine.calculate(
        housePrice = testHousePrice,
        estimatedMonthlyRent = testMonthlyRent
    )

    // Sonuçları görüntüle
    println("═══════════════════════════════════════════════════════════════════════════")
    println("                            HESAPLAMA SONUÇLARI                            ")
    println("═══════════════════════════════════════════════════════════════════════════")
    println()

    println("🏠 ADIM A - SATIN ALMA & SERMAYE YAPISI:")
    println("   Satın Alma Masrafları:    ${formatCurrency(result.purchaseExpenses)}")
    println("   Kredi Tutarı:             ${formatCurrency(result.loanAmount)}")
    println("   Peşinat:                  ${formatCurrency(result.downPayment)}")
    println()

    println("💳 ADIM B - KREDİ AMORTİSMANI:")
    println("   Aylık Taksit:             ${formatCurrency(result.monthlyInstallment)}")
    println("   Toplam Geri Ödeme:        ${formatCurrency(result.totalLoanRepayment)}")
    println()

    println("💰 ADIM C - GERÇEK TOPLAM MALİYET:")
    println("   ⭐ Gerçek Toplam Maliyet: ${formatCurrency(result.realTotalCost)}")
    println()

    println("🏦 ADIM D - VERGİ SONRASI KİRA GELİRİ:")
    println("   Brüt Yıllık Kira:         ${formatCurrency(result.grossAnnualRent)}")
    println("   Yıllık Vergi:             ${formatCurrency(result.annualTax)}")
    println("   Net Yıllık Kira:          ${formatCurrency(result.netAnnualRent)}")
    println()

    println("⏱️ ADIM E - AMORTİSMAN SÜRESİ:")
    println("   ⭐ Amortisman Yılı:        %.2f yıl".format(result.amortizationYears))
    println()

    println("═══════════════════════════════════════════════════════════════════════════")
    println("                          EXCEL KARŞILAŞTIRMA                              ")
    println("═══════════════════════════════════════════════════════════════════════════")
    println()
    println("Bu değerleri Excel'deki formüllerle karşılaştırın:")
    println()
    println("Excel PMT Formülü: =PMT(0.0249, 60, -2500000)")
    println("Beklenen Aylık Taksit: ~97,617.85 TL")
    println("Hesaplanan:            ${formatCurrency(result.monthlyInstallment)}")
    println()

    // PMT doğrulama
    val expectedPMT = 97617.85 // Yaklaşık Excel sonucu
    val pmtDiff = kotlin.math.abs(result.monthlyInstallment - expectedPMT)
    val pmtMatch = pmtDiff < 1.0 // 1 TL tolerans

    if (pmtMatch) {
        println("✅ PMT formülü Excel ile uyumlu!")
    } else {
        println("❌ PMT formülü farklı! Fark: ${formatCurrency(pmtDiff)}")
    }
    println()
    println("═══════════════════════════════════════════════════════════════════════════")
}

/**
 * Para birimi formatla - sadece test için basit formatlama
 */
private fun formatCurrency(value: Double): String {
    return "%,.2f TL".format(value)
}
