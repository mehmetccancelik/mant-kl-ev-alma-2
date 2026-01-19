package com.evanaliz.integration

import com.evanaliz.accessibility.ExtractionResult
import com.evanaliz.core.CalculationResult
import com.evanaliz.core.InvestmentCalculationEngine
import com.evanaliz.core.verdict.InvestmentVerdict
import com.evanaliz.core.verdict.VerdictEngine

/**
 * Entegrasyon Sonucu
 * 
 * Tüm pipeline'ın çıktısı - extraction'dan verdict'e kadar.
 */
sealed class IntegrationResult {
    
    /**
     * Başarılı sonuç
     */
    data class Success(
        val parsedData: ParsedScreenData,
        val calculationResult: CalculationResult,
        val verdict: InvestmentVerdict
    ) : IntegrationResult()
    
    /**
     * Kısmi başarı - sadece fiyat bulundu
     */
    data class PartialSuccess(
        val parsedData: ParsedScreenData,
        val message: String
    ) : IntegrationResult()
    
    /**
     * Hata
     */
    data class Error(
        val error: ParseError,
        val rawTexts: List<String>,
        val userMessage: String
    ) : IntegrationResult()
}

/**
 * Domain Entegrasyon Köprüsü
 * 
 * Accessibility (DOMAIN A) ile Calculation Engine (DOMAIN B) arasındaki köprü.
 * 
 * ╔════════════════════════════════════════════════════════════════════════════╗
 * ║ VERİ AKIŞI                                                                 ║
 * ╠════════════════════════════════════════════════════════════════════════════╣
 * ║ ExtractionResult → Parse → Calculate → Verdict → IntegrationResult        ║
 * ║                                                                            ║
 * ║ DOMAIN A (Android)     │     DOMAIN B (Fintech)                            ║
 * ║ ─────────────────────────────────────────────────────────────────────────  ║
 * ║ ExtractionResult ──────┼───▶ CalculationResult ───▶ InvestmentVerdict     ║
 * ║                        │                                                   ║
 * ║                   KÖPRÜ│                                                   ║
 * ╚════════════════════════════════════════════════════════════════════════════╝
 */
object DomainIntegrationBridge {

    /**
     * Tüm pipeline'ı çalıştır.
     * 
     * @param extractionResult Accessibility'den gelen ham veri
     * @return Entegrasyon sonucu
     */
    fun process(extractionResult: ExtractionResult): IntegrationResult {
        
        // 1. Parse et (Metin → Sayı)
        val parseResult = PriceRentDiscriminator.discriminate(extractionResult)
        
        return when (parseResult) {
            is ParseResult.Failure -> {
                IntegrationResult.Error(
                    error = parseResult.error,
                    rawTexts = parseResult.rawTexts,
                    userMessage = getErrorMessage(parseResult.error)
                )
            }
            
            is ParseResult.Success -> {
                val data = parseResult.data
                
                if (!data.isComplete) {
                    IntegrationResult.PartialSuccess(
                        parsedData = data,
                        message = buildPartialMessage(data)
                    )
                } else {
                    // 2. Hesapla
                    val calculationResult = InvestmentCalculationEngine.calculate(
                        housePrice = data.housePrice!!,
                        estimatedMonthlyRent = data.estimatedMonthlyRent!!
                    )
                    
                    // 3. Karar ver
                    val verdict = VerdictEngine.evaluate(calculationResult)
                    
                    IntegrationResult.Success(
                        parsedData = data,
                        calculationResult = calculationResult,
                        verdict = verdict
                    )
                }
            }
        }
    }

    /**
     * Hata mesajı oluştur (kullanıcı dostu)
     */
    private fun getErrorMessage(error: ParseError): String {
        return when (error) {
            is ParseError.NoDataFound -> 
                "Ekranda fiyat veya kira bilgisi bulunamadı. " +
                "Lütfen bir emlak ilanı sayfasında olduğunuzdan emin olun."
            
            is ParseError.InsufficientData -> 
                "Hem ev fiyatı hem de kira bilgisi gerekli. " +
                "Sadece birini bulabildik."
            
            is ParseError.InvalidFormat -> 
                "Bulunan metinler sayıya çevrilemedi. " +
                "Lütfen fiyatın görünür olduğundan emin olun."
            
            is ParseError.UnexpectedError -> 
                "Beklenmeyen bir hata oluştu: ${error.message}"
        }
    }

    /**
     * Kısmi başarı mesajı oluştur
     */
    private fun buildPartialMessage(data: ParsedScreenData): String {
        return when {
            data.housePrice != null && data.estimatedMonthlyRent == null ->
                "Ev fiyatı bulundu: ${formatCurrency(data.housePrice)}. " +
                "Ancak kira bilgisi bulunamadı."
            
            data.housePrice == null && data.estimatedMonthlyRent != null ->
                "Kira bulundu: ${formatCurrency(data.estimatedMonthlyRent)}. " +
                "Ancak ev fiyatı bulunamadı."
            
            else ->
                "Yeterli veri bulunamadı."
        }
    }

    private fun formatCurrency(value: Double): String {
        return "%,.0f TL".format(value)
    }
}
