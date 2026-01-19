package com.evanaliz.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.evanaliz.R
import com.evanaliz.location.LocationScoreCalculator
import com.evanaliz.location.MetroDataLoader
import com.evanaliz.location.ProximityResult
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow

/**
 * Tab'lı Analiz Ekranı
 * 
 * Tab 1: Ev Yatırım Analizi
 * Tab 2: Konum Değeri
 */
class TabbedAnalysisActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_HOUSE_PRICE = "house_price"
        const val EXTRA_MONTHLY_RENT = "monthly_rent"
        const val EXTRA_LOCATION = "location"
        const val EXTRA_LATITUDE = "latitude"
        const val EXTRA_LONGITUDE = "longitude"
    }

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tabbed_analysis)

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        // Adapter'ı ayarla
        val adapter = AnalysisPagerAdapter(this)
        viewPager.adapter = adapter

        // Tab başlıklarını ayarla
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "💰 Yatırım Analizi"
                1 -> "📍 Konum Değeri"
                else -> "Tab ${position + 1}"
            }
        }.attach()
    }

    inner class AnalysisPagerAdapter(fragmentActivity: FragmentActivity) : 
        FragmentStateAdapter(fragmentActivity) {

        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> InvestmentAnalysisFragment.newInstance(
                    intent.getDoubleExtra(EXTRA_HOUSE_PRICE, 0.0),
                    intent.getDoubleExtra(EXTRA_MONTHLY_RENT, 0.0)
                )
                1 -> LocationValueFragment.newInstance(
                    intent.getStringExtra(EXTRA_LOCATION) ?: "",
                    intent.getDoubleExtra(EXTRA_LATITUDE, 0.0),
                    intent.getDoubleExtra(EXTRA_LONGITUDE, 0.0),
                    intent.getDoubleExtra(EXTRA_HOUSE_PRICE, 0.0)
                )
                else -> Fragment()
            }
        }
    }
}

/**
 * Tab 1: Yatırım Analizi Fragment
 * Excel modelindeki TÜM verileri gösterir ve real-time hesaplama yapar.
 */
class InvestmentAnalysisFragment : Fragment() {

    companion object {
        private const val ARG_HOUSE_PRICE = "house_price"
        private const val ARG_MONTHLY_RENT = "monthly_rent"

        fun newInstance(housePrice: Double, monthlyRent: Double): InvestmentAnalysisFragment {
            return InvestmentAnalysisFragment().apply {
                arguments = Bundle().apply {
                    putDouble(ARG_HOUSE_PRICE, housePrice)
                    putDouble(ARG_MONTHLY_RENT, monthlyRent)
                }
            }
        }
    }

    // UI Elementleri
    private lateinit var inputHousePrice: EditText
    private lateinit var inputExpenseRate: EditText
    private lateinit var inputLoanRate: EditText
    private lateinit var inputInterestRate: EditText
    private lateinit var inputLoanTerm: EditText
    private lateinit var inputMonthlyRent: EditText
    private lateinit var inputRentExemption: EditText
    private lateinit var inputTargetAmort: EditText

    private lateinit var resultExpense: TextView
    private lateinit var resultLoanAmount: TextView
    private lateinit var resultDownPayment: TextView
    private lateinit var resultMonthlyInstallment: TextView
    private lateinit var resultTotalRepayment: TextView
    private lateinit var resultTotalCost: TextView
    private lateinit var resultAnnualRent: TextView
    private lateinit var resultTaxableRent: TextView
    private lateinit var resultNetAnnualRent: TextView

    private lateinit var inputRequiredInstallment: EditText
    private lateinit var inputRequiredInterest: EditText
    private lateinit var inputRequiredLoanRate: EditText
    private lateinit var resultFairPrice: TextView

    private lateinit var summaryTotalCreditCost: TextView
    private lateinit var summaryNoCreditCost: TextView
    private lateinit var summaryRentSupport: TextView

    private lateinit var infl2026: EditText
    private lateinit var infl2027: EditText
    private lateinit var infl2028: EditText
    private lateinit var infl2029: EditText
    private lateinit var infl2030: EditText
    private lateinit var infl2031: EditText

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

