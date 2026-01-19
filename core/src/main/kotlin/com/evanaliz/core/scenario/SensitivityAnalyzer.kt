package com.evanaliz.core.scenario

import com.evanaliz.core.FinancialConstants
import kotlin.math.pow

/**
 * Hassasiyet Analizi Motoru
 * 
 * Anahtar değişkenlerin değişimine göre karlılığın nasıl değiştiğini analiz eder.
 */
object SensitivityAnalyzer {

    /**
     * Hassasiyet analiz seviyesi (yüzde değişim)
     */
    private val SENSITIVITY_LEVELS = listOf(-0.20, -0.10, -0.05, 0.0, 0.05, 0.10, 0.20)

    /**
     * Tam hassasiyet analizi yap.
     */
    fun analyze(
        housePrice: Double,
        estimatedMonthlyRent: Double,
        baseScenario: InvestmentScenario = PredefinedScenarios.REALISTIC
    ): SensitivityAnalysisResult {
        
        // Baz durum hesapla
        val baseResult = ScenarioCalculationEngine.calculate(
            housePrice = housePrice,
            estimatedMonthlyRent = estimatedMonthlyRent,
            scenario = baseScenario
        )
        
        // Faiz hassasiyeti
        val interestSensitivity = analyzeInterestRateSensitivity(
            housePrice, estimatedMonthlyRent, baseScenario
        )
        
        // Fiyat hassasiyeti
        val priceSensitivity = analyzePriceSensitivity(
            housePrice, estimatedMonthlyRent, baseScenario
        )
        
        // Kira hassasiyeti
        val rentSensitivity = analyzeRentSensitivity(
            housePrice, estimatedMonthlyRent, baseScenario
        )
        
        // En etkili değişkeni bul
        val mostImpactfulVariable = findMostImpactfulVariable(
            interestSensitivity, priceSensitivity, rentSensitivity
        )
        
        // Break-even noktalarını bul
        val breakEvenPoints = calculateBreakEvenPoints(
            housePrice, estimatedMonthlyRent, baseScenario
        )
        
        return SensitivityAnalysisResult(
            baseResult = baseResult,
            interestRateSensitivity = interestSensitivity,
            priceSensitivity = priceSensitivity,
            rentSensitivity = rentSensitivity,
            mostImpactfulVariable = mostImpactfulVariable,
            breakEvenPoints = breakEvenPoints
        )
    }
    
    /**
     * Faiz oranı hassasiyeti
     */
    private fun analyzeInterestRateSensitivity(
        housePrice: Double,
        rent: Double,
        baseScenario: InvestmentScenario
    ): List<SensitivityPoint> {
        
        val points = mutableListOf<SensitivityPoint>()
        val baseRate = baseScenario.monthlyInterestRate
        
        // ±1%, ±3%, ±5% değişimler (aylık faize çevrilmiş)
        val changes = listOf(-0.05, -0.03, -0.01, 0.0, 0.01, 0.03, 0.05)
        
        for (change in changes) {
            val newRate = baseRate + (change / 12) // Yıllık değişimi aylığa çevir
            if (newRate <= 0) continue
            
            val modifiedScenario = baseScenario.copy(monthlyInterestRate = newRate)
            val result = ScenarioCalculationEngine.calculate(housePrice, rent, modifiedScenario)
            
            points.add(
                SensitivityPoint(
                    changePercent = change * 100,
                    amortizationYears = result.realTotalCost / 
                        (result.yearlyProjections.firstOrNull()?.netOperatingIncome ?: 1.0),
                    npv = result.riskMetrics.npv,
                    irr = result.riskMetrics.irr
                )
            )
        }
        
        return points
    }
    
    /**
     * Fiyat hassasiyeti
     */
    private fun analyzePriceSensitivity(
        basePrice: Double,
        rent: Double,
        scenario: InvestmentScenario
    ): List<SensitivityPoint> {
        
        return SENSITIVITY_LEVELS.map { change ->
            val newPrice = basePrice * (1 + change)
            val result = ScenarioCalculationEngine.calculate(newPrice, rent, scenario)
            
            SensitivityPoint(
                changePercent = change * 100,
                amortizationYears = result.realTotalCost / 
                    (result.yearlyProjections.firstOrNull()?.netOperatingIncome ?: 1.0),
                npv = result.riskMetrics.npv,
                irr = result.riskMetrics.irr
            )
        }
    }
    
