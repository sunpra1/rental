package softwarica.sunilprasai.cuid10748110.rental.ui.view_house_tenancy_history_payment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import softwarica.sunilprasai.cuid10748110.rental.R
import softwarica.sunilprasai.cuid10748110.rental.model.Payment
import softwarica.sunilprasai.cuid10748110.rental.model.Tenancy
import softwarica.sunilprasai.cuid10748110.rental.ui.shared_view_model.HouseViewModel
import softwarica.sunilprasai.cuid10748110.rental.ui.view_tenant_tenancy_history_payment.PaymentDetailsRVA
import softwarica.sunilprasai.cuid10748110.rental.utils.*
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "ViewPaymentFragment"
private const val ARG_PARAM_TENANCY_HISTORY_POSITION = "tenancyHistoryPosition"
private const val ARG_PARAM_TENANCY_HISTORY_ID = "tenancyHistoryId"
private const val ARG_PARAM_HOUSE_POSITION = "housePosition"
private const val ARG_PARAM_HOUSE_ID = "houseId"
private const val ARG_PARAM_PAYMENT_POSITION = "paymentPosition"
private const val ARG_PARAM_PAYMENT_ID = "paymentId"

class ViewHouseTenancyHistoryPaymentFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {
    private var mParamTenancyHistoryPosition: Int = -1
    private var mParamTenancyHistoryID: String? = null
    private var mParamHousePosition: Int = -1
    private var mParamHouseID: String? = null
    private var mParamPaymentPosition: Int = -1
    private var mParamPaymentID: String? = null

    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mRecyclerViewAdapter: PaymentDetailsRVA
    private lateinit var mAmountPayableTxt: TextView
    private lateinit var mAmountReceivedTxt: TextView
    private lateinit var mPaymentDateTxt: TextView

    private lateinit var mTenancy: Tenancy
    private lateinit var mPayment: Payment
    private lateinit var mViewModel: HouseViewModel
    private lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mParamHousePosition = it.getInt(ARG_PARAM_HOUSE_POSITION)
            mParamHouseID = it.getString(ARG_PARAM_HOUSE_ID)
            mParamTenancyHistoryPosition = it.getInt(ARG_PARAM_TENANCY_HISTORY_POSITION)
            mParamTenancyHistoryID = it.getString(ARG_PARAM_TENANCY_HISTORY_ID)
            mParamPaymentPosition = it.getInt(ARG_PARAM_PAYMENT_POSITION)
            mParamPaymentID = it.getString(ARG_PARAM_PAYMENT_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            inflater.inflate(R.layout.fragment_view_house_tenancy_history_payment, container, false)
        mContext = requireContext()
        mRecyclerView = view.findViewById(R.id.paymentDetailRV)

        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        mSwipeRefreshLayout.setOnRefreshListener(this)

        mRecyclerView.layoutManager = LinearLayoutManager(mContext)
        mRecyclerViewAdapter = PaymentDetailsRVA()
        mRecyclerView.adapter = mRecyclerViewAdapter

        mAmountPayableTxt = view.findViewById(R.id.payableAmount)
        mAmountReceivedTxt = view.findViewById(R.id.receivedAmount)
        mPaymentDateTxt = view.findViewById(R.id.paymentDate)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel = ViewModelProvider(requireActivity()).get(HouseViewModel::class.java)

        mViewModel.getHouses().observe(requireActivity()) {
            if (isAdded) {
                if (it != null && mParamHousePosition != -1 && mParamTenancyHistoryPosition != -1 && mParamPaymentPosition != -1 && mParamPaymentPosition < it[mParamHousePosition].tenanciesHistory!![mParamTenancyHistoryPosition].payments!!.size && it[mParamHousePosition].id == mParamHouseID && it[mParamHousePosition].tenanciesHistory!![mParamTenancyHistoryPosition].id == mParamTenancyHistoryID && it[mParamHousePosition].tenanciesHistory!![mParamTenancyHistoryPosition].payments!![mParamPaymentPosition].id == mParamPaymentID) {
                    mTenancy = mViewModel.getTenancyHistory(
                        mParamHousePosition,
                        mParamTenancyHistoryPosition
                    )
                    mPayment = mViewModel.getHouseTenancyHistoryPayment(
                        mParamHousePosition,
                        mParamTenancyHistoryPosition,
                        mParamPaymentPosition
                    )
                    updateViewElements()
                } else {
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        }
    }

    private fun updateViewElements() {
        mRecyclerViewAdapter.setPaymentDetails(mPayment.paymentDetails!!)
        mAmountPayableTxt.text = mPayment.amountPayable.toString()
        mAmountReceivedTxt.text = mPayment.amountReceived.toString()
        mPaymentDateTxt.text =
            SimpleDateFormat(dd_MMMMM_yyyy, Locale.ENGLISH).format(
                SimpleDateFormat(
                    yyyy_MM_dd, Locale.ENGLISH
                ).parse(
                    "${mPayment.paidForYear}-${mPayment.paiForMonth}-${
                        Calendar.getInstance().apply {
                            time = mTenancy.startDate!!
                        }.get(Calendar.DAY_OF_MONTH)
                    }"
                )!!
            )
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
            ViewHouseTenancyHistoryPaymentFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM_HOUSE_POSITION, housePosition)
                    putString(ARG_PARAM_HOUSE_ID, houseId)
                    putInt(ARG_PARAM_TENANCY_HISTORY_POSITION, tenancyPosition)
                    putString(ARG_PARAM_TENANCY_HISTORY_ID, tenancyId)
                    putInt(ARG_PARAM_PAYMENT_POSITION, paymentPosition)
                    putString(ARG_PARAM_PAYMENT_ID, paymentId)
                }
            }
    }

    override fun onRefresh() {
        mSwipeRefreshLayout.isRefreshing = true
        val consumer = APIService.getService(APIConsumer::class.java)
        val getTenancyHistory = consumer.getTenancyHistory(
            UserToken.getInstance(mContext).token!!,
            mParamHouseID!!,
            mParamTenancyHistoryID!!
        )
        getTenancyHistory.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val apiFetchedTenancyHistory = Tenancy(JSONObject(response.body()!!.string()))
                    mViewModel.replaceTenancyHistory(
                        mParamHousePosition,
                        mParamTenancyHistoryPosition,
                        apiFetchedTenancyHistory
                    )
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
                        displayAPIErrorResponseDialog("Unable to update payment history. Please try again later")
                    }
                } else {
                    displayAPIErrorResponseDialog("Unable to update payment history. Please try again later")
                }
                mSwipeRefreshLayout.isRefreshing = false
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                displayAPIErrorResponseDialog("Unable to update payment history. Please try again later")
                mSwipeRefreshLayout.isRefreshing = false
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
}