    private var isUpdating = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_analysis_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        loadInitialData()
        setupListeners()
        calculateAll()
    }

    private fun initViews(view: View) {
        inputHousePrice = view.findViewById(R.id.input_house_price)
        inputExpenseRate = view.findViewById(R.id.input_expense_rate)
        inputLoanRate = view.findViewById(R.id.input_loan_rate)
        inputInterestRate = view.findViewById(R.id.input_interest_rate)
        inputLoanTerm = view.findViewById(R.id.input_loan_term)
        inputMonthlyRent = view.findViewById(R.id.input_monthly_rent)
        inputRentExemption = view.findViewById(R.id.input_rent_exemption)
        inputTargetAmort = view.findViewById(R.id.input_target_amort)

        resultExpense = view.findViewById(R.id.result_expense)
        resultLoanAmount = view.findViewById(R.id.result_loan_amount)
        resultDownPayment = view.findViewById(R.id.result_down_payment)
        resultMonthlyInstallment = view.findViewById(R.id.result_monthly_installment)
        resultTotalRepayment = view.findViewById(R.id.result_total_repayment)
        resultTotalCost = view.findViewById(R.id.result_total_cost)
        resultAnnualRent = view.findViewById(R.id.result_annual_rent)
        resultTaxableRent = view.findViewById(R.id.result_taxable_rent)
        resultNetAnnualRent = view.findViewById(R.id.result_net_annual_rent)

        inputRequiredInstallment = view.findViewById(R.id.input_required_installment)
        inputRequiredInterest = view.findViewById(R.id.input_required_interest)
        inputRequiredLoanRate = view.findViewById(R.id.input_required_loan_rate)
        resultFairPrice = view.findViewById(R.id.result_fair_price)

        summaryTotalCreditCost = view.findViewById(R.id.summary_total_credit_cost)
        summaryNoCreditCost = view.findViewById(R.id.summary_no_credit_cost)
        summaryRentSupport = view.findViewById(R.id.summary_rent_support)

        infl2026 = view.findViewById(R.id.infl_2026)
        infl2027 = view.findViewById(R.id.infl_2027)
        infl2028 = view.findViewById(R.id.infl_2028)
        infl2029 = view.findViewById(R.id.infl_2029)
        infl2030 = view.findViewById(R.id.infl_2030)
        infl2031 = view.findViewById(R.id.infl_2031)

        housingRate2026 = view.findViewById(R.id.housing_rate_2026)
        housingRate2027 = view.findViewById(R.id.housing_rate_2027)
        housingRate2028 = view.findViewById(R.id.housing_rate_2028)
        housingRate2029 = view.findViewById(R.id.housing_rate_2029)
        housingRate2030 = view.findViewById(R.id.housing_rate_2030)
        housingRate2031 = view.findViewById(R.id.housing_rate_2031)

        housingVal2025 = view.findViewById(R.id.housing_val_2025)
        housingVal2026 = view.findViewById(R.id.housing_val_2026)
        housingVal2027 = view.findViewById(R.id.housing_val_2027)
        housingVal2028 = view.findViewById(R.id.housing_val_2028)
        housingVal2029 = view.findViewById(R.id.housing_val_2029)
        housingVal2030 = view.findViewById(R.id.housing_val_2030)
        housingVal2031 = view.findViewById(R.id.housing_val_2031)

        view.findViewById<android.widget.Button>(R.id.btn_close).setOnClickListener {
            requireActivity().finish()
        }
    }

    private fun loadInitialData() {
        val housePrice = arguments?.getDouble(ARG_HOUSE_PRICE, 0.0) ?: 0.0
        val monthlyRent = arguments?.getDouble(ARG_MONTHLY_RENT, 0.0) ?: 0.0

        if (housePrice > 0) {
            inputHousePrice.setText(formatWholeNumber(housePrice))
        }
        if (monthlyRent > 0) {
            inputMonthlyRent.setText(formatWholeNumber(monthlyRent))
        }
    }

    private fun setupListeners() {
        val mainTextWatcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (!isUpdating) calculateAll()
            }
        }

        listOf(
            inputHousePrice, inputExpenseRate, inputLoanRate, inputInterestRate,
            inputLoanTerm, inputMonthlyRent, inputRentExemption, inputTargetAmort,
            infl2026, infl2027, infl2028, infl2029, infl2030, infl2031,
            housingRate2026, housingRate2027, housingRate2028, housingRate2029, housingRate2030, housingRate2031
        ).forEach { it.addTextChangedListener(mainTextWatcher) }

        val evalTextWatcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
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

            val expenseRate = expenseRatePercent / 100.0
            val loanRate = loanRatePercent / 100.0
            val interestRate = interestRatePercent / 100.0
            
            val purchaseExpense = housePrice * expenseRate
            val loanAmount = housePrice * loanRate
            val downPayment = housePrice - loanAmount
            
            val monthlyInstallment = if (loanAmount > 0 && interestRate > 0 && loanTerm > 0) {
                kotlin.math.abs(pmt(interestRate, loanTerm, loanAmount))
            } else if (loanAmount > 0 && loanTerm > 0) {
                loanAmount / loanTerm
            } else {
                0.0
            }

            val totalRepayment = monthlyInstallment * loanTerm
            val totalCost = totalRepayment + downPayment + purchaseExpense
            
            val annualRent = monthlyRent * 12
            val taxableRent = kotlin.math.max(annualRent - rentExemption, 0.0)
            val netAnnualRent = annualRent * 0.80
            
            val fairPrice = netAnnualRent * targetAmort - purchaseExpense - (totalRepayment - loanAmount)
            
            val noCreditCost = housePrice + purchaseExpense
            val rentSupport = monthlyInstallment - monthlyRent

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

            if (inputRequiredInstallment.text.isEmpty()) {
                inputRequiredInstallment.setText(formatWholeNumber(monthlyInstallment))
            }
            if (inputRequiredInterest.text.isEmpty()) {
                inputRequiredInterest.setText("%.2f".format(interestRatePercent))
            }
            if (inputRequiredLoanRate.text.isEmpty()) {
                inputRequiredLoanRate.setText("%.0f".format(loanRatePercent))
            }

            summaryTotalCreditCost.text = "EVİN HERŞEY DAHİL KREDİLİ FİYATI: ${formatCurrency(totalCost)}"
            summaryNoCreditCost.text = "EVİN KREDİSİZ MASRAFLARI İLE FİYATI: ${formatCurrency(noCreditCost)}"
            summaryRentSupport.text = "TAKSİTLERE KİRA DESTEĞİ: ${formatCurrency(rentSupport)}"

            calculateHousingProjections(housePrice)

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isUpdating = false
        }
    }

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

            val reqInstallment = parseWholeNumber(inputRequiredInstallment.text.toString())
            val reqLoanRatePercent = parseDecimalNumber(inputRequiredLoanRate.text.toString())
            
            val reqLoanRate = reqLoanRatePercent / 100.0
            val reqLoanAmount = housePrice * reqLoanRate
            val reqTotalRepayment = reqInstallment * loanTerm

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

    private fun pmt(rate: Double, nper: Int, pv: Double): Double {
        if (rate == 0.0) return pv / nper
        val factor = (1 + rate).pow(nper.toDouble())
        return pv * rate * factor / (factor - 1)
    }

    private fun parseWholeNumber(text: String): Double {
        return try {
            text.replace(".", "")
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


    private fun parseDecimalNumber(text: String): Double {
        return try {
            text.replace(",", ".")
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

/**
 * Tab 2: Konum Değeri Fragment
 */
class LocationValueFragment : Fragment() {

    companion object {
        private const val ARG_LOCATION = "location"
        private const val ARG_LATITUDE = "latitude"
        private const val ARG_LONGITUDE = "longitude"
        private const val ARG_HOUSE_PRICE = "house_price"

        fun newInstance(location: String, lat: Double, lon: Double, housePrice: Double): LocationValueFragment {
            return LocationValueFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_LOCATION, location)
                    putDouble(ARG_LATITUDE, lat)
                    putDouble(ARG_LONGITUDE, lon)
                    putDouble(ARG_HOUSE_PRICE, housePrice)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_location_value, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val location = arguments?.getString(ARG_LOCATION) ?: ""
        val lat = arguments?.getDouble(ARG_LATITUDE) ?: 0.0
        val lon = arguments?.getDouble(ARG_LONGITUDE) ?: 0.0
        val housePrice = arguments?.getDouble(ARG_HOUSE_PRICE) ?: 0.0

        val tvLocation = view.findViewById<TextView>(R.id.tv_location)
        val tvScore = view.findViewById<TextView>(R.id.tv_location_score)
        val tvScoreDesc = view.findViewById<TextView>(R.id.tv_score_description)
        val tvNearbyStations = view.findViewById<TextView>(R.id.tv_nearby_stations)

        tvLocation.text = if (location.isNotEmpty()) location else "Konum bilgisi bulunamadı"

        if (lat != 0.0 && lon != 0.0) {
            // Metro verilerini yükle ve analiz et
            val metroData = MetroDataLoader.loadMetroData(requireContext())
            val proximityResults = LocationScoreCalculator.analyzeProximity(lat, lon, metroData)
            val totalScore = LocationScoreCalculator.calculateTotalLocationScore(proximityResults)
            val scoreDesc = LocationScoreCalculator.getScoreDescription(totalScore)

            tvScore.text = "$totalScore / 100"
            tvScoreDesc.text = scoreDesc

            // Yakın durakları listele
            if (proximityResults.isNotEmpty()) {
                val stationsText = proximityResults.take(5).joinToString("\n") { result ->
                    "• ${result.station.name} (${result.line.name}): %.1f km - %d puan".format(
                        result.distanceKm, result.score
                    )
                }
                tvNearbyStations.text = stationsText
            } else {
                tvNearbyStations.text = "3 km içinde planlanan metro durağı bulunamadı"
            }
        } else {
            tvScore.text = "?"
            tvScoreDesc.text = "Koordinat bilgisi bulunamadı"
            tvNearbyStations.text = "Konum analizi yapılamadı.\nİlan sayfasından koordinat çekilemedi."
        }
    }
}
