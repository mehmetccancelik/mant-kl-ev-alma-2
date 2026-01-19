package com.evanaliz.core.verdict

/**
 * Yatırım Kategorisi
 * 
 * Yatırımın genel değerlendirmesini ifade eden enum.
 * Sadece iki olası sonuç vardır - ara değer yoktur.
 */
enum class InvestmentCategory {
    
    /**
     * MANTIKLI YATIRIM
     * 
     * Amortisman süresi kabul edilebilir aralıkta.
     * Yatırım yapmak finansal açıdan mantıklı.
     */
    LOGICAL,
    
    /**
     * MANTIKSIZ / PAHALI
     * 
     * Amortisman süresi çok uzun.
     * Yatırım yapmak finansal açıdan mantıksız.
     */
    OVERPRICED
}
