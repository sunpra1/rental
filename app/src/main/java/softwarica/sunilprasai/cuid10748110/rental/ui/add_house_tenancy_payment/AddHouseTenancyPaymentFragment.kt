package softwarica.sunilprasai.cuid10748110.rental.ui.add_house_tenancy_payment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import softwarica.sunilprasai.cuid10748110.rental.R
import softwarica.sunilprasai.cuid10748110.rental.model.Payment
import softwarica.sunilprasai.cuid10748110.rental.model.PaymentDetail
import softwarica.sunilprasai.cuid10748110.rental.model.Tenancy
import softwarica.sunilprasai.cuid10748110.rental.model.Utility
import softwarica.sunilprasai.cuid10748110.rental.ui.shared_view_model.HouseViewModel
import softwarica.sunilprasai.cuid10748110.rental.utils.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private const val ARG_PARAM_TENANCY_POSITION = "tenancyPosition"
private const val ARG_PARAM_TENANCY_ID = "tenancyId"
private const val ARG_PARAM_HOUSE_POSITION = "housePosition"
private const val ARG_PARAM_HOUSE_ID = "houseId"

class AddHouseTenancyPaymentFragment : Fragment(),
    VariableUtilitiesRVA.VariableCostProvidedListener,
    FixedUtilitiesRVA.FixedCostProvidedListener, View.OnClickListener,
    CompoundButton.OnCheckedChangeListener, View.OnFocusChangeListener {
    private var mParamTenancyPosition: Int = -1
    private var mParamTenancyID: String? = null
    private var mParamHousePosition: Int = -1
    private var mParamHouseID: String? = null

    private lateinit var mPayableAmount: TextView

    private lateinit var mVariableUtilitiesRecyclerView: RecyclerView
    private lateinit var mVariableUtilitiesRecyclerViewAdapter: VariableUtilitiesRVA

    private lateinit var mFixedUtilitiesRecyclerView: RecyclerView
    private lateinit var mFixedUtilitiesRecyclerViewAdapter: FixedUtilitiesRVA

    private lateinit var mAmountPaidTil: TextInputLayout
    private lateinit var mAmountPaidEt: TextInputEditText

    private lateinit var mPaymentNoteTil: TextInputLayout
    private lateinit var mPaymentNoteEt: TextInputEditText

    private lateinit var mAddPaymentBtn: MaterialButton
    private lateinit var mViewModel: HouseViewModel

    private lateinit var mAdvanceAndDueWrapper: View
    private lateinit var mAdjustAdvanceCB: CheckBox
    private lateinit var mIncludeDueCB: CheckBox

    private lateinit var mPaymentMadeForTil: TextInputLayout
    private lateinit var mPaymentMadeForCV: CalendarView

    private lateinit var mTenancy: Tenancy
    private var mPaymentDetails: ArrayList<PaymentDetail> = ArrayList()

    private var mPaymentMadeForDate: Date? = null
    private lateinit var mContext: Context
    private var mIsUtilityNotApprovedDialogDisplayed: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mParamHousePosition = it.getInt(ARG_PARAM_HOUSE_POSITION)
            mParamHouseID = it.getString(ARG_PARAM_HOUSE_ID)
            mParamTenancyPosition = it.getInt(ARG_PARAM_TENANCY_POSITION)
            mParamTenancyID = it.getString(ARG_PARAM_TENANCY_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_house_tenancy_payment, container, false)
        mContext = requireContext()
        mPayableAmount = view.findViewById(R.id.payableAmount)
        mVariableUtilitiesRecyclerView = view.findViewById(R.id.variableUtilitiesRV)
        mVariableUtilitiesRecyclerView.layoutManager = GridLayoutManager(mContext, 2)
        mVariableUtilitiesRecyclerViewAdapter = VariableUtilitiesRVA()
        mVariableUtilitiesRecyclerViewAdapter.setOnVariableCostProvidedListener(this)
        mVariableUtilitiesRecyclerView.adapter = mVariableUtilitiesRecyclerViewAdapter

        mFixedUtilitiesRecyclerView = view.findViewById(R.id.fixedUtilitiesRV)
        mFixedUtilitiesRecyclerView.layoutManager = GridLayoutManager(mContext, 2)
        mFixedUtilitiesRecyclerViewAdapter = FixedUtilitiesRVA()
        mFixedUtilitiesRecyclerViewAdapter.setOnVariableCostProvidedListener(this)
        mFixedUtilitiesRecyclerView.adapter = mFixedUtilitiesRecyclerViewAdapter

        mAdvanceAndDueWrapper = view.findViewById(R.id.advanceAndDueWrapper)
        mAdjustAdvanceCB = view.findViewById(R.id.adjustAdvanceCB)
        mAdjustAdvanceCB.setOnCheckedChangeListener(this)
        mIncludeDueCB = view.findViewById(R.id.includeDueCB)
        mIncludeDueCB.setOnCheckedChangeListener(this)

        mAmountPaidTil = view.findViewById(R.id.amountPaidTil)
        mAmountPaidEt = view.findViewById(R.id.amountPaidEt)
        mAmountPaidEt.onFocusChangeListener = this

        mPaymentNoteTil = view.findViewById(R.id.paymentNoteTil)
        mPaymentNoteEt = view.findViewById(R.id.paymentNoteEt)

        mPaymentNoteTil = view.findViewById(R.id.paymentNoteTil)
        mPaymentNoteEt = view.findViewById(R.id.paymentNoteEt)

        mAddPaymentBtn = view.findViewById(R.id.addPaymentBtn)
        mAddPaymentBtn.setOnClickListener(this)

        mPaymentMadeForTil = view.findViewById(R.id.paymentMadeForTil)
        mPaymentMadeForCV = view.findViewById(R.id.paymentMadeForCV)
        mPaymentMadeForCV.setOnDateChangeListener { _, year, month, _ ->
            if (mTenancy.payments!!.map {
                    "${it.paidForYear}-${it.paiForMonth}"
                }.indexOf("${year}-${month + 1}") > -1) {
                mPaymentMadeForTil.apply {
                    isErrorEnabled = true
                    error = "Payment has already been made for ${
                        SimpleDateFormat(MMMMM_yyyy, Locale.ENGLISH).format(
                            SimpleDateFormat(
                                yyyy_MM, Locale.ENGLISH
                            ).parse("${year}-${month + 1}")!!
                        )
                    }"
                }
                mPaymentMadeForDate = null
            } else {
                val tenancyStartDate = Calendar.getInstance().apply {
                    time = mTenancy.startDate!!
                }
                mPaymentMadeForTil.isErrorEnabled = false
                val dayOfMonth =
                    if (tenancyStartDate.get(Calendar.DAY_OF_MONTH) > 28 && month == 1 && year % 4 == 0) {
                        29
                    } else if (tenancyStartDate.get(Calendar.DAY_OF_MONTH) > 28 && month == 1) {
                        28
                    } else {
                        tenancyStartDate.get(Calendar.DAY_OF_MONTH)
                    }
                mPaymentMadeForDate = Calendar.getInstance().let {
                    it.set(year, month, dayOfMonth)
                    it.time
                }
                mPaymentMadeForCV.date = mPaymentMadeForDate!!.time
                AppToast.show(
                    mContext,
                    "Tenancy start date was auto selected",
                    Toast.LENGTH_LONG
                )
            }
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel = ViewModelProvider(requireActivity()).get(HouseViewModel::class.java)

        mViewModel.getHouses().observe(requireActivity()) {
            if (isAdded) {
                if (it != null && mParamHousePosition != -1 && mParamTenancyPosition != -1 && mParamTenancyPosition < it[mParamHousePosition].tenancies!!.size && it[mParamHousePosition].id == mParamHouseID && it[mParamHousePosition].tenancies!![mParamTenancyPosition].id == mParamTenancyID) {
                    mTenancy = it[mParamHousePosition].tenancies!![mParamTenancyPosition]
                    updateViewElements()
                } else {
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        }
    }

    private fun updateViewElements() {
        val mVariableUtilities =
            mTenancy.utilities!!.filter { utility -> utility.isVariableCost!! && utility.approved!! } as ArrayList<Utility>
        val mFixedUtilities =
            mTenancy.utilities!!.filter { utility -> !utility.isVariableCost!! && utility.approved!! } as ArrayList<Utility>
        mVariableUtilitiesRecyclerViewAdapter.setUtilities(mVariableUtilities)
        mFixedUtilitiesRecyclerViewAdapter.setUtilities(mFixedUtilities)

        if (mTenancy.advanceAmount!! > 0 || mTenancy.dueAmount!! > 0) {
            mAdvanceAndDueWrapper.visibility = View.VISIBLE
            if (mTenancy.advanceAmount!! > 0) {
                mAdjustAdvanceCB.visibility = View.VISIBLE
                mAdjustAdvanceCB.text =
                    resources.getString(R.string.adjust_advance_of).format(mTenancy.advanceAmount!!)
            } else {
                mAdjustAdvanceCB.visibility = View.GONE
            }

            if (mTenancy.dueAmount!! > 0) {
                mIncludeDueCB.visibility = View.VISIBLE
                mIncludeDueCB.text =
                    resources.getString(R.string.include_due_of).format(mTenancy.dueAmount!!)
            } else {
                mIncludeDueCB.visibility = View.GONE
            }
        } else {
            mAdvanceAndDueWrapper.visibility = View.VISIBLE
        }

        mPaymentDetails.add(PaymentDetail().apply
        {
            name = "RENT"
            isVariableCost = false
            price = mTenancy.amount
        })

        mPayableAmount.text = calculatePayableAmount().toString()

        //Determining min date
        val today = Calendar.getInstance(Locale.ENGLISH).apply {
            clear(Calendar.AM_PM)
            clear(Calendar.HOUR)
            clear(Calendar.HOUR_OF_DAY)
            clear(Calendar.MINUTE)
            clear(Calendar.SECOND)
            clear(Calendar.MILLISECOND)
        }
        val calendarStartDate = Calendar.getInstance(Locale.ENGLISH).apply {
            time = mTenancy.startDate!!
        }
        val dateBeingIncreasedByAMonthFromTenancyStartDateTillToday =
            Calendar.getInstance(Locale.ENGLISH).apply {
                time = mTenancy.startDate!!
            }
        val due: ArrayList<String> = ArrayList()
        while (dateBeingIncreasedByAMonthFromTenancyStartDateTillToday.before(today)) {
            due.add(
                "${dateBeingIncreasedByAMonthFromTenancyStartDateTillToday.get(Calendar.YEAR)}-${
                    dateBeingIncreasedByAMonthFromTenancyStartDateTillToday.get(
                        Calendar.MONTH
                    ) + 1
                }"
            )
            dateBeingIncreasedByAMonthFromTenancyStartDateTillToday.set(
                Calendar.MONTH,
                dateBeingIncreasedByAMonthFromTenancyStartDateTillToday.get(Calendar.MONTH) + 1
            )
        }

        if (due.size > 0 && mTenancy.accrue!! == Tenancy.RentAccrueAt.END) {
            due.removeAt(0)
        } else {
            due.add(
                "${today.get(Calendar.YEAR)}-${
                    today.get(
                        Calendar.MONTH
                    ) + 2
                }"
            )
        }

        val paid = mTenancy.payments!!.map {
            "${it.paidForYear}-${it.paiForMonth}"
        }

        val dueButNotPaid = due.filter { paid.indexOf(it) == -1 }
        if (dueButNotPaid.isNotEmpty()) {
            val yearMonthArray = dueButNotPaid[0].split("-").map { it.trim() }
            val dateString =
                if (calendarStartDate.get(Calendar.DAY_OF_MONTH) > 28 && yearMonthArray[1].toInt() == 2 && yearMonthArray[0].toInt() % 4 == 0) {
                    "${dueButNotPaid[0]}-29}"
                } else if (calendarStartDate.get(Calendar.DAY_OF_MONTH) > 28 && yearMonthArray[1].toInt() == 2) {
                    "${dueButNotPaid[0]}-28"
                } else {
                    "${dueButNotPaid[0]}-${calendarStartDate.get(Calendar.DAY_OF_MONTH)}"
                }
            val minDate = SimpleDateFormat(
                yyyy_MM_dd,

                Locale.ENGLISH
            ).parse(dateString)
            mPaymentMadeForCV.minDate = minDate!!.time
            mPaymentMadeForCV.date = minDate.time
        } else {
            val minDate = SimpleDateFormat(
                yyyy_MM_dd,
                Locale.ENGLISH
            ).parse(
                SimpleDateFormat(yyyy_MM_dd, Locale.ENGLISH).format(
                    today.apply {
                        set(Calendar.DAY_OF_MONTH, calendarStartDate.get(Calendar.DAY_OF_MONTH))
                    }.time
                )
            )
            mPaymentMadeForCV.minDate = minDate!!.time
            mPaymentMadeForCV.date = minDate.time
        }

        //Displaying dialog if some utilities are not approved
        val utilitiesStillNotApproved =
            mTenancy.utilities!!.filter { !it.approved!! } as ArrayList<Utility>
        if (utilitiesStillNotApproved.size > 0 && !mIsUtilityNotApprovedDialogDisplayed) {
            mIsUtilityNotApprovedDialogDisplayed = !mIsUtilityNotApprovedDialogDisplayed
            val notApprovedUtilitiesMessage =
                StringBuilder().append("${if (utilitiesStillNotApproved.size > 1) "Utilities" else "Utility"} ")

            utilitiesStillNotApproved.mapIndexed { index, utility ->
                when {
                    index < utilitiesStillNotApproved.size - 2 -> {
                        notApprovedUtilitiesMessage.append(utility.name).append(", ")
                    }
                    index < utilitiesStillNotApproved.size - 1 -> {
                        notApprovedUtilitiesMessage.append(utility.name).append(" and ")
                    }
                    else -> {
                        notApprovedUtilitiesMessage.append(utility.name)
                    }
                }
            }
            notApprovedUtilitiesMessage.append(" ${if (utilitiesStillNotApproved.size > 1) "are" else "is"} still not approved by tenant.\nDo you still want to make a payment")
            AlertDialog.Builder(mContext).apply {
                setIcon(R.drawable.information)
                setTitle("UTILITIES UNAPPROVED")
                setMessage(notApprovedUtilitiesMessage)
                setPositiveButton(R.string.my_continue) { dialog, _ ->
                    dialog.dismiss()
                }
                setNegativeButton(R.string.cancel) { _, _ ->
                    if (isAdded) requireActivity().supportFragmentManager.popBackStack()
                }
                show()
            }
        }
    }

    private fun calculatePayableAmount(): Int {
        var payableAmount = 0
        mPaymentDetails.map {
            payableAmount += if (it.isVariableCost!!) {
                it.units!! * it.price!!
            } else {
                it.price!!
            }
        }
        return payableAmount
    }

    companion object {
        @JvmStatic
        fun newInstance(
            housePosition: Int,
            houseId: String,
            tenantPosition: Int,
            tenantId: String
        ) =
            AddHouseTenancyPaymentFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM_HOUSE_POSITION, housePosition)
                    putString(ARG_PARAM_HOUSE_ID, houseId)
                    putInt(ARG_PARAM_TENANCY_POSITION, tenantPosition)
                    putString(ARG_PARAM_TENANCY_ID, tenantId)
                }
            }
    }

    override fun onVariableCostProvided(utility: Utility, value: Int) {
        val index = mPaymentDetails.indexOfFirst { it.name == utility.name }
        if (index == -1 && value != 0) {
            mPaymentDetails.add(PaymentDetail().apply {
                name = utility.name
                isVariableCost = utility.isVariableCost
                price = utility.price
                units = value
            })
        } else if (value == 0) {
            mPaymentDetails =
                mPaymentDetails.filter { it.name != utility.name } as ArrayList<PaymentDetail>
        } else {
            mPaymentDetails[index].units = value
        }
        mPayableAmount.text = calculatePayableAmount().toString()
    }

    private fun validateAndConfirm() {
        var isValid = true

        mAmountPaidEt.text?.let {
            if (it.trim().isEmpty()) {
                isValid = false
                mAmountPaidTil.apply {
                    isErrorEnabled = true
                    error = "Amount received is required"
                }
            }
        }

        if (mPaymentMadeForDate == null) {
            isValid = false
            mPaymentMadeForTil.apply {
                isErrorEnabled = true
                error = "Please select the date for which payment is made"
            }
        } else {
            val calenderDate = Calendar.getInstance(Locale.ENGLISH).apply {
                time = mPaymentMadeForDate!!
            }
            if (mTenancy.payments!!.map {
                    "${it.paidForYear}-${it.paiForMonth}"
                }
                    .indexOf("${calenderDate.get(Calendar.YEAR)}-${calenderDate.get(Calendar.MONTH) + 1}") > -1) {
                isValid = false
                mPaymentMadeForTil.apply {
                    isErrorEnabled = true
                    error = "Payment has already been made for ${
                        SimpleDateFormat(MMMMM_yyyy, Locale.ENGLISH).format(
                            SimpleDateFormat(
                                yyyy_MM, Locale.ENGLISH
                            ).parse("${calenderDate.get(Calendar.YEAR)}-${calenderDate.get(Calendar.MONTH) + 1}")!!
                        )
                    }"
                }
                mPaymentMadeForDate = null
            }
        }

        if (isValid) {
            val ignoredUtilities: ArrayList<Utility> = mTenancy.utilities!!.filter { utility ->
                utility.approved!! && mPaymentDetails.indexOfFirst { it.name == utility.name } == -1
            } as ArrayList<Utility>

            if (ignoredUtilities.size > 0) {
                val ignoredUtilitiesName = StringBuilder()
                ignoredUtilities.mapIndexed { index, utility ->
                    when {
                        index < ignoredUtilities.size - 2 -> {
                            ignoredUtilitiesName.append(utility.name).append(", ")
                        }
                        index < ignoredUtilities.size - 1 -> {
                            ignoredUtilitiesName.append(utility.name).append(" and ")
                        }
                        else -> {
                            ignoredUtilitiesName.append(utility.name)
                        }
                    }
                }
                AlertDialog.Builder(mContext).apply {
                    setIcon(R.drawable.information)
                    setTitle("UTILITIES IGNORED")
                    setMessage("$ignoredUtilitiesName.\nDo you still want to add payment?")
                    setPositiveButton(R.string.ok) { _, _ ->
                        addPayment()
                    }
                    setNegativeButton(R.string.cancel) { _, _ ->
                        //Do nothing
                    }
                    show()
                }
            } else addPayment()
        } else {
            VibrateView.vibrate(
                mContext,
                R.anim.shake,
                requireView().findViewById(R.id.formCardView)
            )
        }
    }

    override fun onFixedCostProvided(utility: Utility, toBeIncluded: Boolean) {
        val index = mPaymentDetails.indexOfFirst { it.name == utility.name }
        if (index == -1 && toBeIncluded) {
            if (toBeIncluded) {
                mPaymentDetails.add(PaymentDetail().apply {
                    name = utility.name
                    isVariableCost = utility.isVariableCost
                    price = utility.price
                })
            }
        } else if (!toBeIncluded) {
            mPaymentDetails =
                mPaymentDetails.filter { it.name != utility.name } as ArrayList<PaymentDetail>
        }
        mPayableAmount.text = calculatePayableAmount().toString()
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                mAddPaymentBtn.id -> {
                    validateAndConfirm()
                }
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if (buttonView != null) {
            when (buttonView.id) {
                mAdjustAdvanceCB.id -> {
                    if (mTenancy.advanceAmount!! > 0) {
                        val index =
                            mPaymentDetails.indexOfFirst { it.name.equals("ADVANCE_ADJUSTED") }
                        if (index == -1 && isChecked) {
                            mPaymentDetails.add(PaymentDetail().apply {
                                name = "ADVANCE_ADJUSTED"
                                isVariableCost = false
                                price = -(mTenancy.advanceAmount!!)
                            })
                        } else if (!isChecked) {
                            mPaymentDetails =
                                mPaymentDetails.filter { !it.name.equals("ADVANCE_ADJUSTED") } as ArrayList<PaymentDetail>
                        }
                        mPayableAmount.text = calculatePayableAmount().toString()
                    }
                }

                mIncludeDueCB.id -> {
                    if (mTenancy.dueAmount!! > 0) {
                        val index = mPaymentDetails.indexOfFirst { it.name.equals("DUE_INCLUDED") }
                        if (index == -1 && isChecked) {
                            mPaymentDetails.add(PaymentDetail().apply {
                                name = "DUE_INCLUDED"
                                isVariableCost = false
                                price = mTenancy.dueAmount!!
                            })
                        } else if (!isChecked) {
                            mPaymentDetails =
                                mPaymentDetails.filter { !it.name.equals("DUE_INCLUDED") } as ArrayList<PaymentDetail>
                        }
                        mPayableAmount.text = calculatePayableAmount().toString()
                    }
                }
            }
        }
    }

    private fun getPaymentObject(): Payment {
        val paymentMadeForDate = Calendar.getInstance(Locale.ENGLISH).apply {
            time = mPaymentMadeForDate!!
        }
        return Payment().apply {
            amountPayable = calculatePayableAmount()
            amountReceived = mAmountPaidEt.text!!.trim().toString().toInt()
            paiForMonth = paymentMadeForDate.get(Calendar.MONTH) + 1
            paidForYear = paymentMadeForDate.get(Calendar.YEAR)
            paymentDetails = mPaymentDetails
            mPaymentNoteEt.text?.let {
                if (it.trim().toString().isNotEmpty()) note = it.trim().toString()
            }
        }
    }

    private fun addPayment(payment: Payment = getPaymentObject()) {
        val requestBody =
            RequestBody.create(MediaType.parse(TYPE_JSON), payment.getJSONObject().toString())

        val consumer = APIService.getService(APIConsumer::class.java)
        val addPayment = consumer.addPayment(
            UserToken.getInstance(mContext).token!!,
            mParamHouseID!!,
            mParamTenancyID!!,
            requestBody
        )
        addPayment.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    val tenancy = Tenancy(JSONObject(response.body()!!.string()))
                    mViewModel.replaceTenancy(
                        mParamHousePosition,
                        mParamTenancyPosition,
                        tenancy
                    )
                    AppToast.show(
                        mContext,
                        "Payment has been added successfully",
                        Toast.LENGTH_LONG
                    )
                    requireActivity().supportFragmentManager.popBackStack()
                } else if (!response.isSuccessful && response.errorBody() != null) {
                    val errorResponse = JSONObject(response.errorBody()!!.string())
                    if (errorResponse.has("message")) {
                        val errorMessage = StringBuilder()
                        try {
                            val errorResponseMessage = errorResponse.getJSONObject("message")
                            errorResponseMessage.keys().forEach {
                                errorMessage.append(errorResponseMessage[it]).append("\n")
                            }
                        } catch (e: JSONException) {
                            errorMessage.append(errorResponse.getString("message"))
                        } finally {
                            if (errorMessage.isNotEmpty())
                                displayAPIErrorResponseDialog(errorMessage.toString())
                        }
                    } else {
                        displayAPIErrorResponseDialog("Unable to add payment. Please try again later")
                    }
                } else {
                    displayAPIErrorResponseDialog("Unable to add payment. Please try again later")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                displayAPIErrorResponseDialog("Unable to add payment. Please try again later")
            }

            private fun displayAPIErrorResponseDialog(message: String) {

                AlertDialog.Builder(mContext)
                    .setIcon(R.drawable.information)
                    .setTitle("INFORMATION")
                    .setMessage(message)
                    .setPositiveButton(
                        "OK"
                    ) { dialog, _ -> dialog!!.dismiss() }
                    .show()
            }
        })
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (v != null) {
            when (v.id) {
                mAmountPaidEt.id ->
                    if (hasFocus) {
                        mAmountPaidTil.isErrorEnabled = false
                    } else {
                        mAmountPaidEt.text?.let {
                            if (it.trim().isEmpty()) {
                                mAmountPaidTil.apply {
                                    isErrorEnabled = true
                                    error = "Amount received is required"
                                }
                                VibrateView.vibrate(mContext, R.anim.shake, mAmountPaidTil)
                            }
                        }
                    }
            }
        }
    }
}