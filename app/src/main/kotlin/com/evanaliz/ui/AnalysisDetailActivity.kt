package com.evanaliz.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.evanaliz.R
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow

/**
 * Detaylı Analiz Ekranı
 * 
 * Excel modelindeki TÜM verileri gösterir ve real-time hesaplama yapar.
 * Excel formülleri:
 * - Aylık Taksit: =MUTLAK(DEVRESEL_ÖDEME(B7,B8,B5)) yani ABS(PMT(rate, term, loan))
 * - Toplam Geri Ödeme: =B9*B8
 * - Toplam Maliyet: =B10+B6+B3
 * - Adil Fiyat: =B16*B19-B3-(B10-B5)
 */
class AnalysisDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_HOUSE_PRICE = "house_price"
        const val EXTRA_MONTHLY_RENT = "monthly_rent"
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEMEL GİRİŞLER
    // ═══════════════════════════════════════════════════════════════════════════
    private lateinit var inputHousePrice: EditText
    private lateinit var inputExpenseRate: EditText
    private lateinit var inputLoanRate: EditText
    private lateinit var inputInterestRate: EditText
    private lateinit var inputLoanTerm: EditText
    private lateinit var inputMonthlyRent: EditText
    private lateinit var inputRentExemption: EditText
    private lateinit var inputTargetAmort: EditText

    // ═══════════════════════════════════════════════════════════════════════════
    // SONUÇLAR
    // ═══════════════════════════════════════════════════════════════════════════
    private lateinit var resultExpense: TextView
    private lateinit var resultLoanAmount: TextView
    private lateinit var resultDownPayment: TextView
    private lateinit var resultMonthlyInstallment: TextView
    private lateinit var resultTotalRepayment: TextView
    private lateinit var resultTotalCost: TextView
    private lateinit var resultAnnualRent: TextView
    private lateinit var resultTaxableRent: TextView
    private lateinit var resultNetAnnualRent: TextView

    // Değerlendirme (interaktif)
    private lateinit var inputRequiredInstallment: EditText
    private lateinit var inputRequiredInterest: EditText
    private lateinit var inputRequiredLoanRate: EditText
    private lateinit var resultFairPrice: TextView

    // Özet
    private lateinit var summaryTotalCreditCost: TextView
    private lateinit var summaryNoCreditCost: TextView
    private lateinit var summaryRentSupport: TextView

    // ═══════════════════════════════════════════════════════════════════════════
    // ENFLASYON TAHMİNLERİ
    // ═══════════════════════════════════════════════════════════════════════════
    private lateinit var infl2026: EditText
    private lateinit var infl2027: EditText
    private lateinit var infl2028: EditText
    private lateinit var infl2029: EditText
    private lateinit var infl2030: EditText
    private lateinit var infl2031: EditText

    // ═══════════════════════════════════════════════════════════════════════════
    // KONUT ENFLASYON TAHMİNLERİ
    // ═══════════════════════════════════════════════════════════════════════════
    private lateinit var housingRate2026: EditText
    private lateinit var housingRate2027: EditText
    private lateinit var housingRate2028: EditText
    private lateinit var housingRate2029: EditText
    private lateinit var housingRate2030: EditText
    private lateinit var housingRate2031: EditText

    private lateinit var housingVal2025: TextView
    private lateinit var housingVal2026: TextView
    private lateinit var housingVal2027: TextView
    private lateinit var housingVal2028: TextView
    private lateinit var housingVal2029: TextView
    private lateinit var housingVal2030: TextView
    private lateinit var housingVal2031: TextView

    // Hangi modda güncelleme yapılıyor (sonsuz döngüyü önlemek için)
    private var isUpdating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis_detail)

        initViews()
        loadInitialData()
        setupListeners()
        calculateAll()
    }

    private fun initViews() {
        // Temel Girişler
        inputHousePrice = findViewById(R.id.input_house_price)
        inputExpenseRate = findViewById(R.id.input_expense_rate)
        inputLoanRate = findViewById(R.id.input_loan_rate)
        inputInterestRate = findViewById(R.id.input_interest_rate)
        inputLoanTerm = findViewById(R.id.input_loan_term)
        inputMonthlyRent = findViewById(R.id.input_monthly_rent)
        inputRentExemption = findViewById(R.id.input_rent_exemption)
        inputTargetAmort = findViewById(R.id.input_target_amort)

        // Sonuçlar
        resultExpense = findViewById(R.id.result_expense)
        resultLoanAmount = findViewById(R.id.result_loan_amount)
        resultDownPayment = findViewById(R.id.result_down_payment)
        resultMonthlyInstallment = findViewById(R.id.result_monthly_installment)
        resultTotalRepayment = findViewById(R.id.result_total_repayment)
        resultTotalCost = findViewById(R.id.result_total_cost)
        resultAnnualRent = findViewById(R.id.result_annual_rent)
        resultTaxableRent = findViewById(R.id.result_taxable_rent)
        resultNetAnnualRent = findViewById(R.id.result_net_annual_rent)

        // Değerlendirme (interaktif)
        inputRequiredInstallment = findViewById(R.id.input_required_installment)
        inputRequiredInterest = findViewById(R.id.input_required_interest)
        inputRequiredLoanRate = findViewById(R.id.input_required_loan_rate)
        resultFairPrice = findViewById(R.id.result_fair_price)

        // Özet
        summaryTotalCreditCost = findViewById(R.id.summary_total_credit_cost)
        summaryNoCreditCost = findViewById(R.id.summary_no_credit_cost)
        summaryRentSupport = findViewById(R.id.summary_rent_support)

        // Enflasyon Tahminleri
        infl2026 = findViewById(R.id.infl_2026)
        infl2027 = findViewById(R.id.infl_2027)
        infl2028 = findViewById(R.id.infl_2028)
        infl2029 = findViewById(R.id.infl_2029)
        infl2030 = findViewById(R.id.infl_2030)
        infl2031 = findViewById(R.id.infl_2031)

        // Konut Enflasyon Tahminleri
        housingRate2026 = findViewById(R.id.housing_rate_2026)
        housingRate2027 = findViewById(R.id.housing_rate_2027)
        housingRate2028 = findViewById(R.id.housing_rate_2028)
        housingRate2029 = findViewById(R.id.housing_rate_2029)
        housingRate2030 = findViewById(R.id.housing_rate_2030)
        housingRate2031 = findViewById(R.id.housing_rate_2031)

        housingVal2025 = findViewById(R.id.housing_val_2025)
        housingVal2026 = findViewById(R.id.housing_val_2026)
        housingVal2027 = findViewById(R.id.housing_val_2027)
        housingVal2028 = findViewById(R.id.housing_val_2028)
        housingVal2029 = findViewById(R.id.housing_val_2029)
        housingVal2030 = findViewById(R.id.housing_val_2030)
        housingVal2031 = findViewById(R.id.housing_val_2031)

        // Kapat butonu
        findViewById<Button>(R.id.btn_close).setOnClickListener {
            finish()
        }
    }

    private fun loadInitialData() {
        val housePrice = intent.getDoubleExtra(EXTRA_HOUSE_PRICE, 0.0)
        val monthlyRent = intent.getDoubleExtra(EXTRA_MONTHLY_RENT, 0.0)

        if (housePrice > 0) {
            inputHousePrice.setText(formatWholeNumber(housePrice))
        }
        if (monthlyRent > 0) {
            inputMonthlyRent.setText(formatWholeNumber(monthlyRent))
        }
    }

    private fun setupListeners() {
        val mainTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!isUpdating) calculateAll()
            }
        }

        // Ana input alanlarına listener ekle
        listOf(
            inputHousePrice, inputExpenseRate, inputLoanRate, inputInterestRate,
            inputLoanTerm, inputMonthlyRent, inputRentExemption, inputTargetAmort,
            infl2026, infl2027, infl2028, infl2029, infl2030, infl2031,
            housingRate2026, housingRate2027, housingRate2028, housingRate2029, housingRate2030, housingRate2031
        ).forEach { it.addTextChangedListener(mainTextWatcher) }

        // Değerlendirme interaktif alanları - birbirini etkilesin
        val evalTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!isUpdating) calculateEvaluationFromInputs()
            }
        }
        inputRequiredInstallment.addTextChangedListener(evalTextWatcher)
        inputRequiredInterest.addTextChangedListener(evalTextWatcher)
        inputRequiredLoanRate.addTextChangedListener(evalTextWatcher)
    }

    private fun calculateAll() {
        if (isUpdating) return
        isUpdating = true

        try {
            // ═══════════════════════════════════════════════════════════════
            // GİRİŞ DEĞERLERİNİ PARSE ET
            // ═══════════════════════════════════════════════════════════════
            val housePrice = parseWholeNumber(inputHousePrice.text.toString())
            val expenseRatePercent = parseDecimalNumber(inputExpenseRate.text.toString())
            val loanRatePercent = parseDecimalNumber(inputLoanRate.text.toString())
            val interestRatePercent = parseDecimalNumber(inputInterestRate.text.toString())
            val loanTerm = parseWholeNumber(inputLoanTerm.text.toString()).toInt()
            val monthlyRent = parseWholeNumber(inputMonthlyRent.text.toString())
            val rentExemption = parseWholeNumber(inputRentExemption.text.toString())
            val targetAmort = parseWholeNumber(inputTargetAmort.text.toString()).toInt()

            if (housePrice <= 0) {
                isUpdating = false
                return
            }

            // Yüzdeleri orana çevir
            val expenseRate = expenseRatePercent / 100.0
            val loanRate = loanRatePercent / 100.0
            val interestRate = interestRatePercent / 100.0  // Aylık faiz oranı

            // ═══════════════════════════════════════════════════════════════
            // KREDİ HESAPLAMALARI (Excel formülleri)
            // ═══════════════════════════════════════════════════════════════
            // B3 = B1 * B2 (Toplam Alım Masrafı)
            val purchaseExpense = housePrice * expenseRate

            // B5 = B1 * B4 (Kredi Tutarı)
            val loanAmount = housePrice * loanRate

            // B6 = B1 - B5 (Peşinat)
            val downPayment = housePrice - loanAmount

            // B9 = ABS(PMT(B7, B8, B5)) - Excel PMT formülü
            val monthlyInstallment = if (loanAmount > 0 && interestRate > 0 && loanTerm > 0) {
                abs(pmt(interestRate, loanTerm, loanAmount))
            } else if (loanAmount > 0 && loanTerm > 0) {
                loanAmount / loanTerm
            } else {
                0.0
            }

            // B10 = B9 * B8 (Toplam Geri Ödeme)
            val totalRepayment = monthlyInstallment * loanTerm

            // B11 = B10 + B6 + B3 (Toplam Maliyet)
            val totalCost = totalRepayment + downPayment + purchaseExpense

            // ═══════════════════════════════════════════════════════════════
            // KİRA HESAPLAMALARI
            // ═══════════════════════════════════════════════════════════════
            // B13 = B12 * 12 (Yıllık Kira)
            val annualRent = monthlyRent * 12

            // B15 = MAX(B13 - B14, 0) (Vergilendirilen Kira)
            val taxableRent = max(annualRent - rentExemption, 0.0)

            // B16 = B13 * 0.8 (Net Yıllık Kira)
            val netAnnualRent = annualRent * 0.80

            // ═══════════════════════════════════════════════════════════════
            // ADİL FİYAT HESAPLAMASI
            // B20 = B16 * B19 - B3 - (B10 - B5)
            // ═══════════════════════════════════════════════════════════════
            val fairPrice = netAnnualRent * targetAmort - purchaseExpense - (totalRepayment - loanAmount)

            // ═══════════════════════════════════════════════════════════════
            // ÖZET
            // ═══════════════════════════════════════════════════════════════
            val noCreditCost = housePrice + purchaseExpense
            val rentSupport = monthlyInstallment - monthlyRent

            // ═══════════════════════════════════════════════════════════════
            // SONUÇLARI GÖSTER
            // ═══════════════════════════════════════════════════════════════
            resultExpense.text = formatCurrency(purchaseExpense)
            resultLoanAmount.text = formatCurrency(loanAmount)
            resultDownPayment.text = formatCurrency(downPayment)
            resultMonthlyInstallment.text = formatCurrency(monthlyInstallment)
            resultTotalRepayment.text = formatCurrency(totalRepayment)
            resultTotalCost.text = formatCurrency(totalCost)
            resultAnnualRent.text = formatCurrency(annualRent)
            resultTaxableRent.text = formatCurrency(taxableRent)
            resultNetAnnualRent.text = formatCurrency(netAnnualRent)
            resultFairPrice.text = formatCurrency(fairPrice)

            // Değerlendirme interaktif alanları - başlangıç değerleri
            if (inputRequiredInstallment.text.isEmpty()) {
                inputRequiredInstallment.setText(formatWholeNumber(monthlyInstallment))
            }
            if (inputRequiredInterest.text.isEmpty()) {
                inputRequiredInterest.setText("%.2f".format(interestRatePercent))
            }
            if (inputRequiredLoanRate.text.isEmpty()) {
                inputRequiredLoanRate.setText("%.0f".format(loanRatePercent))
            }

            // Özet
            summaryTotalCreditCost.text = "EVİN HERŞEY DAHİL KREDİLİ FİYATI: ${formatCurrency(totalCost)}"
            summaryNoCreditCost.text = "EVİN KREDİSİZ MASRAFLARI İLE FİYATI: ${formatCurrency(noCreditCost)}"
            summaryRentSupport.text = "TAKSİTLERE KİRA DESTEĞİ: ${formatCurrency(rentSupport)}"

            // Konut enflasyon projeksiyonu
            calculateHousingProjections(housePrice)

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isUpdating = false
        }
    }

    /**
     * Değerlendirme bölümündeki interaktif alanlar değiştiğinde çağrılır.
     * Birini değiştirince diğerleri otomatik ayarlanır.
     */
    private fun calculateEvaluationFromInputs() {
        if (isUpdating) return
        isUpdating = true

        try {
            val housePrice = parseWholeNumber(inputHousePrice.text.toString())
            val expenseRatePercent = parseDecimalNumber(inputExpenseRate.text.toString())
            val loanTerm = parseWholeNumber(inputLoanTerm.text.toString()).toInt()
            val monthlyRent = parseWholeNumber(inputMonthlyRent.text.toString())
            val targetAmort = parseWholeNumber(inputTargetAmort.text.toString()).toInt()

            if (housePrice <= 0) {
                isUpdating = false
                return
            }

            val expenseRate = expenseRatePercent / 100.0
            val purchaseExpense = housePrice * expenseRate
            val annualRent = monthlyRent * 12
            val netAnnualRent = annualRent * 0.80

            // Kullanıcının girdiği değerleri al
            val reqInstallment = parseWholeNumber(inputRequiredInstallment.text.toString())
            val reqInterestPercent = parseDecimalNumber(inputRequiredInterest.text.toString())
            val reqLoanRatePercent = parseDecimalNumber(inputRequiredLoanRate.text.toString())

            val reqInterestRate = reqInterestPercent / 100.0
            val reqLoanRate = reqLoanRatePercent / 100.0

            // Bu değerlere göre kredi tutarını hesapla
            val reqLoanAmount = housePrice * reqLoanRate
            
            // Bu değerlere göre toplam geri ödeme
            val reqTotalRepayment = reqInstallment * loanTerm

            // Adil fiyat hesapla
            val fairPrice = netAnnualRent * targetAmort - purchaseExpense - (reqTotalRepayment - reqLoanAmount)

            resultFairPrice.text = formatCurrency(fairPrice)

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isUpdating = false
        }
    }

    private fun calculateHousingProjections(basePrice: Double) {
        try {
            val rate2026 = parseDecimalNumber(housingRate2026.text.toString()) / 100.0
            val rate2027 = parseDecimalNumber(housingRate2027.text.toString()) / 100.0
            val rate2028 = parseDecimalNumber(housingRate2028.text.toString()) / 100.0
            val rate2029 = parseDecimalNumber(housingRate2029.text.toString()) / 100.0
            val rate2030 = parseDecimalNumber(housingRate2030.text.toString()) / 100.0
            val rate2031 = parseDecimalNumber(housingRate2031.text.toString()) / 100.0

            val val2025 = basePrice
            val val2026 = val2025 * (1 + rate2026)
            val val2027 = val2026 * (1 + rate2027)
            val val2028 = val2027 * (1 + rate2028)
            val val2029 = val2028 * (1 + rate2029)
            val val2030 = val2029 * (1 + rate2030)
            val val2031 = val2030 * (1 + rate2031)

            housingVal2025.text = formatCurrency(val2025)
            housingVal2026.text = formatCurrency(val2026)
            housingVal2027.text = formatCurrency(val2027)
            housingVal2028.text = formatCurrency(val2028)
            housingVal2029.text = formatCurrency(val2029)
            housingVal2030.text = formatCurrency(val2030)
            housingVal2031.text = formatCurrency(val2031)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Excel PMT fonksiyonu
     * PMT(rate, nper, pv) = pv * rate * (1+rate)^nper / ((1+rate)^nper - 1)
     */
    private fun pmt(rate: Double, nper: Int, pv: Double): Double {
        if (rate == 0.0) return pv / nper
        val factor = (1 + rate).pow(nper.toDouble())
        return pv * rate * factor / (factor - 1)
    }

    /**
     * Tam sayı parse et (11800000 veya 11.800.000 formatı)
     */
    private fun parseWholeNumber(text: String): Double {
        return try {
            text.replace(".", "")  // Binlik ayırıcıyı kaldır
                .replace(",", "")
                .replace(" ", "")
                .replace("₺", "")
                .replace("TL", "")
                .trim()
                .toDoubleOrNull() ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }

    /**
     * Ondalıklı sayı parse et (2.49 veya 2,49 formatı)
     * Ör: Faiz oranı, yüzde değerleri
     */
    private fun parseDecimalNumber(text: String): Double {
        return try {
            text.replace(",", ".")  // Virgülü noktaya çevir
                .replace(" ", "")
                .replace("%", "")
                .trim()
                .toDoubleOrNull() ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }

    private fun formatWholeNumber(value: Double): String {
        return "%,.0f".format(value).replace(",", ".")
    }

    private fun formatCurrency(value: Double): String {
        return "₺%,.2f".format(value).replace(",", "X").replace(".", ",").replace("X", ".")
    }
}
