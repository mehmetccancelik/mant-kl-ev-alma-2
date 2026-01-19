package com.evanaliz.location

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

/**
 * Metro Hattı Veri Modeli
 */
data class MetroLine(
    @SerializedName("proje_adi") val name: String,
    @SerializedName("proje_durumu") val status: String,  // "Insaat", "Planlanan", "Aktif"
    @SerializedName("stations") val stations: List<MetroStation>,
    @SerializedName("etki_mesafesi_km") val impactRadiusKm: Double = 2.0
)

/**
 * Metro Durağı Veri Modeli
 */
data class MetroStation(
    @SerializedName("ad") val name: String,
    @SerializedName("lat") val latitude: Double,
    @SerializedName("lon") val longitude: Double,
    @SerializedName("sira") val order: Int = 0
)

/**
 * Metro Veritabanı
 */
data class MetroDatabase(
    @SerializedName("projects") val lines: List<MetroLine>
)

/**
 * Yakınlık Sonucu
 */
data class ProximityResult(
    val station: MetroStation,
    val line: MetroLine,
    val distanceKm: Double,
    val score: Int
)

/**
 * Metro Veri Yükleyici
 */
object MetroDataLoader {
    
    private var cachedDatabase: MetroDatabase? = null
    
    /**
     * assets/metro_lines.json dosyasından metro verilerini yükler
     */
    fun loadMetroData(context: Context): MetroDatabase {
        cachedDatabase?.let { return it }
        
        return try {
            val jsonString = context.assets
                .open("metro_lines.json")
                .bufferedReader()
                .use { it.readText() }
            
            val database = Gson().fromJson(jsonString, MetroDatabase::class.java)
            cachedDatabase = database
            database
        } catch (e: Exception) {
            e.printStackTrace()
            // Dosya yoksa veya hata varsa boş veritabanı döndür
            MetroDatabase(emptyList())
        }
    }
}