    /**
     * Kira hassasiyeti
     */
    private fun analyzeRentSensitivity(
        price: Double,
        baseRent: Double,
        scenario: InvestmentScenario
    ): List<SensitivityPoint> {
        
        return SENSITIVITY_LEVELS.map { change ->
            val newRent = baseRent * (1 + change)
            val result = ScenarioCalculationEngine.calculate(price, newRent, scenario)
            
            SensitivityPoint(
                changePercent = change * 100,
                amortizationYears = result.realTotalCost / 
                    (result.yearlyProjections.firstOrNull()?.netOperatingIncome ?: 1.0),
                npv = result.riskMetrics.npv,
                irr = result.riskMetrics.irr
            )
        }
    }
    
    /**
     * En etkili değişkeni bul
     */
    private fun findMostImpactfulVariable(
        interest: List<SensitivityPoint>,
        price: List<SensitivityPoint>,
        rent: List<SensitivityPoint>
    ): String {
        
        fun calculateSpread(points: List<SensitivityPoint>): Double {
            val npvs = points.map { it.npv }
            return (npvs.maxOrNull() ?: 0.0) - (npvs.minOrNull() ?: 0.0)
        }
        
        val interestSpread = calculateSpread(interest)
        val priceSpread = calculateSpread(price)
        val rentSpread = calculateSpread(rent)
        
        return when {
            interestSpread >= priceSpread && interestSpread >= rentSpread -> "Faiz Oranı"
            priceSpread >= interestSpread && priceSpread >= rentSpread -> "Ev Fiyatı"
            else -> "Kira Geliri"
        }
    }
    
    /**
     * Break-even noktalarını hesapla
     */
    private fun calculateBreakEvenPoints(
        price: Double,
        rent: Double,
        scenario: InvestmentScenario
    ): BreakEvenPoints {
        
        // Kira için break-even: NPV = 0 olan minimum kira
        var breakEvenRent = rent
        for (multiplier in listOf(0.5, 0.6, 0.7, 0.8, 0.9, 1.0)) {
            val testRent = rent * multiplier
            val result = ScenarioCalculationEngine.calculate(price, testRent, scenario)
            if (result.riskMetrics.npv <= 0) {
                breakEvenRent = testRent * 1.1 // Biraz üstüne çık
                break
            }
        }
        
        // Fiyat için break-even: NPV = 0 olan maksimum fiyat
        var breakEvenPrice = price
        for (multiplier in listOf(1.5, 1.4, 1.3, 1.2, 1.1, 1.0)) {
            val testPrice = price * multiplier
            val result = ScenarioCalculationEngine.calculate(testPrice, rent, scenario)
            if (result.riskMetrics.npv >= 0) {
                breakEvenPrice = testPrice
                break
            }
        }
        
        return BreakEvenPoints(
            minimumRentForBreakEven = breakEvenRent,
            maximumPriceForBreakEven = breakEvenPrice,
            maxAcceptableInterestRate = scenario.monthlyInterestRate * 1.5 // Basit tahmin
        )
    }
}

/**
 * Hassasiyet Noktası
 */
data class SensitivityPoint(
    val changePercent: Double,
    val amortizationYears: Double,
    val npv: Double,
    val irr: Double
)

/**
 * Break-Even Noktaları
 */
data class BreakEvenPoints(
    val minimumRentForBreakEven: Double,
    val maximumPriceForBreakEven: Double,
    val maxAcceptableInterestRate: Double
)

/**
 * Hassasiyet Analizi Sonucu
 */
data class SensitivityAnalysisResult(
    val baseResult: ScenarioResult,
    val interestRateSensitivity: List<SensitivityPoint>,
    val priceSensitivity: List<SensitivityPoint>,
    val rentSensitivity: List<SensitivityPoint>,
    val mostImpactfulVariable: String,
    val breakEvenPoints: BreakEvenPoints
)
