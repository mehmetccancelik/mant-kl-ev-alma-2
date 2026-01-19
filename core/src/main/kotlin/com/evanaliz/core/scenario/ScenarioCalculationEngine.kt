package com.evanaliz.core.scenario

import com.evanaliz.core.FinancialConstants
import kotlin.math.pow

/**
 * Senaryo Bazlı Hesaplama Motoru
 * 
 * Farklı senaryolar altında yatırım analizini gerçekleştirir.
 */
object ScenarioCalculationEngine {

    /**
     * Senaryo bazlı hesaplama yap.
     * 
     * @param housePrice Ev fiyatı
     * @param estimatedMonthlyRent Başlangıç aylık kira
     * @param scenario Uygulanacak senaryo
     * @param projectionYears Projeksiyon süresi (yıl)
     * @return Senaryo bazlı hesaplama sonucu
     */
    fun calculate(
        housePrice: Double,
        estimatedMonthlyRent: Double,
        scenario: InvestmentScenario,
        projectionYears: Int = 10
    ): ScenarioResult {
        
        // ═══════════════════════════════════════════════════════════════════════
        // TEMEL HESAPLAMALAR
        // ═══════════════════════════════════════════════════════════════════════
        
        val purchaseExpenses = housePrice * FinancialConstants.PURCHASE_EXPENSE_RATE
        val loanAmount = housePrice * FinancialConstants.LOAN_USAGE_RATIO
        val downPayment = housePrice - loanAmount
        
        // PMT (senaryo faizi ile)
        val r = scenario.monthlyInterestRate
        val n = FinancialConstants.LOAN_TERM_MONTHS
        val compoundFactor = (1 + r).pow(n)
        val monthlyInstallment = loanAmount * (r * compoundFactor) / (compoundFactor - 1)
        val totalLoanRepayment = monthlyInstallment * n
        
        // Gerçek toplam maliyet
        val realTotalCost = downPayment + totalLoanRepayment + purchaseExpenses
        
        // ═══════════════════════════════════════════════════════════════════════
        // YILLIK PROJEKSİYONLAR
        // ═══════════════════════════════════════════════════════════════════════
        
        val yearlyProjections = mutableListOf<YearlyProjection>()
        var cumulativeCashFlow = -downPayment - purchaseExpenses // İlk yatırım
        var currentPropertyValue = housePrice
        var currentMonthlyRent = estimatedMonthlyRent
        
        for (year in 1..projectionYears) {
            // Kira artışı
            if (year > 1) {
                currentMonthlyRent *= (1 + scenario.annualRentGrowth)
            }
            
            // Brüt yıllık kira
            val grossAnnualRent = currentMonthlyRent * 12
            
            // Boşluk kaybı
            val vacancyLoss = grossAnnualRent * scenario.vacancyRate
            
            // Bakım giderleri
            val maintenanceCost = grossAnnualRent * scenario.maintenanceCostRate
            
            // Net işletme geliri
            val netOperatingIncome = grossAnnualRent - vacancyLoss - maintenanceCost
            
            // Vergi (istisna sonrası)
            val taxableIncome = grossAnnualRent - FinancialConstants.ANNUAL_RENT_TAX_EXEMPTION
            val annualTax = if (taxableIncome > 0) {
                taxableIncome * FinancialConstants.INCOME_TAX_RATE
            } else 0.0
            
            // Kredi taksiti (ilk 5 yıl)
            val annualLoanPayment = if (year <= 5) monthlyInstallment * 12 else 0.0
            
            // Net nakit akışı
            val netCashFlow = netOperatingIncome - annualTax - annualLoanPayment
            cumulativeCashFlow += netCashFlow
            
            // Gayrimenkul değer artışı
            currentPropertyValue *= (1 + scenario.annualPropertyAppreciation)
            
            yearlyProjections.add(
                YearlyProjection(
                    year = year,
                    grossRent = grossAnnualRent,
                    vacancyLoss = vacancyLoss,
                    maintenanceCost = maintenanceCost,
                    netOperatingIncome = netOperatingIncome,
                    annualTax = annualTax,
                    loanPayment = annualLoanPayment,
                    netCashFlow = netCashFlow,
                    cumulativeCashFlow = cumulativeCashFlow,
                    propertyValue = currentPropertyValue
                )
            )
        }
        
        // ═══════════════════════════════════════════════════════════════════════
        // RİSK METRİKLERİ
        // ═══════════════════════════════════════════════════════════════════════
        
        val riskMetrics = calculateRiskMetrics(
            initialInvestment = downPayment + purchaseExpenses,
            yearlyProjections = yearlyProjections,
            finalPropertyValue = currentPropertyValue,
            discountRate = scenario.annualInflationRate
        )
        
        return ScenarioResult(
            scenario = scenario,
            monthlyInstallment = monthlyInstallment,
            totalLoanRepayment = totalLoanRepayment,
            realTotalCost = realTotalCost,
            yearlyProjections = yearlyProjections,
            riskMetrics = riskMetrics,
            finalPropertyValue = currentPropertyValue
        )
    }
    
