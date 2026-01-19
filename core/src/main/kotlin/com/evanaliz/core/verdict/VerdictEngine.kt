package com.evanaliz.core.verdict

import com.evanaliz.core.CalculationResult

/**
 * Karar Motoru
 * 
 * Finansal hesaplama sonuçlarını yorumlayan ve yatırım kararı üreten motor.
 * 
 * ╔════════════════════════════════════════════════════════════════════════════╗
 * ║ TASARIM PRENSİPLERİ                                                        ║
 * ╠════════════════════════════════════════════════════════════════════════════╣
 * ║ 1. Hesaplama YAPMAZ - Sadece yorumlar                                      ║
 * ║ 2. Tek metrik kullanır - amortizationYears                                 ║
 * ║ 3. Deterministik - Aynı girdi = Aynı karar                                 ║
 * ║ 4. Şeffaf eşikler - Merkezi ve değiştirilebilir                            ║
 * ║ 5. UI'dan bağımsız - Renk/metin önerisi verir, render etmez                ║
 * ╚════════════════════════════════════════════════════════════════════════════╝
 */
object VerdictEngine {

    /**
     * Yatırım kararı üret.
     * 
     * @param calculationResult Hesaplama motorundan (Prompt 1) gelen sonuç.
     * @return Yorumlanmış yatırım kararı.
     */
    fun evaluate(calculationResult: CalculationResult): InvestmentVerdict {
        
        val amortizationYears = calculationResult.amortizationYears
        
        // ═══════════════════════════════════════════════════════════════════════
        // KARAR KURALI
        // ═══════════════════════════════════════════════════════════════════════
        // 
        // Eğer amortisman süresi < 14 yıl → MANTIKLI
        // Eğer amortisman süresi >= 14 yıl → MANTIKSIZ
        //
        val isLogical = amortizationYears < VerdictThresholds.MAX_ACCEPTABLE_AMORTIZATION_YEARS
        
        // ═══════════════════════════════════════════════════════════════════════
        // KARAR ÇIKTILARI
        // ═══════════════════════════════════════════════════════════════════════
        
        val category: InvestmentCategory
        val colorHint: ColorHint
        val statusText: String
        val summaryExplanation: String
        
        if (isLogical) {
            // ✅ MANTIKLI YATIRIM
            category = InvestmentCategory.LOGICAL
            colorHint = ColorHint.GREEN
            statusText = "MANTIKLI YATIRIM ✅"
            summaryExplanation = buildLogicalExplanation(amortizationYears)
        } else {
            // ❌ MANTIKSIZ / PAHALI
            category = InvestmentCategory.OVERPRICED
            colorHint = ColorHint.RED
            statusText = "MANTIKSIZ / PAHALI ❌"
            summaryExplanation = buildOverpricedExplanation(amortizationYears)
        }
        
        return InvestmentVerdict(
            amortizationYears = amortizationYears,
            statusText = statusText,
            category = category,
            colorHint = colorHint,
            summaryExplanation = summaryExplanation
        )
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // AÇIKLAMA METİN ÜRETİCİLERİ
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Mantıklı yatırım için açıklama metni üret.
     */
    private fun buildLogicalExplanation(amortizationYears: Double): String {
        val formattedYears = "%.1f".format(amortizationYears)
        
        return """
            Bu ev, kredi ve masraflar dahil kira geliriyle yaklaşık $formattedYears yılda kendini amorti ediyor.
            
            Bu süre kabul edilebilir aralıkta (${VerdictThresholds.MAX_ACCEPTABLE_AMORTIZATION_YEARS.toInt()} yılın altında) olduğu için yatırım mantıklı olarak değerlendirilmiştir.
            
            Kira geliri ile yatırımınızı makul bir sürede geri kazanabilirsiniz.
        """.trimIndent()
    }
    
    /**
     * Pahalı/mantıksız yatırım için açıklama metni üret.
     */
    private fun buildOverpricedExplanation(amortizationYears: Double): String {
        val formattedYears = "%.1f".format(amortizationYears)
        val threshold = VerdictThresholds.MAX_ACCEPTABLE_AMORTIZATION_YEARS.toInt()
        
        return """
            Bu ev, kredi ve masraflar dahil kira geliriyle yaklaşık $formattedYears yılda kendini amorti ediyor.
            
            Bu süre kabul edilebilir aralığın ($threshold yıl) üzerinde olduğu için yatırım mantıksız/pahalı olarak değerlendirilmiştir.
            
            Bu fiyattan satın almak yerine daha uygun fiyatlı alternatifler aramanız önerilir.
        """.trimIndent()
    }
}
