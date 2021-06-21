package softwarica.sunilprasai.cuid10748110.rental.ui.view_house_tenancy_history

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.button.MaterialButton
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import softwarica.sunilprasai.cuid10748110.rental.R
import softwarica.sunilprasai.cuid10748110.rental.model.Tenancy
import softwarica.sunilprasai.cuid10748110.rental.ui.shared_view_model.HouseViewModel
import softwarica.sunilprasai.cuid10748110.rental.ui.view_house_tenancy_history_payment.ViewHouseTenancyHistoryPaymentFragment
import softwarica.sunilprasai.cuid10748110.rental.utils.*
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_PARAM_TENANCY_HISTORY_POSITION = "tenancyHistoryPosition"
private const val ARG_PARAM_TENANCY_HISTORY_ID = "tenancyHistoryId"
private const val ARG_PARAM_HOUSE_POSITION = "housePosition"
private const val ARG_PARAM_HOUSE_ID = "houseId"

class ViewHouseTenancyHistoryFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener,
    View.OnClickListener, PaymentsRVItemTouchListener.OnPaymentTouch {
    private var mParamTenancyHistoryPosition: Int = -1
    private var mParamTenancyHistoryID: String? = null
    private var mParamHousePosition: Int = -1
    private var mParamHouseID: String? = null

    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout

    private lateinit var mTenantImage: ImageView
    private lateinit var mTenantName: TextView
    private lateinit var mTenancyDetails: TextView
    private lateinit var mTenancyInfo: TextView
    private lateinit var mTenancyAdvanceAmount: TextView
    private lateinit var mTenancyDueAmount: TextView
    private lateinit var mTenancyStartDate: TextView
    private lateinit var mTenancyEndDate: TextView
    private lateinit var mTenancyRoomType: TextView

    private lateinit var mTenancyImage: ImageView

    private lateinit var mPreviousImageBtn: TextView
    private lateinit var mNextImageBtn: TextView

    private lateinit var mViewUtilitiesOrPaymentBtn: MaterialButton

    private lateinit var mRVTitle: TextView
    private lateinit var mRVNoItemsInfo: TextView
    private lateinit var mUtilitiesRecyclerView: RecyclerView
    private lateinit var mPaymentsRecyclerView: RecyclerView
    private lateinit var mUtilitiesRecyclerViewAdapter: UtilitiesRVA
    private lateinit var mPaymentsRecyclerViewAdapter: PaymentsRVA

    private lateinit var mViewModel: HouseViewModel
    private lateinit var mTenancy: Tenancy
    private var mIsUtilityListVisible: Boolean = false
    private var mCurrentImagePosition = 0
    private lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mParamHousePosition = it.getInt(ARG_PARAM_HOUSE_POSITION)
            mParamHouseID = it.getString(ARG_PARAM_HOUSE_ID)
            mParamTenancyHistoryPosition = it.getInt(ARG_PARAM_TENANCY_HISTORY_POSITION)
            mParamTenancyHistoryID = it.getString(ARG_PARAM_TENANCY_HISTORY_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_house_tenancy_history, container, false)
        mContext = requireContext()
        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        mSwipeRefreshLayout.setOnRefreshListener(this)

        mTenantImage = view.findViewById(R.id.tenantImage)
        mTenantName = view.findViewById(R.id.tenantName)
        mTenancyDetails = view.findViewById(R.id.tenancyDetails)
        mTenancyInfo = view.findViewById(R.id.tenancyInfo)
        mTenancyAdvanceAmount = view.findViewById(R.id.tenancyAdvanceAmount)
        mTenancyDueAmount = view.findViewById(R.id.tenancyDueAmount)
        mTenancyStartDate = view.findViewById(R.id.tenancyStartDate)
        mTenancyEndDate = view.findViewById(R.id.tenancyEndDate)
        mTenancyRoomType = view.findViewById(R.id.tenancyRoomType)

        mTenancyImage = view.findViewById(R.id.tenancyImage)

        mPreviousImageBtn = view.findViewById(R.id.previousImageBtn)
        mPreviousImageBtn.setOnClickListener(this)
        mNextImageBtn = view.findViewById(R.id.nextImageBtn)
        mNextImageBtn.setOnClickListener(this)

        mRVTitle = view.findViewById(R.id.rvTitle)
        mRVNoItemsInfo = view.findViewById(R.id.rvNoItemsInfo)
        mUtilitiesRecyclerView = view.findViewById(R.id.utilitiesRecyclerView)
        mUtilitiesRecyclerView.layoutManager = LinearLayoutManager(mContext)
        mUtilitiesRecyclerViewAdapter = UtilitiesRVA()
        mUtilitiesRecyclerView.adapter = mUtilitiesRecyclerViewAdapter

        mPaymentsRecyclerView = view.findViewById(R.id.paymentsRecyclerView)
        mPaymentsRecyclerView.layoutManager = LinearLayoutManager(mContext)
        mPaymentsRecyclerViewAdapter = PaymentsRVA()
        mPaymentsRecyclerView.adapter = mPaymentsRecyclerViewAdapter
        mPaymentsRecyclerView.addOnItemTouchListener(
            PaymentsRVItemTouchListener(
                mContext,
                mPaymentsRecyclerView,
                this
            )
        )

        mViewUtilitiesOrPaymentBtn = view.findViewById(R.id.viewUtilitiesOrPaymentsBtn)
        mViewUtilitiesOrPaymentBtn.setOnClickListener(this)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel =
            ViewModelProvider(requireActivity()).get(HouseViewModel::class.java)

        mViewModel.getHouses().observe(requireActivity()) {
            if (isAdded) {
                if (it != null && mParamHousePosition != -1 && mParamTenancyHistoryPosition != -1 && mParamTenancyHistoryPosition < it[mParamHousePosition].tenanciesHistory!!.size && it[mParamHousePosition].id == mParamHouseID && it[mParamHousePosition].tenanciesHistory!![mParamTenancyHistoryPosition].id == mParamTenancyHistoryID) {
                    mTenancy =
                        it[mParamHousePosition].tenanciesHistory!![mParamTenancyHistoryPosition]
                    updateView()
                } else {
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        }
    }

    private fun updateView() {
        mTenancy.tenant!!.image?.let {
            LoadImage(object : LoadImage.ImageLoader {
                override fun onImageLoaded(imageBitmap: Bitmap?) {
                    mTenantImage.setImageBitmap(imageBitmap)
                }

            }).execute(it.buffer)
        }

        mTenantName.text = mTenancy.tenant!!.fullName
        mTenancy.images!!.let {
            if (it.size > 0) {
                LoadImage(object : LoadImage.ImageLoader {
                    override fun onImageLoaded(imageBitmap: Bitmap?) {
                        mTenancyImage.setImageBitmap(imageBitmap)
                    }
                }).execute(it[0].buffer)
            }
        }

        var totalAmountPaid = 0
        mTenancy.payments!!.forEach {
            totalAmountPaid += it.amountReceived!!
        }
        mTenancyInfo.text =
            resources.getString(R.string.tenancy_history_details_format)
                .format(totalAmountPaid)

        mTenancyAdvanceAmount.text = mTenancy.advanceAmount.toString()
        mTenancyDueAmount.text = mTenancy.dueAmount.toString()
        mTenancyRoomType.text = mTenancy.roomType.toString()
        mTenancyStartDate.text =
            SimpleDateFormat(yyyy_MM_dd, Locale.ENGLISH).format(mTenancy.startDate!!)
        mTenancyEndDate.text =
            SimpleDateFormat(yyyy_MM_dd, Locale.ENGLISH).format(mTenancy.endDate!!)

        mUtilitiesRecyclerViewAdapter.setUtilities(mTenancy.utilities!!)
        mPaymentsRecyclerViewAdapter.setPayments(mTenancy.payments!!)

        mViewUtilitiesOrPaymentBtn.text =
            if (mIsUtilityListVisible) resources.getString(R.string.view_payments) else resources.getString(
                R.string.view_utilities
            )
        if (mIsUtilityListVisible) {
            mRVTitle.text = resources.getString(R.string.utilities)
            mPaymentsRecyclerView.visibility = View.GONE
            mUtilitiesRecyclerView.visibility = View.VISIBLE
            if (mTenancy.utilities!!.size == 0) {
                mRVNoItemsInfo.visibility = View.VISIBLE
                mRVNoItemsInfo.text = resources.getString(R.string.no_utility_yet)
            } else {
                mRVNoItemsInfo.visibility = View.GONE
            }
        } else {
            mRVTitle.text = resources.getString(R.string.payments)
            mUtilitiesRecyclerView.visibility = View.GONE
            mPaymentsRecyclerView.visibility = View.VISIBLE
            if (mTenancy.payments!!.size == 0) {
                mRVNoItemsInfo.visibility = View.VISIBLE
                mRVNoItemsInfo.text = resources.getString(R.string.no_payment_yet)
            } else {
                mRVNoItemsInfo.visibility = View.GONE
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(
            housePosition: Int,
            houseId: String,
            tenancyPosition: Int,
            tenancyId: String
        ) =
            ViewHouseTenancyHistoryFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM_HOUSE_POSITION, housePosition)
                    putString(ARG_PARAM_HOUSE_ID, houseId)
                    putInt(ARG_PARAM_TENANCY_HISTORY_POSITION, tenancyPosition)
                    putString(ARG_PARAM_TENANCY_HISTORY_ID, tenancyId)
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
                        displayAPIErrorResponseDialog("Unable to update tenancy history details. Please try again later")
                    }
                } else {
                    displayAPIErrorResponseDialog("Unable to update tenancy history details. Please try again later")
                }
                mSwipeRefreshLayout.isRefreshing = false
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                displayAPIErrorResponseDialog("Unable to update tenancy history details. Please try again later")
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

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                mPreviousImageBtn.id -> {
                    if (mTenancy.images!!.size > 0) {
                        if (mCurrentImagePosition == 0) {
                            mCurrentImagePosition = mTenancy.images!!.size - 1
                        } else {
                            mCurrentImagePosition--
                        }
                        LoadImage(object : LoadImage.ImageLoader {
                            override fun onImageLoaded(imageBitmap: Bitmap?) {
                                mTenancyImage.setImageBitmap(imageBitmap)
                            }
                        }).execute(mTenancy.images!![mCurrentImagePosition].buffer)
                    }
                }

                mNextImageBtn.id -> {
                    if (mTenancy.images!!.size > 0) {
                        if (mCurrentImagePosition == mTenancy.images!!.size - 1) {
                            mCurrentImagePosition = 0
                        } else {
                            mCurrentImagePosition++
                        }
                        LoadImage(object : LoadImage.ImageLoader {
                            override fun onImageLoaded(imageBitmap: Bitmap?) {
                                mTenancyImage.setImageBitmap(imageBitmap)
                            }
                        }).execute(mTenancy.images!![mCurrentImagePosition].buffer)
                    }
                }

                mViewUtilitiesOrPaymentBtn.id -> {
                    mIsUtilityListVisible = !mIsUtilityListVisible
                    mViewUtilitiesOrPaymentBtn.text =
                        if (mIsUtilityListVisible) resources.getString(R.string.view_payments) else resources.getString(
                            R.string.view_utilities
                        )
                    if (mIsUtilityListVisible) {
                        mRVTitle.text = resources.getString(R.string.utilities)
                        mPaymentsRecyclerView.visibility = View.GONE
                        mUtilitiesRecyclerView.visibility = View.VISIBLE
                        if (mTenancy.utilities!!.size == 0) {
                            mRVNoItemsInfo.visibility = View.VISIBLE
                            mRVNoItemsInfo.text = resources.getString(R.string.no_utility_yet)
                        } else {
                            mRVNoItemsInfo.visibility = View.GONE
                        }
                    } else {
                        mRVTitle.text = resources.getString(R.string.payments)
                        mUtilitiesRecyclerView.visibility = View.GONE
                        mPaymentsRecyclerView.visibility = View.VISIBLE
                        if (mTenancy.payments!!.size == 0) {
                            mRVNoItemsInfo.visibility = View.VISIBLE
                            mRVNoItemsInfo.text = resources.getString(R.string.no_payment_yet)
                        } else {
                            mRVNoItemsInfo.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    override fun onPaymentClick(position: Int) {
        requireActivity().supportFragmentManager
            .beginTransaction()
            .addToBackStack(ViewHouseTenancyHistoryPaymentFragment::class.java.simpleName)
            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right)
            .replace(
                R.id.fragment_holder_two,
                ViewHouseTenancyHistoryPaymentFragment.newInstance(
                    mParamHousePosition,
                    mParamHouseID!!,
                    mParamTenancyHistoryPosition,
                    mParamTenancyHistoryID!!,
                    position,
                    mViewModel.getHouseTenancyHistoryPayment(
                        mParamHousePosition,
                        mParamTenancyHistoryPosition,
                        position
                    ).id!!
                )
            )
            .commit()
    }
}