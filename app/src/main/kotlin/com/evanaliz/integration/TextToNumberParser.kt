package com.evanaliz.integration

/**
 * Metin → Sayı Dönüştürücü
 * 
 * Türk para formatındaki metinleri Double'a çevirir.
 * 
 * Desteklenen formatlar:
 * - "3.500.000 TL"
 * - "₺25.000"
 * - "5000000"
 * - "25,000.50"
 */
object TextToNumberParser {

    // Sayı olmayan karakterler (para birimi vs) için regex
    private val NON_NUMERIC_REGEX = Regex("[^0-9.,]")
    
    // Birden fazla nokta/virgül için temizlik
    private val MULTIPLE_SEPARATORS_REGEX = Regex("[.,](?=[^.,]*[.,])")

    /**
     * Metni Double'a çevir.
     * 
     * @param text Ham metin (örn: "3.500.000 TL")
     * @return Double veya null (parse edilemezse)
     */
    fun parse(text: String?): Double? {
        if (text.isNullOrBlank()) return null
        
        return try {
            // 1. Temizle: TL, ₺, boşlukları kaldır
            var cleaned = text
                .replace("TL", "")
                .replace("₺", "")
                .replace(" ", "")
                .trim()
            
            // 2. Sayı karakterleri dışındakileri kaldır
            cleaned = NON_NUMERIC_REGEX.replace(cleaned, "")
            
            if (cleaned.isBlank()) return null
            
            // 3. Türk formatını anla
            // Türk formatı: 3.500.000,50 (nokta binlik, virgül ondalık)
            // Amerikan formatı: 3,500,000.50 (virgül binlik, nokta ondalık)
            
            val dotCount = cleaned.count { it == '.' }
            val commaCount = cleaned.count { it == ',' }
            
            when {
                // Sadece nokta var ve birden fazla → Türk binlik ayırıcı
                dotCount > 1 && commaCount == 0 -> {
                    cleaned = cleaned.replace(".", "")
                }
                
                // Nokta ve virgül var → Türk formatı (nokta binlik, virgül ondalık)
                dotCount > 0 && commaCount == 1 -> {
                    cleaned = cleaned.replace(".", "").replace(",", ".")
                }
                
                // Sadece virgül var ve birden fazla → Amerikan binlik ayırıcı
                commaCount > 1 && dotCount == 0 -> {
                    cleaned = cleaned.replace(",", "")
                }
                
                // Virgül ve nokta var, nokta sonda → Amerikan formatı
                commaCount > 0 && dotCount == 1 && cleaned.lastIndexOf('.') > cleaned.lastIndexOf(',') -> {
                    cleaned = cleaned.replace(",", "")
                }
                
                // Tek nokta var → muhtemelen ondalık (Amerikan) veya binlik (Türk)
                dotCount == 1 && commaCount == 0 -> {
                    // Noktadan sonra 3 rakam varsa → binlik ayırıcı
                    val afterDot = cleaned.substringAfterLast('.')
                    if (afterDot.length == 3) {
                        cleaned = cleaned.replace(".", "")
                    }
                    // Aksi halde ondalık olarak bırak
                }
                
                // Tek virgül var → muhtemelen ondalık (Türk)
                commaCount == 1 && dotCount == 0 -> {
                    val afterComma = cleaned.substringAfterLast(',')
                    if (afterComma.length <= 2) {
                        // Ondalık virgül
                        cleaned = cleaned.replace(",", ".")
                    } else {
                        // Binlik ayırıcı
                        cleaned = cleaned.replace(",", "")
                    }
                }
            }
            
            // 4. Double'a çevir
            cleaned.toDoubleOrNull()
            
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Birden fazla metni parse et ve listeyi döndür.
     */
    fun parseAll(texts: List<String>): List<Double> {
        return texts.mapNotNull { parse(it) }
    }
}
