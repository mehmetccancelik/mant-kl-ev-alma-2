package com.evanaliz.core.scenario

/**
 * Yatırım Kararı
 */
enum class InvestmentDecision {
    /**
     * Güçlü Al - Tüm senaryolarda karlı
     */
    STRONG_BUY,
    
    /**
     * Koşullu Al - Bazı riskler var ama genel olarak olumlu
     */
    CONDITIONAL_BUY,
    
    /**
     * Nötr / Bekle - Belirsizlik yüksek
     */
    NEUTRAL_WAIT,
    
    /**
     * Yüksek Risk - Kaçın
     */
    HIGH_RISK_AVOID
}

/**
 * Karar Desteği Motoru
 * 
 * Tüm senaryo sonuçlarını değerlendirerek yatırım kararı üretir.
 */
object DecisionSupportEngine {

    /**
     * Yatırım kararı üret.
     */
    fun generateDecision(
        housePrice: Double,
        estimatedMonthlyRent: Double
    ): InvestmentDecisionReport {
        
        // Tüm senaryoları hesapla
        val scenarioResults = PredefinedScenarios.ALL.map { scenario ->
            ScenarioCalculationEngine.calculate(
                housePrice = housePrice,
                estimatedMonthlyRent = estimatedMonthlyRent,
                scenario = scenario
            )
        }
        
        // Hassasiyet analizi
        val sensitivityAnalysis = SensitivityAnalyzer.analyze(
            housePrice = housePrice,
            estimatedMonthlyRent = estimatedMonthlyRent
        )
        
        // Karar ver
        val decision = evaluateDecision(scenarioResults)
        
        // Gerekçeler
        val reasons = generateReasons(scenarioResults, sensitivityAnalysis)
        
        // Risk açıklaması
        val riskExplanation = generateRiskExplanation(scenarioResults, sensitivityAnalysis)
        
        return InvestmentDecisionReport(
            decision = decision,
            scenarioResults = scenarioResults,
            sensitivityAnalysis = sensitivityAnalysis,
            reasons = reasons,
            riskExplanation = riskExplanation
        )
    }
    
    /**
     * Kararı değerlendir
     */
    private fun evaluateDecision(results: List<ScenarioResult>): InvestmentDecision {
        
        val positiveNpvCount = results.count { it.riskMetrics.isNpvPositive }
        val paybackAchievedCount = results.count { it.riskMetrics.isPaybackAchieved }
        
        val optimistic = results.find { it.scenario.type == ScenarioType.OPTIMISTIC }
        val realistic = results.find { it.scenario.type == ScenarioType.REALISTIC }
        val pessimistic = results.find { it.scenario.type == ScenarioType.PESSIMISTIC }
        val stress = results.find { it.scenario.type == ScenarioType.EXTREME_STRESS }
        
        return when {
            // Tüm senaryolarda NPV pozitif ve hatta stres senaryosunda bile karlı
            positiveNpvCount == 4 && stress?.riskMetrics?.isNpvPositive == true -> {
                InvestmentDecision.STRONG_BUY
            }
            
            // Gerçekçi ve iyimser senaryoda karlı, stres'te zarar sınırlı
            realistic?.riskMetrics?.isNpvPositive == true && 
            optimistic?.riskMetrics?.isNpvPositive == true &&
            (stress?.riskMetrics?.npv ?: 0.0) > -500_000 -> {
                InvestmentDecision.CONDITIONAL_BUY
            }
            
            // Sadece gerçekçi senaryoda karlı
            realistic?.riskMetrics?.isNpvPositive == true -> {
                InvestmentDecision.NEUTRAL_WAIT
            }
            
            // Diğer durumlar = Yüksek risk
            else -> {
                InvestmentDecision.HIGH_RISK_AVOID
            }
        }
    }
    
