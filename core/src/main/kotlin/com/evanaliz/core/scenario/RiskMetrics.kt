package com.evanaliz.core.scenario

/**
 * Risk Metrikleri
 * 
 * Yatırımın risk profilini tanımlayan finansal göstergeler.
 */
data class RiskMetrics(
    
    /**
     * Geri Ödeme Süresi (Yıl)
     * 
     * Yatırımın kira gelirleri ile kendini kaç yılda geri ödediği.
     * 
     * Basit dil: "Paranızı kaç yılda geri alırsınız?"
     */
    val paybackPeriodYears: Double,
    
    /**
     * Net Bugünkü Değer (NPV)
     * 
     * Gelecekteki tüm nakit akışlarının bugünkü değeri eksi ilk yatırım.
     * Pozitif NPV = Yatırım karlı
     * Negatif NPV = Yatırım zararlı
     * 
     * Basit dil: "Bu yatırım bugünün parasıyla ne kadar kazandırır?"
     */
    val npv: Double,
    
    /**
     * İç Verim Oranı (IRR)
     * 
     * Yatırımın yıllık bileşik getiri oranı.
     * IRR > Alternatif getiri → Yatırım mantıklı
     * 
     * Basit dil: "Bu yatırım yılda yüzde kaç kazandırır?"
     */
    val irr: Double,
    
    /**
     * Nakit-Nakit Getiri
     * 
     * İlk yıl nakit akışı / İlk yatırım
     * Kısa vadeli likidite göstergesi.
     * 
     * Basit dil: "İlk yıl paranızın yüzde kaçı size geri döner?"
     */
    val cashOnCashReturn: Double,
    
    /**
     * En Kötü Durum Düşüşü
     * 
     * Kümülatif nakit akışının en düşük noktası.
     * Ne kadar negatife düşebileceğinizi gösterir.
     * 
     * Basit dil: "En kötü durumda ne kadar ekside olursunuz?"
     */
    val worstCaseDrawdown: Double
) {
    /**
     * IRR yüzde formatı
     */
    val irrPercentage: Double
        get() = irr * 100
    
    /**
     * Cash-on-cash yüzde formatı
     */
    val cashOnCashPercentage: Double
        get() = cashOnCashReturn * 100
    
    /**
     * Geri ödeme gerçekleşti mi?
     */
    val isPaybackAchieved: Boolean
        get() = paybackPeriodYears != Double.MAX_VALUE
    
    /**
     * NPV pozitif mi?
     */
    val isNpvPositive: Boolean
        get() = npv > 0
}
