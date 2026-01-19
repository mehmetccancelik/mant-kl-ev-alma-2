package com.evanaliz.integration

import com.evanaliz.accessibility.ExtractionResult
import com.evanaliz.accessibility.TextDetectionRules

/**
 * Fiyat/Kira Ayrıştırıcı
 * 
 * Bulunan sayısal değerlerden hangisinin ev fiyatı, hangisinin kira olduğunu
 * akıllı kurallarla belirler.
 * 
 * ╔════════════════════════════════════════════════════════════════════════════╗
 * ║ AYRIŞTIRMA KURALLARI                                                       ║
 * ╠════════════════════════════════════════════════════════════════════════════╣
 * ║ 1. En büyük değer = Ev fiyatı (genellikle milyonlar)                       ║
 * ║ 2. Makul aralıktaki ikinci değer = Kira (binler)                           ║
 * ║ 3. Ev fiyatı > 100.000 TL olmalı                                           ║
 * ║ 4. Kira < Ev fiyatının %5'i olmalı                                         ║
 * ╚════════════════════════════════════════════════════════════════════════════╝
 */
object PriceRentDiscriminator {

    /**
     * Minimum ev fiyatı (TL)
     */
    private const val MIN_HOUSE_PRICE = 100_000.0

    /**
     * Maksimum ev fiyatı (TL)
     */
    private const val MAX_HOUSE_PRICE = 100_000_000.0

    /**
     * Minimum aylık kira (TL)
     */
    private const val MIN_RENT = 1_000.0

    /**
     * Maksimum aylık kira (TL)
     */
    private const val MAX_RENT = 500_000.0

    /**
     * Kiranın fiyata maksimum oranı
     * Aylık kira, ev fiyatının %5'inden fazla olamaz (yıllık %60 getiri mantıksız)
     */
    private const val MAX_RENT_TO_PRICE_RATIO = 0.05

    /**
     * Extraction sonucunu parse edilmiş veriye dönüştür.
     */
    fun discriminate(extractionResult: ExtractionResult): ParseResult {
        
        if (!extractionResult.isSuccessful) {
            return ParseResult.Failure(
                error = ParseError.UnexpectedError(
                    extractionResult.errorMessage ?: "Bilinmeyen hata"
                ),
                rawTexts = extractionResult.extractedTexts
            )
        }
        
        if (extractionResult.extractedTexts.isEmpty()) {
            return ParseResult.Failure(
                error = ParseError.NoDataFound,
                rawTexts = emptyList()
            )
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // 1. KOORDİNAT FABRİKASI
        // ═══════════════════════════════════════════════════════════════════════════
        var latitude: Double? = null
        var longitude: Double? = null
        val texts = extractionResult.extractedTexts

        // Koordinat bul
        for (text in texts) {
            if (TextDetectionRules.isCoordinate(text)) {
                val coords = TextToNumberParser.parseCoordinates(text)
                if (coords != null) {
                    latitude = coords.first
                    longitude = coords.second
                    break // İlk bulduğumuzu al
                }
            }
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // 2. ETİKETLİ FİYAT ARAMA (LABEL-BASED)
        // ═══════════════════════════════════════════════════════════════════════════
        var labeledPrice: Double? = null
        
        for (i in 0 until texts.size - 1) {
            val current = texts[i].trim()
            if (current.equals("Fiyat", ignoreCase = true) || current.equals("FİYAT", ignoreCase = true)) {
                // Etiket bulundu, bir sonrakine bak
                val next = texts[i + 1]
                val value = TextToNumberParser.parse(next)
                
                if (value != null && value >= MIN_HOUSE_PRICE) {
                    labeledPrice = value
                    break
                }
            }
        }
        
        // Tüm metinleri sayıya çevir
        val allValues = TextToNumberParser.parseAll(extractionResult.extractedTexts)
            .filter { it > 0 }
            .sortedDescending()
        
        if (allValues.isEmpty()) {
            return ParseResult.Failure(
                error = ParseError.InvalidFormat,
                rawTexts = extractionResult.extractedTexts
            )
        }
        
        // Ev fiyatı adaylarını bul
        val priceCandidates = allValues.filter { 
            it in MIN_HOUSE_PRICE..MAX_HOUSE_PRICE 
        }
        
        // Kira adaylarını bul
        val rentCandidates = allValues.filter { 
            it in MIN_RENT..MAX_RENT 
        }
        
        // En iyi eşleşmeyi bul
        var bestPrice: Double? = labeledPrice // Varsa etiketli fiyatı kullan
        var bestRent: Double? = null
        
        for (price in priceCandidates) {
            for (rent in rentCandidates) {
                // Eğer etiketli fiyat varsa, sadece onu kullan
                if (bestPrice != null && price != bestPrice) continue

                // Kira, fiyattan farklı olmalı
                if (rent == price) continue
                
                // Kira, fiyattan küçük olmalı
                if (rent >= price) continue
                
                // Kira/fiyat oranı makul olmalı
                if (rent / price > MAX_RENT_TO_PRICE_RATIO) continue
                
                // İlk geçerli eşleşmeyi al
                bestPrice = price
                bestRent = rent
                break
            }
            if (bestPrice != null) break
        }
        
        // Eğer kira bulunamadıysa, en büyük değeri fiyat olarak al
        if (bestPrice == null && priceCandidates.isNotEmpty()) {
            bestPrice = priceCandidates.first()
        }
        
        // Eğer kira hâlâ bulunamadıysa ve birden fazla değer varsa
        if (bestRent == null && allValues.size > 1) {
            // En büyük olmayan ve makul aralıkta olan ilk değeri kira olarak al
            bestRent = allValues
                .filter { it != bestPrice && it in MIN_RENT..MAX_RENT }
                .firstOrNull()
        }
        
        val parsedData = ParsedScreenData(
            housePrice = bestPrice,
            estimatedMonthlyRent = bestRent,
            allDetectedValues = allValues,
            sourcePackage = extractionResult.sourcePackage,
            latitude = latitude,
            longitude = longitude
        )
        
        return if (parsedData.isComplete) {
            ParseResult.Success(parsedData)
        } else {
            ParseResult.Failure(
                error = ParseError.InsufficientData,
                rawTexts = extractionResult.extractedTexts
            )
        }
    }
}