    /**
     * Karar gerekçelerini oluştur
     */
    private fun generateReasons(
        results: List<ScenarioResult>,
        sensitivity: SensitivityAnalysisResult
    ): List<String> {
        
        val reasons = mutableListOf<String>()
        
        val realistic = results.find { it.scenario.type == ScenarioType.REALISTIC }
        val stress = results.find { it.scenario.type == ScenarioType.EXTREME_STRESS }
        
        if (realistic != null) {
            if (realistic.riskMetrics.isNpvPositive) {
                reasons.add("✅ Gerçekçi senaryoda NPV pozitif: ${formatCurrency(realistic.riskMetrics.npv)}")
            } else {
                reasons.add("❌ Gerçekçi senaryoda NPV negatif: ${formatCurrency(realistic.riskMetrics.npv)}")
            }
            
            if (realistic.riskMetrics.paybackPeriodYears < 15) {
                reasons.add("✅ Geri ödeme süresi makul: %.1f yıl".format(realistic.riskMetrics.paybackPeriodYears))
            } else {
                reasons.add("⚠️ Geri ödeme süresi uzun: %.1f yıl".format(realistic.riskMetrics.paybackPeriodYears))
            }
        }
        
        if (stress != null) {
            if (stress.riskMetrics.isNpvPositive) {
                reasons.add("✅ Stres senaryosunda bile karlı")
            } else {
                reasons.add("⚠️ Stres senaryosunda zarar: ${formatCurrency(stress.riskMetrics.npv)}")
            }
        }
        
        reasons.add("📊 En etkili değişken: ${sensitivity.mostImpactfulVariable}")
        
        return reasons
    }
    
    /**
     * Risk açıklaması oluştur (basit dil)
     */
    private fun generateRiskExplanation(
        results: List<ScenarioResult>,
        sensitivity: SensitivityAnalysisResult
    ): RiskExplanation {
        
        val stress = results.find { it.scenario.type == ScenarioType.EXTREME_STRESS }
        val realistic = results.find { it.scenario.type == ScenarioType.REALISTIC }
        
        val biggestRisk = when (sensitivity.mostImpactfulVariable) {
            "Faiz Oranı" -> "Faiz oranlarının artması bu yatırımı en çok etkileyen risk. " +
                "Eğer kredi faizleri yükselirse, aylık taksitiniz artar ve karlılık düşer."
            "Ev Fiyatı" -> "Ev fiyatının değişmesi bu yatırımı en çok etkileyen faktör. " +
                "Eğer daha yüksek fiyata alırsanız, amorti süresi uzar."
            else -> "Kira geliri bu yatırımın en kritik değişkeni. " +
                "Eğer beklenen kirayı alamazsanız, yatırım zarar edebilir."
        }
        
        val successCondition = """
            Bu yatırımın başarılı olması için:
            • Kira gelirinin en az ${formatCurrency(sensitivity.breakEvenPoints.minimumRentForBreakEven)} olması
            • Ev fiyatının ${formatCurrency(sensitivity.breakEvenPoints.maximumPriceForBreakEven)} üzerine çıkmaması
            • Kiracı bulma süresinin kısa tutulması
            gerekir.
        """.trimIndent()
        
        val failureCondition = if (stress?.riskMetrics?.isNpvPositive == false) {
            """
                Bu yatırım şu durumlarda başarısız olur:
                • Ekonomik kriz ve çok yüksek faiz ortamında
                • Kiracı bulunamazsa veya kira ödemeleri aksarsa
                • Beklenmeyen büyük bakım giderleri çıkarsa
            """.trimIndent()
        } else {
            "Bu yatırım oldukça dayanıklı görünüyor, ancak aşırı senaryolarda bile dikkatli olunmalı."
        }
        
        return RiskExplanation(
            biggestRisk = biggestRisk,
            successCondition = successCondition,
            failureCondition = failureCondition
        )
    }
    
    private fun formatCurrency(value: Double): String {
        return "%,.0f TL".format(value)
    }
}

/**
 * Risk Açıklaması (Basit Dil)
 */
data class RiskExplanation(
    val biggestRisk: String,
    val successCondition: String,
    val failureCondition: String
)

/**
 * Yatırım Karar Raporu
 */
data class InvestmentDecisionReport(
    val decision: InvestmentDecision,
    val scenarioResults: List<ScenarioResult>,
    val sensitivityAnalysis: SensitivityAnalysisResult,
    val reasons: List<String>,
    val riskExplanation: RiskExplanation
) {
    /**
     * Karar metni
     */
    val decisionText: String
        get() = when (decision) {
            InvestmentDecision.STRONG_BUY -> "💪 GÜÇLÜ AL - Tüm senaryolarda karlı"
            InvestmentDecision.CONDITIONAL_BUY -> "✅ KOŞULLU AL - Riskler var ama genel olumlu"
            InvestmentDecision.NEUTRAL_WAIT -> "⏸️ NÖTR / BEKLE - Belirsizlik yüksek"
            InvestmentDecision.HIGH_RISK_AVOID -> "🚫 YÜKSEK RİSK - KAÇIN"
        }
}
