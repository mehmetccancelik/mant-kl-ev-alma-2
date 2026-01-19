package com.evanaliz.core.verdict

/**
 * Renk İpucu
 * 
 * UI katmanına renk önerisi sağlayan enum.
 * Gerçek renk değerlerini içermez, sadece semantik ipucu verir.
 * 
 * UI katmanı bu ipucunu kendi tasarım sistemine göre yorumlar.
 */
enum class ColorHint {
    
    /**
     * YEŞİL - Olumlu sonuç
     * Yatırım mantıklı, kullanıcıyı teşvik edici.
     */
    GREEN,
    
    /**
     * KIRMIZI - Olumsuz sonuç
     * Yatırım mantıksız, kullanıcıyı uyarıcı.
     */
    RED
}
