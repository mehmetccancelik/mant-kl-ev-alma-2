package com.evanaliz.integration

import android.util.Log
import com.evanaliz.accessibility.ExtractionResult
import com.evanaliz.accessibility.TextDetectionRules

/**
 * Fiyat/Kira Ayrıştırıcı
 */
object PriceRentDiscriminator {

    private const val TAG = "EvAnaliz_Discrim"
    
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
        Log.d(TAG, "discriminate() START - source: ${extractionResult.sourcePackage}")
        
        if (!extractionResult.isSuccessful) {
            Log.e(TAG, "Extraction failed: ${extractionResult.errorMessage}")
            return ParseResult.Failure(
                error = ParseError.UnexpectedError(
                    extractionResult.errorMessage ?: "Bilinmeyen hata"
                ),
                rawTexts = extractionResult.extractedTexts
            )
        }
        
        val texts = extractionResult.extractedTexts
        Log.d(TAG, "Extracted texts count: ${texts.size}")
        texts.forEachIndexed { index, s -> Log.v(TAG, "Text[$index]: $s") }

        if (texts.isEmpty()) {
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

        // Önce tam koordinat formatını ara
        for (text in texts) {
            val coords = TextToNumberParser.parseCoordinates(text)
            if (coords != null) {
                latitude = coords.first
                longitude = coords.second
                Log.i(TAG, "Full coordinate found: $latitude, $longitude in text: \"$text\"")
                break 
            }
        }

        // Eğer bulamadıysak, ayrı parça olarak ara (Google Maps'te genelde ayrı satırlardadır)
        if (latitude == null || longitude == null) {
            var latFound: Double? = null
            var lonFound: Double? = null
            
            for (text in texts) {
                // Sadece N/S/E/W içerenleri değil, Türkiye aralığındaki sayıları da ara
                val regex = Regex("""^(\d{2}\.\d{3,})""")
                val match = regex.find(text.trim())
                if (match != null) {
                    val value = match.groupValues[1].toDoubleOrNull()
                    if (value != null) {
                        // Türkiye Latitude: 36-42
                        if (value in 35.0..43.0 && latFound == null) {
                            latFound = value
                            Log.d(TAG, "Potential Latitude found: $value")
                        }
                        // Türkiye Longitude: 26-45
                        else if (value in 25.0..46.0 && lonFound == null) {
                            lonFound = value
                            Log.d(TAG, "Potential Longitude found: $value")
                        }
                    }
                }
                
                // Derece formatı (tekli)
                if (text.contains("°")) {
                    if (text.contains("N") || text.contains("S")) {
                        // DMS parsing needed or just simple decimal + degree
                    }
                }
            }
            
            if (latFound != null && lonFound != null) {
                latitude = latFound
                longitude = lonFound
                Log.i(TAG, "Combined coordinates from separate parts: $latitude, $longitude")
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
        
        // Eğer hiçbir sayı bulunamadıysa VE koordinat da yoksa hata döndür
        if (allValues.isEmpty() && latitude == null) {
            return ParseResult.Failure(
                error = ParseError.NoDataFound,
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
