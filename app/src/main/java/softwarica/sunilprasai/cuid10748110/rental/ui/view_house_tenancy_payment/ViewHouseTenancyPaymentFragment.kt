package softwarica.sunilprasai.cuid10748110.rental.ui.view_house_tenancy_payment

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
import softwarica.sunilprasai.cuid10748110.rental.utils.*
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "ViewPaymentFragment"
private const val ARG_PARAM_TENANCY_POSITION = "tenancyPosition"
private const val ARG_PARAM_TENANCY_ID = "tenancyId"
private const val ARG_PARAM_HOUSE_POSITION = "housePosition"
private const val ARG_PARAM_HOUSE_ID = "houseId"
private const val ARG_PARAM_PAYMENT_POSITION = "paymentPosition"
private const val ARG_PARAM_PAYMENT_ID = "paymentId"

class ViewHouseTenancyPaymentFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {
    private var mParamTenancyPosition: Int = -1
    private var mParamTenancyID: String? = null
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

    private lateinit var mViewModel: HouseViewModel
    private lateinit var mContext: Context

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
        val view = inflater.inflate(R.layout.fragment_view_house_tenancy_payment, container, false)
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
        mRecyclerViewAdapter.setPaymentDetails(payment.paymentDetails!!)
        mAmountPayableTxt.text = payment.amountPayable.toString()
        mAmountReceivedTxt.text = payment.amountReceived.toString()
        mPaymentDateTxt.text =
            SimpleDateFormat(dd_MMMMM_yyyy, Locale.ENGLISH).format(
                SimpleDateFormat(
                    yyyy_MM_dd, Locale.ENGLISH
                ).parse(
                    "${payment.paidForYear}-${payment.paiForMonth}-${
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
            ViewHouseTenancyPaymentFragment().apply {
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

    override fun onRefresh() {
        mSwipeRefreshLayout.isRefreshing = true
        val consumer = APIService.getService(APIConsumer::class.java)
        val getTenancy = consumer.getTenancy(
            UserToken.getInstance(mContext).token!!,
            mParamHouseID!!,
            mParamTenancyID!!
        )
        getTenancy.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val apiFetchedTenancy = Tenancy(JSONObject(response.body()!!.string()))
                    mViewModel.replaceTenancy(
                        mParamHousePosition,
                        mParamTenancyPosition,
                        apiFetchedTenancy
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
                        displayAPIErrorResponseDialog("Unable to update payment details. Please try again later")
                    }
                } else {
                    displayAPIErrorResponseDialog("Unable to update payment details. Please try again later")
                }
                mSwipeRefreshLayout.isRefreshing = false
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                displayAPIErrorResponseDialog("Unable to update payment details. Please try again later")
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