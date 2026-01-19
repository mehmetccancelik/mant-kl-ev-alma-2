package com.evanaliz.core.validation

import com.evanaliz.core.InvestmentCalculationEngine
import kotlin.math.abs

/**
 * Excel Uyumluluk Doğrulama Motoru
 * 
 * Kotlin hesaplama motorunu Excel referans değerleri ile karşılaştırır.
 * 
 * ╔════════════════════════════════════════════════════════════════════════════╗
 * ║ DOĞRULAMA PRENSİPLERİ                                                      ║
 * ╠════════════════════════════════════════════════════════════════════════════╣
 * ║ 1. Excel TEK KAYNAK - Excel sonucu doğru kabul edilir                      ║
 * ║ 2. Tolerans bazlı - Küçük yuvarlama farkları kabul edilir                  ║
 * ║ 3. Tüm alanlar test edilir - Sadece final değil, ara değerler de           ║
 * ║ 4. Şeffaf raporlama - Her fark açıkça görülür                              ║
 * ╚════════════════════════════════════════════════════════════════════════════╝
 */
object ExcelParityValidator {

    /**
     * Tek bir test senaryosunu doğrula.
     * 
     * @param testCase Excel'den alınan referans test senaryosu.
     * @return Senaryo doğrulama sonucu.
     */
    fun validateScenario(testCase: ExcelTestCase): ScenarioValidationResult {
        
        // Kotlin motorunu çalıştır
        val kotlinResult = InvestmentCalculationEngine.calculate(
            housePrice = testCase.inputHousePrice,
            estimatedMonthlyRent = testCase.inputMonthlyRent
        )
        
        // Her alanı karşılaştır
        val fieldResults = mutableListOf<FieldValidationResult>()
        
        // ═══════════════════════════════════════════════════════════════════════
        // ALAN KARŞILAŞTIRMALARI
        // ═══════════════════════════════════════════════════════════════════════
        
        fieldResults.add(
            compareField("purchaseExpenses", 
                testCase.expectedPurchaseExpenses, 
                kotlinResult.purchaseExpenses)
        )
        
        fieldResults.add(
            compareField("loanAmount", 
                testCase.expectedLoanAmount, 
                kotlinResult.loanAmount)
        )
        
        fieldResults.add(
            compareField("downPayment", 
                testCase.expectedDownPayment, 
                kotlinResult.downPayment)
        )
        
        fieldResults.add(
            compareField("monthlyInstallment ⭐", 
                testCase.expectedMonthlyInstallment, 
                kotlinResult.monthlyInstallment)
        )
        
        fieldResults.add(
            compareField("totalLoanRepayment", 
                testCase.expectedTotalLoanRepayment, 
                kotlinResult.totalLoanRepayment)
        )
        
        fieldResults.add(
            compareField("realTotalCost ⭐", 
                testCase.expectedRealTotalCost, 
                kotlinResult.realTotalCost)
        )
        
        fieldResults.add(
            compareField("grossAnnualRent", 
                testCase.expectedGrossAnnualRent, 
                kotlinResult.grossAnnualRent)
        )
        
        fieldResults.add(
            compareField("annualTax", 
                testCase.expectedAnnualTax, 
                kotlinResult.annualTax)
        )
        
        fieldResults.add(
            compareField("netAnnualRent", 
                testCase.expectedNetAnnualRent, 
                kotlinResult.netAnnualRent)
        )
        
        fieldResults.add(
            compareField("amortizationYears ⭐", 
                testCase.expectedAmortizationYears, 
                kotlinResult.amortizationYears)
        )
        
        // Genel sonuç
        val overallPassed = fieldResults.all { it.passed }
        
        return ScenarioValidationResult(
            scenarioName = testCase.scenarioName,
            description = testCase.description,
            fieldResults = fieldResults,
            overallPassed = overallPassed
        )
    }
    
    /**
     * Tüm test senaryolarını doğrula ve tam rapor üret.
     * 
     * @param testCases Tüm Excel test senaryoları.
     * @return Tam doğrulama raporu.
     */
    fun validateAll(testCases: List<ExcelTestCase>): FullValidationReport {
        
        val scenarioResults = testCases.map { validateScenario(it) }
        val overallPassed = scenarioResults.all { it.overallPassed }
        
        return FullValidationReport(
            scenarioResults = scenarioResults,
            overallPassed = overallPassed,
            validationTimestamp = java.time.LocalDateTime.now().toString()
        )
    }
    
    /**
     * Tek bir alanı karşılaştır.
     */
    private fun compareField(
        fieldName: String,
        excelValue: Double,
        kotlinValue: Double
    ): FieldValidationResult {
        
        val absoluteDifference = abs(excelValue - kotlinValue)
        
        // Tolerans kontrolü - mutlak veya yüzde bazlı
        val passedAbsolute = absoluteDifference <= ValidationConstants.MAX_ALLOWED_DIFFERENCE
        val passedPercentage = if (excelValue != 0.0) {
            (absoluteDifference / abs(excelValue)) <= ValidationConstants.MAX_ALLOWED_PERCENTAGE_DIFFERENCE
        } else {
            absoluteDifference == 0.0
        }
        
        // Her iki toleranstan biri sağlanırsa PASS
        val passed = passedAbsolute || passedPercentage
        
        return FieldValidationResult(
            fieldName = fieldName,
            excelValue = excelValue,
            kotlinValue = kotlinValue,
            absoluteDifference = absoluteDifference,
            passed = passed
        )
    }
    
    /**
     * Doğrulama raporunu konsola yazdır.
     */
    fun printReport(report: FullValidationReport) {
        println("═══════════════════════════════════════════════════════════════════════════")
        println("              EXCEL UYUMLULUK DOĞRULAMA RAPORU                             ")
        println("═══════════════════════════════════════════════════════════════════════════")
        println("Tarih: ${report.validationTimestamp}")
        println("Toplam Senaryo: ${report.totalScenarioCount}")
        println("Başarılı: ${report.passedScenarioCount}")
        println()
        
        for (scenario in report.scenarioResults) {
            println("───────────────────────────────────────────────────────────────────────────")
            println("📋 ${scenario.scenarioName}")
            println("   ${scenario.description}")
            println("   Sonuç: ${if (scenario.overallPassed) "✅ PASS" else "❌ FAIL"}")
            println()
            
            println("   %-25s %15s %15s %12s %8s".format(
                "Alan", "Excel", "Kotlin", "Fark", "Durum"
            ))
            println("   " + "-".repeat(75))
            
            for (field in scenario.fieldResults) {
                println("   %-25s %15.2f %15.2f %12.4f %8s".format(
                    field.fieldName,
                    field.excelValue,
                    field.kotlinValue,
                    field.absoluteDifference,
                    field.statusSymbol
                ))
            }
            println()
        }
        
        println("═══════════════════════════════════════════════════════════════════════════")
        println("GENEL SONUÇ: ${if (report.overallPassed) "✅ TÜM TESTLER GEÇTİ" else "❌ BAŞARISIZ TESTLER VAR"}")
        println("═══════════════════════════════════════════════════════════════════════════")
    }
}
