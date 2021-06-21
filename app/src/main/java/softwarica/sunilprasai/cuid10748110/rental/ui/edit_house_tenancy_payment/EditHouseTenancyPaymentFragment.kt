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
import softwarica.sunilprasai.cuid10748110.rental.ui.edit_house_tenancy_payment.FixedUtilitiesPaymentRVA
import softwarica.sunilprasai.cuid10748110.rental.ui.edit_house_tenancy_payment.OtherAdjustmentsRVA
import softwarica.sunilprasai.cuid10748110.rental.ui.edit_house_tenancy_payment.VariableUtilitiesPaymentRVA
import softwarica.sunilprasai.cuid10748110.rental.ui.shared_view_model.HouseViewModel
import softwarica.sunilprasai.cuid10748110.rental.utils.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private const val ARG_PARAM_TENANCY_POSITION = "tenancyPosition"
private const val ARG_PARAM_TENANCY_ID = "tenancyId"
private const val ARG_PARAM_HOUSE_POSITION = "housePosition"
private const val ARG_PARAM_HOUSE_ID = "houseId"
private const val ARG_PARAM_PAYMENT_POSITION = "paymentPosition"
private const val ARG_PARAM_PAYMENT_ID = "paymentId"

class EditHouseTenancyPaymentFragment : Fragment(),
    VariableUtilitiesPaymentRVA.VariableCostUpdatedListener,
    FixedUtilitiesPaymentRVA.FixedCostUpdatedListener,
    OtherAdjustmentsRVA.OtherAdjustmentProvidedListener, View.OnClickListener,
    CompoundButton.OnCheckedChangeListener, View.OnFocusChangeListener {
    private var mParamTenancyPosition: Int = -1
    private var mParamTenancyID: String? = null
    private var mParamHousePosition: Int = -1
    private var mParamHouseID: String? = null
    private var mParamPaymentPosition: Int = -1
    private var mParamPaymentID: String? = null

    private lateinit var mPayableAmount: TextView

    private lateinit var mVariableUtilitiesRecyclerView: RecyclerView
    private lateinit var mVariableUtilitiesRecyclerViewAdapter: VariableUtilitiesPaymentRVA

    private lateinit var mFixedUtilitiesRecyclerView: RecyclerView
    private lateinit var mFixedUtilitiesRecyclerViewAdapter: FixedUtilitiesPaymentRVA

    private lateinit var mOtherAdjustmentsRecyclerView: RecyclerView
    private lateinit var mOtherAdjustmentsRecyclerViewAdapter: OtherAdjustmentsRVA

    private lateinit var mAmountPaidTil: TextInputLayout
    private lateinit var mAmountReceivedEt: TextInputEditText

    private lateinit var mPaymentNoteTil: TextInputLayout
    private lateinit var mPaymentNoteEt: TextInputEditText

    private lateinit var mEditPaymentBtn: MaterialButton
    private lateinit var mViewModel: HouseViewModel

    private lateinit var mAdvanceAndDueWrapper: View
    private lateinit var mAdjustAdvanceCB: CheckBox
    private lateinit var mIncludeDueCB: CheckBox

    private lateinit var mPaymentMadeForTil: TextInputLayout
    private lateinit var mPaymentMadeForCV: CalendarView

    private lateinit var mTenancy: Tenancy
    private var mPaymentDetails: ArrayList<PaymentDetail> = ArrayList()

    private lateinit var mVariableUtilitiesWrapper: View
    private lateinit var mFixedUtilitiesWrapper: View
    private lateinit var mOtherAdjustmentsWrapper: View

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
            mParamPaymentPosition = it.getInt(ARG_PARAM_PAYMENT_POSITION)
            mParamPaymentID = it.getString(ARG_PARAM_PAYMENT_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_house_tenancy_payment, container, false)
        mContext = requireContext()
        mPayableAmount = view.findViewById(R.id.payableAmount)
        mVariableUtilitiesRecyclerView = view.findViewById(R.id.variableUtilitiesRV)
        mVariableUtilitiesRecyclerView.layoutManager = GridLayoutManager(mContext, 2)
        mVariableUtilitiesRecyclerViewAdapter = VariableUtilitiesPaymentRVA()
        mVariableUtilitiesRecyclerViewAdapter.setOnVariableCostUpdatedListener(this)
        mVariableUtilitiesRecyclerView.adapter = mVariableUtilitiesRecyclerViewAdapter

        mFixedUtilitiesRecyclerView = view.findViewById(R.id.fixedUtilitiesRV)
        mFixedUtilitiesRecyclerView.layoutManager = GridLayoutManager(mContext, 2)
        mFixedUtilitiesRecyclerViewAdapter = FixedUtilitiesPaymentRVA()
        mFixedUtilitiesRecyclerViewAdapter.setOnVariableCostUpdatedListener(this)
        mFixedUtilitiesRecyclerView.adapter = mFixedUtilitiesRecyclerViewAdapter

        mOtherAdjustmentsRecyclerView = view.findViewById(R.id.otherAdjustmentsRV)
        mOtherAdjustmentsRecyclerView.layoutManager = GridLayoutManager(mContext, 2)
        mOtherAdjustmentsRecyclerViewAdapter = OtherAdjustmentsRVA()
        mOtherAdjustmentsRecyclerViewAdapter.setOnOtherAdjustmentsProvidedListener(this)
        mOtherAdjustmentsRecyclerView.adapter = mOtherAdjustmentsRecyclerViewAdapter

        mAdvanceAndDueWrapper = view.findViewById(R.id.advanceAndDueWrapper)
        mAdjustAdvanceCB = view.findViewById(R.id.adjustAdvanceCB)
        mAdjustAdvanceCB.setOnCheckedChangeListener(this)
        mIncludeDueCB = view.findViewById(R.id.includeDueCB)
        mIncludeDueCB.setOnCheckedChangeListener(this)

        mAmountPaidTil = view.findViewById(R.id.amountPaidTil)
        mAmountReceivedEt = view.findViewById(R.id.amountPaidEt)
        mAmountReceivedEt.onFocusChangeListener = this

        mPaymentNoteTil = view.findViewById(R.id.paymentNoteTil)
        mPaymentNoteEt = view.findViewById(R.id.paymentNoteEt)

        mPaymentNoteTil = view.findViewById(R.id.paymentNoteTil)
        mPaymentNoteEt = view.findViewById(R.id.paymentNoteEt)

        mEditPaymentBtn = view.findViewById(R.id.editPaymentBtn)
        mEditPaymentBtn.setOnClickListener(this)

        mVariableUtilitiesWrapper = view.findViewById(R.id.variableUtilitiesWrapper)
        mFixedUtilitiesWrapper = view.findViewById(R.id.fixedUtilitiesWrapper)
        mOtherAdjustmentsWrapper = view.findViewById(R.id.otherAdjustmentsWrapper)

        mPaymentMadeForTil = view.findViewById(R.id.paymentMadeForTil)
        mPaymentMadeForCV = view.findViewById(R.id.paymentMadeForCV)
        mPaymentMadeForCV.setOnDateChangeListener { _, year, month, dayOfMonth ->
            mPaymentMadeForTil.isErrorEnabled = false
            mPaymentMadeForDate = Calendar.getInstance().let {
                it.set(year, month, dayOfMonth)
                it.time
            }
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel = ViewModelProvider(requireActivity()).get(HouseViewModel::class.java)

        mViewModel.getHouses().observe(requireActivity()) {
            if (isAdded) {
                if (it != null && mParamHousePosition != -1 && mParamTenancyPosition != -1 && mParamPaymentPosition != -1 && mParamPaymentPosition < it[mParamHousePosition].tenancies!![mParamTenancyPosition].payments!!.size && it[mParamHousePosition].id == mParamHouseID && it[mParamHousePosition].tenancies!![mParamTenancyPosition].id == mParamTenancyID && it[mParamHousePosition].tenancies!![mParamTenancyPosition].payments!![mParamPaymentPosition].id == mParamPaymentID) {
                    mTenancy = it[mParamHousePosition].tenancies!![mParamTenancyPosition]
                    val payment = mViewModel.getPayment(
                        mParamHousePosition,
                        mParamTenancyPosition,
                        mParamPaymentPosition
                    )
                    updateViewElements(payment)
                } else {
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        }
    }

    private fun updateViewElements(payment: Payment) {
        val variableUtilities: ArrayList<PaymentDetail> = ArrayList()
        val fixedUtilities: ArrayList<PaymentDetail> = ArrayList()
        val otherAdjustments: ArrayList<PaymentDetail> = ArrayList()

        payment.paymentDetails!!.forEach { paymentDetail ->
            if (paymentDetail.isVariableCost!!)
                variableUtilities.add(paymentDetail)
            else if (!paymentDetail.isVariableCost!! && !(paymentDetail.name == "RENT" || paymentDetail.name == "ADVANCE_ADJUSTED") || paymentDetail.name == "DUE_INCLUDED") {
                fixedUtilities.add(paymentDetail)
            } else if (paymentDetail.name == "ADVANCE_ADJUSTED" || paymentDetail.name == "DUE_INCLUDED") {
                otherAdjustments.add(paymentDetail)
            }
        }

        if (variableUtilities.size == 0) mVariableUtilitiesWrapper.visibility =
            View.GONE else mVariableUtilitiesWrapper.visibility = View.VISIBLE
        if (fixedUtilities.size == 0) mFixedUtilitiesWrapper.visibility =
            View.GONE else mFixedUtilitiesWrapper.visibility = View.VISIBLE
        if (otherAdjustments.size == 0) mOtherAdjustmentsWrapper.visibility =
            View.GONE else mOtherAdjustmentsWrapper.visibility = View.VISIBLE


        val newVariableUtilities: ArrayList<Utility> = ArrayList()
        val newFixedUtilities: ArrayList<Utility> = ArrayList()
        mTenancy.utilities!!.forEach {
            if (it.isVariableCost!! && variableUtilities.indexOfFirst { pd -> pd.name == it.name } == -1) {
                newVariableUtilities.add(it)
            } else if (!it.isVariableCost!! && fixedUtilities.indexOfFirst { pd -> pd.name == it.name } == -1) {
                newFixedUtilities.add(it)
            }
        }

        mVariableUtilitiesRecyclerViewAdapter.setPaymentDetails(
            variableUtilities,
            newVariableUtilities
        )
        mFixedUtilitiesRecyclerViewAdapter.setPaymentDetails(fixedUtilities, newFixedUtilities)
        mOtherAdjustmentsRecyclerViewAdapter.setOtherAdjustments(otherAdjustments)

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

        mPaymentDetails = payment.paymentDetails!!
        mAmountReceivedEt.setText(payment.amountReceived!!.toString())
        mPaymentNoteEt.setText(payment.note)
        mPayableAmount.text = calculatePayableAmount().toString()
        mPaymentMadeForDate = SimpleDateFormat(
            yyyy_MM_dd,
            Locale.ENGLISH
        ).parse(
            "${payment.paidForYear}-${payment.paiForMonth}-${
                Calendar.getInstance(Locale.ENGLISH).apply { time = mTenancy.startDate!! }
                    .get(Calendar.DATE)
            }"
        )
        mPaymentMadeForCV.date = mPaymentMadeForDate!!.time

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
            notApprovedUtilitiesMessage.append(" are still not approved by tenant.\nDo you still want to update payment")
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
            if (it.isVariableCost!!) {
                payableAmount += it.units!! * it.price!!
            } else {
                payableAmount += it.price!!
            }
        }
        return payableAmount
    }

    companion object {
        @JvmStatic
        fun newInstance(
            housePosition: Int,
            houseId: String,
            tenancyPosition: Int,
            tenancyId: String,
            paymentPosition: Int,
            paymentId: String
        ) =
            EditHouseTenancyPaymentFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM_HOUSE_POSITION, housePosition)
                    putString(ARG_PARAM_HOUSE_ID, houseId)
                    putInt(ARG_PARAM_TENANCY_POSITION, tenancyPosition)
                    putString(ARG_PARAM_TENANCY_ID, tenancyId)
                    putInt(ARG_PARAM_PAYMENT_POSITION, paymentPosition)
                    putString(ARG_PARAM_PAYMENT_ID, paymentId)
                }
            }
    }

    override fun onVariableCostUpdated(utility: Utility, value: Int) {
        val index = mPaymentDetails.indexOfFirst { it.name == utility.name }
        if (index == -1 && value != 0) {
            mPaymentDetails.add(PaymentDetail().apply {
                name = utility.name
                isVariableCost = utility.isVariableCost
                price = utility.price
                units = value
            })
        } else if (value > 0) {
            mPaymentDetails[index].units = value
        } else {
            mPaymentDetails =
                mPaymentDetails.filter { it.name != utility.name } as ArrayList<PaymentDetail>
        }
        mPayableAmount.text = calculatePayableAmount().toString()
    }

    private fun validateAndConfirm() {
        var isValid = true

        mAmountReceivedEt.text?.let {
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
        }

        if (isValid) {
            val ignoredUtilities: ArrayList<Utility> = mTenancy.utilities!!.filter { utility ->
                mPaymentDetails.indexOfFirst { it.name == utility.name } == -1
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
                        updatePayment()
                    }
                    setNegativeButton(R.string.cancel) { _, _ ->
                        //Do nothing
                    }
                    show()
                }
            } else updatePayment()
        } else {
            VibrateView.vibrate(
                mContext,
                R.anim.shake,
                requireView().findViewById(R.id.formCardView)
            )
        }
    }

    override fun onFixedCostUpdated(utility: Utility, toBeIncluded: Boolean) {
        val index = mPaymentDetails.indexOfFirst { it.name == utility.name }
        if (index == -1 && toBeIncluded) {
            mPaymentDetails.add(PaymentDetail().apply {
                name = utility.name
                isVariableCost = utility.isVariableCost
                price = utility.price
            })
        } else if (!toBeIncluded) {
            mPaymentDetails =
                mPaymentDetails.filter { it.name != utility.name } as ArrayList<PaymentDetail>
        }
        mPayableAmount.text = calculatePayableAmount().toString()
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                mEditPaymentBtn.id -> {
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
                            mPaymentDetails.indexOfFirst { it.name.equals("NEW_ADVANCE_ADJUSTED") }
                        if (index == -1 && isChecked) {
                            mPaymentDetails.add(PaymentDetail().apply {
                                name = "NEW_ADVANCE_ADJUSTED"
                                isVariableCost = false
                                price = -(mTenancy.advanceAmount!!)
                            })
                        } else if (!isChecked) {
                            mPaymentDetails =
                                mPaymentDetails.filter { !it.name.equals("NEW_ADVANCE_ADJUSTED") } as ArrayList<PaymentDetail>
                        }
                        mPayableAmount.text = calculatePayableAmount().toString()
                    }
                }

                mIncludeDueCB.id -> {
                    if (mTenancy.dueAmount!! > 0) {
                        val index =
                            mPaymentDetails.indexOfFirst { it.name.equals("NEW_DUE_INCLUDED") }
                        if (index == -1 && isChecked) {
                            mPaymentDetails.add(PaymentDetail().apply {
                                name = "NEW_DUE_INCLUDED"
                                isVariableCost = false
                                price = mTenancy.dueAmount!!
                            })
                        } else if (!isChecked) {
                            mPaymentDetails =
                                mPaymentDetails.filter { !it.name.equals("NEW_DUE_INCLUDED") } as ArrayList<PaymentDetail>
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
        val indexOfAdvance = mPaymentDetails.indexOfFirst { it.name == "ADVANCE_ADJUSTED" }
        val indexOfNewAdvance = mPaymentDetails.indexOfFirst { it.name == "NEW_ADVANCE_ADJUSTED" }

        if (indexOfAdvance > -1 && indexOfNewAdvance > -1) {
            mPaymentDetails[indexOfAdvance].price =
                mPaymentDetails[indexOfAdvance].price!! + mPaymentDetails[indexOfNewAdvance].price!!
            mPaymentDetails.removeAt(indexOfNewAdvance)
        } else if (indexOfNewAdvance > -1) {
            mPaymentDetails[indexOfNewAdvance].name = "ADVANCE_ADJUSTED"
        }

        val indexOfDue = mPaymentDetails.indexOfFirst { it.name == "DUE_INCLUDED" }
        val indexOfNewDue = mPaymentDetails.indexOfFirst { it.name == "NEW_DUE_INCLUDED" }

        if (indexOfDue > -1 && indexOfNewDue > -1) {
            mPaymentDetails[indexOfDue].price =
                mPaymentDetails[indexOfDue].price!! + mPaymentDetails[indexOfNewDue].price!!
            mPaymentDetails.removeAt(indexOfNewDue)
        } else if (indexOfNewDue > -1) {
            mPaymentDetails[indexOfNewDue].name = "DUE_INCLUDED"
        }

        return Payment().apply {
            amountPayable = calculatePayableAmount()
            amountReceived = mAmountReceivedEt.text!!.trim().toString().toInt()
            paiForMonth = paymentMadeForDate.get(Calendar.MONTH) + 1
            paidForYear = paymentMadeForDate.get(Calendar.YEAR)
            paymentDetails = mPaymentDetails
            mPaymentNoteEt.text?.let {
                if (it.trim().toString().isNotEmpty()) note = it.trim().toString()
            }
        }
    }

    private fun updatePayment(payment: Payment = getPaymentObject()) {
        val requestBody =
            RequestBody.create(MediaType.parse(TYPE_JSON), payment.getJSONObject().toString())

        val consumer = APIService.getService(APIConsumer::class.java)
        val updatePayment = consumer.updatePayment(
            UserToken.getInstance(mContext).token!!,
            mParamHouseID!!,
            mParamTenancyID!!,
            mParamPaymentID!!,
            requestBody
        )
        updatePayment.enqueue(object : Callback<ResponseBody> {
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
                        "Payment has been updated successfully",
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
                        displayAPIErrorResponseDialog("Unable to update payment details. Please try again later")
                    }
                } else {
                    displayAPIErrorResponseDialog("Unable to update payment details. Please try again later")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                displayAPIErrorResponseDialog("Unable to update payment details. Please try again later")
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
                mAmountReceivedEt.id ->
                    if (hasFocus) {
                        mAmountPaidTil.isErrorEnabled = false
                    } else {
                        mAmountReceivedEt.text?.let {
                            if (it.trim().isEmpty()) {
                                mAmountPaidTil.apply {
                                    isErrorEnabled = true
                                    error = "Amount received is required"
                                }
                                VibrateView.vibrate(
                                    mContext,
                                    R.anim.shake,
                                    mAmountPaidTil
                                )
                            }
                        }
                    }
            }
        }
    }

    override fun onOtherAdjustmentProvided(utility: Utility, toBeIncluded: Boolean) {
        val index = mPaymentDetails.indexOfFirst { it.name == utility.name }
        if (index == -1) {
            if (toBeIncluded) {
                mPaymentDetails.add(PaymentDetail().apply {
                    name = utility.name
                    isVariableCost = utility.isVariableCost
                    price = utility.price
                })
            } else {
                mPaymentDetails =
                    mPaymentDetails.filter { it.name != utility.name } as ArrayList<PaymentDetail>
            }
        } else {
            mPaymentDetails =
                mPaymentDetails.filter { it.name != utility.name } as ArrayList<PaymentDetail>
        }
        mPayableAmount.text = calculatePayableAmount().toString()
    }
}