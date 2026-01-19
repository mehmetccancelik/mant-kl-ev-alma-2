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
     * Koordinat Regex (DMS formatı)
     * Örn: 40°52'25.0"N 29°18'23.2"E
     */
    private val COORDINATE_REGEX = Regex("""\d+°\d+'[\d.]+"[NS]\s+\d+°\d+'[\d.]+"[EW]""")

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
        return COORDINATE_REGEX.containsMatchIn(text)
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
