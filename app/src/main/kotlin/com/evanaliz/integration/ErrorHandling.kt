package com.evanaliz.integration

/**
 * Hata Yönetim Stratejisi
 * 
 * Tüm uygulama genelinde hata yönetimi için merkezi kurallar.
 */
object ErrorHandling {

    /**
     * Hata Kategorisi
     */
    enum class ErrorCategory {
        /**
         * Kullanıcı düzeltebilir (yanlış ekran, eksik veri)
         */
        USER_RECOVERABLE,
        
        /**
         * Sistem hatası (izin yok, servis kapalı)
         */
        SYSTEM_ERROR,
        
        /**
         * Geçici hata (tekrar denenebilir)
         */
        TRANSIENT
    }

    /**
     * Uygulama Hatası
     */
    data class AppError(
        val category: ErrorCategory,
        val code: String,
        val userMessage: String,
        val technicalMessage: String,
        val isRetryable: Boolean
    )

    /**
     * Parse hatasını AppError'a çevir
     */
    fun toAppError(parseError: ParseError): AppError {
        return when (parseError) {
            is ParseError.NoDataFound -> AppError(
                category = ErrorCategory.USER_RECOVERABLE,
                code = "E001",
                userMessage = "Ekranda fiyat bilgisi bulunamadı.\n\n" +
                    "Lütfen bir emlak ilanı sayfasında olduğunuzdan emin olun.",
                technicalMessage = "No numeric data found in accessibility tree",
                isRetryable = true
            )
            
            is ParseError.InsufficientData -> AppError(
                category = ErrorCategory.USER_RECOVERABLE,
                code = "E002",
                userMessage = "Hem ev fiyatı hem de kira bilgisi gerekli.\n\n" +
                    "Sadece birini bulabildik. Lütfen ilan detaylarını kontrol edin.",
                technicalMessage = "Only partial data found (price or rent missing)",
                isRetryable = true
            )
            
            is ParseError.InvalidFormat -> AppError(
                category = ErrorCategory.USER_RECOVERABLE,
                code = "E003",
                userMessage = "Bulunan değerler sayıya çevrilemedi.\n\n" +
                    "Lütfen fiyatların ekranda görünür olduğundan emin olun.",
                technicalMessage = "Text to number parsing failed",
                isRetryable = true
            )
            
            is ParseError.UnexpectedError -> AppError(
                category = ErrorCategory.SYSTEM_ERROR,
                code = "E999",
                userMessage = "Beklenmeyen bir hata oluştu.\n\n" +
                    "Lütfen uygulamayı yeniden başlatın.",
                technicalMessage = parseError.message,
                isRetryable = false
            )
        }
    }

    /**
     * Accessibility servis hatası
     */
    fun accessibilityServiceNotRunning(): AppError = AppError(
        category = ErrorCategory.SYSTEM_ERROR,
        code = "E100",
        userMessage = "Erişilebilirlik servisi çalışmıyor.\n\n" +
            "Lütfen Ayarlar > Erişilebilirlik bölümünden servisi aktifleştirin.",
        technicalMessage = "AccessibilityService not running",
        isRetryable = false
    )

    /**
     * Overlay izni hatası
     */
    fun overlayPermissionMissing(): AppError = AppError(
        category = ErrorCategory.SYSTEM_ERROR,
        code = "E101",
        userMessage = "Ekran üzeri izni gerekli.\n\n" +
            "Lütfen uygulamaya 'Diğer uygulamaların üzerinde göster' izni verin.",
        technicalMessage = "SYSTEM_ALERT_WINDOW permission not granted",
        isRetryable = false
    )

    /**
     * Hesaplama hatası
     */
    fun calculationFailed(message: String): AppError = AppError(
        category = ErrorCategory.SYSTEM_ERROR,
        code = "E200",
        userMessage = "Hesaplama yapılırken bir hata oluştu.\n\n" +
            "Lütfen girilen değerleri kontrol edin.",
        technicalMessage = "Calculation engine error: $message",
        isRetryable = true
    )
}
