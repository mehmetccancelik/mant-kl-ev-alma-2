package com.evanaliz.accessibility

/**
 * Metin Algılama Kuralları
 * 
 * Ekrandaki metinlerden gayrimenkul ile ilgili sayısal değerleri
 * tespit etmek için kullanılan kurallar.
 * 
 * ╔════════════════════════════════════════════════════════════════════════════╗
 * ║ ALGILAMA STRATEJİSİ                                                        ║
 * ╠════════════════════════════════════════════════════════════════════════════╣
 * ║ 1. Para birimi içeriyor mu? (TL, ₺)                                        ║
 * ║ 2. Büyük sayı içeriyor mu? (≥ 4 basamak)                                   ║
 * ║                                                                            ║
 * ║ ❌ "fiyat", "kira" gibi etiketlere güvenme                                  ║
 * ║ ❌ Dil varsayımı yapma                                                      ║
 * ╚════════════════════════════════════════════════════════════════════════════╝
 */
object TextDetectionRules {

    // ═══════════════════════════════════════════════════════════════════════════
    // PARA BİRİMİ GÖSTERGELERİ
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Para birimi göstergeleri
     */
    private val CURRENCY_INDICATORS = listOf(
        "TL",
        "₺",
        "tl",
        "Tl"
    )

    /**
     * Minimum basamak sayısı (büyük sayı tespiti için)
     * 
     * 4+ basamaklı sayılar gayrimenkul fiyatı olabilir.
     * Örn: 1000, 25000, 3500000
     */
    private const val MIN_DIGIT_COUNT = 4

    /**
     * Sayı ayırıcı karakterler (Türk formatı)
     */
    private val NUMBER_SEPARATORS = listOf('.', ',', ' ')

    /**
     * Koordinat Regex (DMS formatı) - TAM FORMAT
     * Örn: 40°52'25.0"N 29°18'23.2"E
     */
    private val COORDINATE_REGEX_FULL = Regex("""\d+°\d+'[\d.]+"[NS]\s+\d+°\d+'[\d.]+"[EW]""")

    /**
     * Koordinat Regex - TEK PARÇA (Latitude veya Longitude)
     * Örn: 40°52'25.0"N veya 29°18'23.2"E
     */
    private val COORDINATE_REGEX_SINGLE = Regex("""\d+°\d+'[\d.]+"[NSEW]""")

    /**
     * Ondalık koordinat formatı (tam çift)
     * Örn: 40.873611, 29.306444 veya 40.873611 29.306444
     * En az 3 ondalık basamak yeterli
     */
    private val COORDINATE_REGEX_DECIMAL = Regex("""\d{1,3}\.\d{3,}[,\s]+\d{1,3}\.\d{3,}""")

    /**
     * Google Maps ondalık + derece formatı
     * Örn: 40.873611°N 29.306444°E veya 40.873611° N, 29.306444° E
     */
    private val COORDINATE_REGEX_DECIMAL_DEGREE = Regex("""\d{1,3}\.\d{3,}°\s*[NS][,\s]+\d{1,3}\.\d{3,}°\s*[EW]""")

    /**
     * Tek ondalık koordinat (latitude veya longitude olabilir)
     * Türkiye için: lat 35-43, lon 25-45 arası
     */
    private val COORDINATE_REGEX_SINGLE_DECIMAL = Regex("""\b(3[5-9]|4[0-3])\.\d{4,}\b|\b(2[5-9]|3\d|4[0-5])\.\d{4,}\b""")

    /**
     * Google Maps URL'den koordinat
     * Örn: @40.8736,29.3064
     */
    private val COORDINATE_REGEX_URL = Regex("""@(-?\d{1,3}\.\d+),(-?\d{1,3}\.\d+)""")

    /**
     * Önemli Etiketler
     */
    private val IMPORTANT_LABELS = listOf("Fiyat", "fiyat", "FİYAT")