    /**
     * Risk metriklerini hesapla.
     */
    private fun calculateRiskMetrics(
        initialInvestment: Double,
        yearlyProjections: List<YearlyProjection>,
        finalPropertyValue: Double,
        discountRate: Double
    ): RiskMetrics {
        
        // Geri ödeme süresi (payback period)
        var paybackYears = 0.0
        for (projection in yearlyProjections) {
            if (projection.cumulativeCashFlow >= 0) {
                paybackYears = projection.year.toDouble()
                break
            }
        }
        if (paybackYears == 0.0) {
            // Projeksiyon süresi içinde geri ödenmedi
            paybackYears = Double.MAX_VALUE
        }
        
        // NPV (Net Present Value)
        var npv = -initialInvestment
        for ((index, projection) in yearlyProjections.withIndex()) {
            val discountFactor = (1 + discountRate).pow(index + 1)
            npv += projection.netCashFlow / discountFactor
        }
        // Son yılda satış geliri ekle
        val finalYearDiscountFactor = (1 + discountRate).pow(yearlyProjections.size)
        npv += finalPropertyValue / finalYearDiscountFactor
        
        // IRR (basitleştirilmiş tahmin)
        val totalReturn = yearlyProjections.sumOf { it.netCashFlow } + finalPropertyValue
        val irr = ((totalReturn / initialInvestment).pow(1.0 / yearlyProjections.size)) - 1
        
        // Cash-on-cash return (ilk yıl)
        val firstYearCashFlow = yearlyProjections.firstOrNull()?.netCashFlow ?: 0.0
        val cashOnCashReturn = firstYearCashFlow / initialInvestment
        
        // Worst-case drawdown
        val worstDrawdown = yearlyProjections.minOfOrNull { it.cumulativeCashFlow } ?: 0.0
        
        return RiskMetrics(
            paybackPeriodYears = paybackYears,
            npv = npv,
            irr = irr,
            cashOnCashReturn = cashOnCashReturn,
            worstCaseDrawdown = worstDrawdown
        )
    }
}

/**
 * Yıllık Projeksiyon
 */
data class YearlyProjection(
    val year: Int,
    val grossRent: Double,
    val vacancyLoss: Double,
    val maintenanceCost: Double,
    val netOperatingIncome: Double,
    val annualTax: Double,
    val loanPayment: Double,
    val netCashFlow: Double,
    val cumulativeCashFlow: Double,
    val propertyValue: Double
)

/**
 * Senaryo Sonucu
 */
data class ScenarioResult(
    val scenario: InvestmentScenario,
    val monthlyInstallment: Double,
    val totalLoanRepayment: Double,
    val realTotalCost: Double,
    val yearlyProjections: List<YearlyProjection>,
    val riskMetrics: RiskMetrics,
    val finalPropertyValue: Double
)
