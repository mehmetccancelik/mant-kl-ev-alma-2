package com.evanaliz.location

import kotlin.math.*

/**
 * Konum Skor Hesaplayıcı
 * 
 * Haversine formülü ile mesafe hesaplar ve yakınlığa göre skor üretir.
 */
object LocationScoreCalculator {
    
    // Dünya yarıçapı (km)
    private const val EARTH_RADIUS_KM = 6371.0
    
    // Skor eşikleri
    private const val TIER1_MAX_KM = 1.0   // 0-1 km = 100 puan
    private const val TIER2_MAX_KM = 2.0   // 1-2 km = 75 puan
    private const val TIER3_MAX_KM = 3.0   // 2-3 km = 50 puan
    
    /**
     * Haversine formülü ile iki koordinat arası mesafeyi hesaplar
     * @return Mesafe (km)
     */
    fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = sin(dLat / 2).pow(2) + 
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * 
                sin(dLon / 2).pow(2)
        
        val c = 2 * asin(sqrt(a))
        
        return EARTH_RADIUS_KM * c
    }
    
    /**
     * Mesafeye göre skor hesaplar
     * @param distanceKm Mesafe (km)
     * @return Skor (0-100)
     */
    fun calculateScore(distanceKm: Double): Int {
        return when {
            distanceKm <= TIER1_MAX_KM -> 100
            distanceKm <= TIER2_MAX_KM -> 75
            distanceKm <= TIER3_MAX_KM -> 50
            else -> 0
        }
    }
    
    /**
     * Bir konumun tüm metro duraklarına olan yakınlığını analiz eder
     * @return Yakın durakların listesi (3 km içindekiler)
     */
    fun analyzeProximity(
        propertyLat: Double,
        propertyLon: Double,
        metroDatabase: MetroDatabase
    ): List<ProximityResult> {
        val results = mutableListOf<ProximityResult>()
        
        for (line in metroDatabase.lines) {
            for (station in line.stations) {
                val distance = haversineDistance(
                    propertyLat, propertyLon,
                    station.latitude, station.longitude
                )
                
                val score = calculateScore(distance)
                
                // Sadece 3 km içindekileri ekle
                if (distance <= TIER3_MAX_KM) {
                    results.add(ProximityResult(
                        station = station,
                        line = line,
                        distanceKm = distance,
                        score = score
                    ))
                }
            }
        }
        
        // Mesafeye göre sırala (en yakından en uzağa)
        return results.sortedBy { it.distanceKm }
    }
    
    /**
     * Toplam konum skorunu hesaplar
     * En yakın 3 durağın ağırlıklı ortalaması
     */
    fun calculateTotalLocationScore(proximityResults: List<ProximityResult>): Int {
        if (proximityResults.isEmpty()) return 0
        
        // En yakın 3 durağı al
        val topResults = proximityResults.take(3)
        
        // Ağırlıklı ortalama (en yakın daha ağırlıklı)
        val weights = listOf(0.5, 0.3, 0.2)
        var weightedSum = 0.0
        var totalWeight = 0.0
        
        for ((index, result) in topResults.withIndex()) {
            val weight = weights.getOrElse(index) { 0.1 }
            weightedSum += result.score * weight
            totalWeight += weight
        }
        
        return if (totalWeight > 0) {
            (weightedSum / totalWeight).toInt()
        } else {
            0
        }
    }
    
    /**
     * Skor açıklaması üretir
     */
    fun getScoreDescription(score: Int): String {
        return when {
            score >= 90 -> "Mükemmel Konum 🌟"
            score >= 75 -> "Çok İyi Konum ✅"
            score >= 50 -> "İyi Konum 👍"
            score >= 25 -> "Orta Konum ⚠️"
            else -> "Uzak Konum ❌"
        }
    }
}