    // ═══════════════════════════════════════════════════════════════════════════
    // ANA TESPİT FONKSİYONU
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Metin, aday veri mi?
     * 
     * @param text Kontrol edilecek metin
     * @return Para/sayı içeriyorsa true
     */
    fun isCandidateText(text: String?): Boolean {
        if (text.isNullOrBlank()) return false
        
        val trimmed = text.trim()
        if (trimmed.length < 3) return false
        
        // Önemli etiket mi? (Fiyat vb.)
        if (IMPORTANT_LABELS.any { trimmed.contains(it, ignoreCase = true) }) return true

        // Koordinat mı?
        if (isCoordinate(trimmed)) return true

        // Para birimi içeriyor mu?
        if (containsCurrency(trimmed)) return true
        
        // Büyük sayı içeriyor mu?
        if (containsLargeNumber(trimmed)) return true
        
        return false
    }

    /**
     * Koordinat içeriyor mu?
     */
    fun isCoordinate(text: String): Boolean {
        return COORDINATE_REGEX_FULL.containsMatchIn(text) ||
               COORDINATE_REGEX_SINGLE.containsMatchIn(text) ||
               COORDINATE_REGEX_DECIMAL.containsMatchIn(text) ||
               COORDINATE_REGEX_DECIMAL_DEGREE.containsMatchIn(text) ||
               COORDINATE_REGEX_SINGLE_DECIMAL.containsMatchIn(text) ||
               COORDINATE_REGEX_URL.containsMatchIn(text) ||
               text.contains("°") // Derece işareti varsa koordinat olabilir
    }

    /**
     * Metinden koordinat parçasını çıkar
     */
    fun extractCoordinateMatch(text: String): String? {
        COORDINATE_REGEX_FULL.find(text)?.let { return it.value }
        COORDINATE_REGEX_DECIMAL_DEGREE.find(text)?.let { return it.value }
        COORDINATE_REGEX_DECIMAL.find(text)?.let { return it.value }
        COORDINATE_REGEX_URL.find(text)?.let { return it.value }
        COORDINATE_REGEX_SINGLE.find(text)?.let { return it.value }
        COORDINATE_REGEX_SINGLE_DECIMAL.find(text)?.let { return it.value }
        return null
    }

    /**
     * Para birimi göstergesi içeriyor mu?
     */
    fun containsCurrency(text: String): Boolean {
        return CURRENCY_INDICATORS.any { indicator ->
            text.contains(indicator)
        }
    }

    /**
     * Büyük sayı içeriyor mu? (4+ basamak)
     */
    fun containsLargeNumber(text: String): Boolean {
        // Sayı olmayan karakterleri çıkar (ayırıcılar hariç)
        val digitsOnly = extractDigits(text)
        return digitsOnly.length >= MIN_DIGIT_COUNT
    }

    /**
     * Metinden sadece rakamları çıkar
     */
    private fun extractDigits(text: String): String {
        return text.filter { it.isDigit() }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // METİN TEMİZLEME (PRE-PARSING)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Metni temizle (parse etmeden önce).
     * 
     * İşlemler:
     * - Boşlukları normalize et
     * - Non-breaking space'leri değiştir
     * - Trim
     * 
     * ⚠️ Double'a dönüştürme YAPILMAZ.
     */
    fun cleanText(text: String): String {
        return text
            // Non-breaking space → normal boşluk
            .replace('\u00A0', ' ')
            // Çoklu boşlukları tekile indir
            .replace(Regex("\\s+"), " ")
            // Baştan/sondan trim
            .trim()
    }

    /**
     * Sayı formatını normalize et (Türk formatı).
     * 
     * Örnek: "3.500.000 TL" → "3500000 TL"
     * 
     * ⚠️ Bu fonksiyon sadece görüntüleme için normalizasyon yapar.
     * ⚠️ Double'a dönüştürme YAPILMAZ.
     */
    fun normalizeNumberFormat(text: String): String {
        // Binlik ayırıcı noktaları kaldır (Türk formatı)
        // Ama ondalık virgülü koru
        var normalized = text
        
        // "3.500.000" → "3500000" (noktalar binlik ayırıcı)
        // Kural: Sayının içindeki noktalar kaldırılır
        val parts = text.split(" ")
        val processedParts = parts.map { part ->
            if (part.any { it.isDigit() }) {
                // Binlik ayırıcı noktaları kaldır
                part.replace(".", "")
            } else {
                part
            }
        }
        
        return processedParts.joinToString(" ")
    }
}
