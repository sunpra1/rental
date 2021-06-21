package softwarica.sunilprasai.cuid10748110.rental.ui.view_tenant_tenancy

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import softwarica.sunilprasai.cuid10748110.rental.R
import softwarica.sunilprasai.cuid10748110.rental.model.Payment
import softwarica.sunilprasai.cuid10748110.rental.model.Tenancy
import softwarica.sunilprasai.cuid10748110.rental.model.Utility
import softwarica.sunilprasai.cuid10748110.rental.ui.shared_view_model.TenantTenanciesViewModel
import softwarica.sunilprasai.cuid10748110.rental.ui.view_tenant_tenancy_house.ViewTenantTenancyHouseFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.view_tenant_tenancy_payment.ViewTenantTenancyPaymentFragment
import softwarica.sunilprasai.cuid10748110.rental.utils.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private const val ARG_PARAM_TENANCY_POSITION = "position"
private const val ARG_PARAM_TENANCY_ID = "id"

class ViewTenantTenancyFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener,
    View.OnClickListener, TenantTenancyPaymentsRVItemTouchListener.OnPaymentTouch,
    TenantTenancyUtilitiesRVItemTouchListener.OnUtilityTouch {
    private var mParamTenancyPosition: Int = -1
    private var mParamTenancyID: String? = null

    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout

    private lateinit var mHouseOwnerImage: ImageView
    private lateinit var mHouseOwnerName: TextView
    private lateinit var mHouseAddress: TextView
    private lateinit var mTenancyNextRentDueInfo: TextView
    private lateinit var mTenancyDetails: TextView
    private lateinit var mTenancyDueInfo: TextView
    private lateinit var mTenancyAdvanceAmount: TextView
    private lateinit var mTenancyDueAmount: TextView
    private lateinit var mTenancyStartDate: TextView
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

    private lateinit var mViewHouseBtn: MaterialButton

    private lateinit var mViewModel: TenantTenanciesViewModel
    private lateinit var mTenancy: Tenancy
    private var mIsUtilityListVisible: Boolean = false
    private var mCurrentImagePosition = 0
    private lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mParamTenancyPosition = it.getInt(ARG_PARAM_TENANCY_POSITION)
            mParamTenancyID = it.getString(ARG_PARAM_TENANCY_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_tenant_tenancy, container, false)
        mContext = requireContext()
        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        mSwipeRefreshLayout.setOnRefreshListener(this)

        mHouseOwnerImage = view.findViewById(R.id.ownerImage)
        mHouseOwnerName = view.findViewById(R.id.ownerName)
        mHouseAddress = view.findViewById(R.id.houseAddress)
        mTenancyNextRentDueInfo = view.findViewById(R.id.tenancyNextRentDueInfo)
        mTenancyDetails = view.findViewById(R.id.tenancyDetails)
        mTenancyDueInfo = view.findViewById(R.id.tenancyDueInfo)
        mTenancyAdvanceAmount = view.findViewById(R.id.tenancyAdvanceAmount)
        mTenancyDueAmount = view.findViewById(R.id.tenancyDueAmount)
        mTenancyStartDate = view.findViewById(R.id.tenancyStartDate)
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
        mUtilitiesRecyclerView.addOnItemTouchListener(
            TenantTenancyUtilitiesRVItemTouchListener(
                mUtilitiesRecyclerView,
                this
            )
        )

        mPaymentsRecyclerView = view.findViewById(R.id.paymentsRecyclerView)
        mPaymentsRecyclerView.layoutManager = LinearLayoutManager(mContext)
        mPaymentsRecyclerViewAdapter = PaymentsRVA()
        mPaymentsRecyclerView.adapter = mPaymentsRecyclerViewAdapter
        mPaymentsRecyclerView.addOnItemTouchListener(
            TenantTenancyPaymentsRVItemTouchListener(
                mContext,
                mPaymentsRecyclerView,
                this
            )
        )

        mViewUtilitiesOrPaymentBtn = view.findViewById(R.id.viewUtilitiesOrPaymentsBtn)
        mViewUtilitiesOrPaymentBtn.setOnClickListener(this)

        mViewHouseBtn = view.findViewById(R.id.viewHouseBtn)
        mViewHouseBtn.setOnClickListener(this)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel = ViewModelProvider(requireActivity()).get(TenantTenanciesViewModel::class.java)

        mViewModel.getTenantTenancies().observe(requireActivity()) {
            if (isAdded) {
                if (it != null && mParamTenancyPosition != -1 && mParamTenancyPosition < it.size && it[mParamTenancyPosition].id == mParamTenancyID) {
                    mTenancy = it[mParamTenancyPosition]
                    updateView()
                } else {
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        }
    }

    private fun updateView() {
        mTenancy.house!!.owner!!.image?.let {
            LoadImage(object : LoadImage.ImageLoader {
                override fun onImageLoaded(imageBitmap: Bitmap?) {
                    mHouseOwnerImage.setImageBitmap(imageBitmap)
                }

            }).execute(it.buffer)
        }

        mHouseOwnerName.text = mTenancy.house!!.owner!!.fullName
        mTenancy.images!!.let {
            if (it.size > 0) {
                LoadImage(object : LoadImage.ImageLoader {
                    override fun onImageLoaded(imageBitmap: Bitmap?) {
                        mTenancyImage.setImageBitmap(imageBitmap)
                    }
                }).execute(it[0].buffer)
            }
        }

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
        val nextRentDueDate = Calendar.getInstance(Locale.ENGLISH).apply {
            clear(Calendar.AM_PM)
            clear(Calendar.HOUR)
            clear(Calendar.HOUR_OF_DAY)
            clear(Calendar.MINUTE)
            clear(Calendar.SECOND)
            clear(Calendar.MILLISECOND)
            if (calendarStartDate.get(Calendar.DAY_OF_MONTH) < get(Calendar.DAY_OF_MONTH)) {
                set(Calendar.MONTH, get(Calendar.MONTH) + 1)
            }
            set(Calendar.DAY_OF_MONTH, calendarStartDate.get(Calendar.DAY_OF_MONTH))
        }

        when {
            mTenancy.startDate!!.time == today.timeInMillis -> mTenancyNextRentDueInfo.text =
                resources.getString(R.string.tenancy_started_today)
            nextRentDueDate.timeInMillis == today.timeInMillis -> {
                mTenancyNextRentDueInfo.text = resources.getString(R.string.your_rent_due_today)
            }
            else -> {
                mTenancyNextRentDueInfo.text =
                    resources.getString(R.string.your_next_due_info_format).format(
                        "${nextRentDueDate.get(Calendar.YEAR)}-${nextRentDueDate.get(Calendar.MONTH) + 1}-${
                            nextRentDueDate.get(Calendar.DAY_OF_MONTH)
                        }"
                    )
            }
        }
        mTenancyDetails.text = resources.getString(R.string.your_tenancy_details_format).format(
            mTenancy.roomCount.toString(),
            mTenancy.amount.toString(),
            mTenancy.accrue.toString().toLowerCase(Locale.ROOT)
        )

        val dateBeingIncreasedByAMonthFromTenancyStartDateTillToday =
            Calendar.getInstance(Locale.ENGLISH).apply {
                time = mTenancy.startDate!!
            }
        val due: ArrayList<String> = ArrayList()
        while (dateBeingIncreasedByAMonthFromTenancyStartDateTillToday.before(today.apply {
                set(
                    Calendar.DAY_OF_MONTH,
                    today.get(Calendar.DAY_OF_MONTH) + 1
                )
            })) {
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
        }

        val paid = mTenancy.payments!!.map {
            "${it.paidForYear}-${it.paiForMonth}"
        }

        val dueButNotPaid = due.filter { paid.indexOf(it) == -1 }
        if (dueButNotPaid.isEmpty()) {
            mTenancyDueInfo.text = getString(R.string.you_cleared_all_dues_info)
            mTenancyDueInfo.setTextColor(resources.getColor(R.color.colorGreen, null))
        } else {
            val dueInfo = StringBuilder()
            dueInfo.append("You have not paid rent for ")
            dueButNotPaid.mapIndexed { index, date ->
                dueInfo.append(
                    SimpleDateFormat(MMMMM_yyyy, Locale.ENGLISH).format(
                        SimpleDateFormat(
                            yyyy_MM, Locale.ENGLISH
                        ).parse(date)!!
                    )
                )

                when {
                    index < dueButNotPaid.size - 2 -> {
                        dueInfo.append(", ")
                    }
                    index < dueButNotPaid.size - 1 -> {
                        dueInfo.append(" and ")
                    }
                    else -> {
                        dueInfo.append("")
                    }
                }
            }
            mTenancyDueInfo.text = dueInfo
        }

        mTenancyAdvanceAmount.text = mTenancy.advanceAmount.toString()
        mTenancyDueAmount.text = mTenancy.dueAmount.toString()
        mTenancyRoomType.text = mTenancy.roomType.toString()
        mTenancyStartDate.text =
            SimpleDateFormat(yyyy_MM_dd, Locale.ENGLISH).format(mTenancy.startDate!!)

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
        fun newInstance(tenancyPosition: Int, tenancyID: String) =
            ViewTenantTenancyFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM_TENANCY_POSITION, tenancyPosition)
                    putString(ARG_PARAM_TENANCY_ID, tenancyID)
                }
            }
    }

    override fun onRefresh() {
        mSwipeRefreshLayout.isRefreshing = true
        val consumer = APIService.getService(APIConsumer::class.java)
        val tenantTenancyResponse =
            consumer.getTenantTenancy(
                UserToken.getInstance(mContext).token!!,
                mParamTenancyID!!
            )
        tenantTenancyResponse.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val apiFetchedTenancy = Tenancy(JSONObject(response.body()!!.string()))
                    mViewModel.replaceTenantTenancy(mParamTenancyPosition, apiFetchedTenancy)
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
                        displayAPIErrorResponseDialog("Unable to refresh your tenancy details. Please try again later")
                    }
                } else {
                    displayAPIErrorResponseDialog("Unable to refresh your tenancy details. Please try again later")
                }
                mSwipeRefreshLayout.isRefreshing = false
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                displayAPIErrorResponseDialog("Unable to refresh your tenancy details. Please try again later")
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

                mViewHouseBtn.id -> {
                    val viewTenantHouseFrag = ViewTenantTenancyHouseFragment.newInstance(
                        mParamTenancyPosition,
                        mParamTenancyID!!
                    )
                    requireActivity().supportFragmentManager
                        .beginTransaction()
                        .addToBackStack(ViewTenantTenancyHouseFragment::class.java.simpleName)
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right)
                        .replace(R.id.fragment_holder_two, viewTenantHouseFrag)
                        .commit()

                }
            }
        }
    }

    override fun onPaymentClick(position: Int) {
        val utility = mViewModel.getTenantTenancyPayment(mParamTenancyPosition, position)
        val viewTenantPaymentFragment = ViewTenantTenancyPaymentFragment.newInstance(
            mParamTenancyPosition,
            mParamTenancyID!!,
            position,
            utility.id!!
        )
        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right)
            .replace(R.id.fragment_holder_two, viewTenantPaymentFragment)
            .addToBackStack(ViewTenantTenancyPaymentFragment::class.simpleName).commit()
    }

    override fun onPaymentApproveSelected(position: Int) {
        val payment = mViewModel.getTenantTenancyPayment(mParamTenancyPosition, position)
        val approvedPayment = Payment(payment).apply { approved = true }
        mViewModel.replaceTenantTenancyPayment(mParamTenancyPosition, position, approvedPayment)
        Snackbar.make(
            requireView(),
            "Click UNDO to roll back payment approved status.",
            Snackbar.LENGTH_LONG
        )
            .setAction("UNDO") {
                mViewModel.replaceTenantTenancyPayment(mParamTenancyPosition, position, payment)
            }
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)
                    if (event == DISMISS_EVENT_TIMEOUT || event == DISMISS_EVENT_SWIPE) {
                        togglePaymentApproveStatus(position, payment)
                    }
                }
            }).show()
    }

    override fun onPaymentDisapproveSelected(position: Int) {
        val payment = mViewModel.getTenantTenancyPayment(mParamTenancyPosition, position)
        val approvedPayment = Payment(payment).apply { approved = false }
        mViewModel.replaceTenantTenancyPayment(mParamTenancyPosition, position, approvedPayment)
        Snackbar.make(
            requireView(),
            "Click UNDO to roll back payment approved status.",
            Snackbar.LENGTH_LONG
        )
            .setAction("UNDO") {
                mViewModel.replaceTenantTenancyPayment(mParamTenancyPosition, position, payment)
            }
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)
                    if (event == DISMISS_EVENT_TIMEOUT || event == DISMISS_EVENT_SWIPE) {
                        togglePaymentApproveStatus(position, payment)
                    }
                }
            }).show()
    }

    override fun onUtilityApproveSelected(position: Int) {
        val utility = mViewModel.getTenantTenancyUtility(mParamTenancyPosition, position)
        val approvedUtility = Utility(utility).apply { approved = true }
        mViewModel.replaceTenantTenancyUtility(mParamTenancyPosition, position, approvedUtility)
        Snackbar.make(
            requireView(),
            "Click UNDO to roll back utility approved status.",
            Snackbar.LENGTH_LONG
        )
            .setAction("UNDO") {
                mViewModel.replaceTenantTenancyUtility(mParamTenancyPosition, position, utility)
            }
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)
                    if (event == DISMISS_EVENT_TIMEOUT || event == DISMISS_EVENT_SWIPE) {
                        toggleUtilityApproveStatus(position, utility)
                    }
                }
            }).show()
    }

    override fun onUtilityDisapproveSelected(position: Int) {
        val utility = mViewModel.getTenantTenancyUtility(mParamTenancyPosition, position)
        val approvedUtility = Utility(utility).apply { approved = false }
        mViewModel.replaceTenantTenancyUtility(mParamTenancyPosition, position, approvedUtility)
        Snackbar.make(
            requireView(),
            "Click UNDO to rollback utility approved status.",
            Snackbar.LENGTH_LONG
        )
            .setAction("UNDO") {
                mViewModel.replaceTenantTenancyUtility(mParamTenancyPosition, position, utility)
            }
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)
                    if (event == DISMISS_EVENT_TIMEOUT || event == DISMISS_EVENT_SWIPE) {
                        toggleUtilityApproveStatus(position, utility)
                    }
                }
            }).show()
    }

    private fun togglePaymentApproveStatus(position: Int, oldPayment: Payment) {
        val consumer = APIService.getService(APIConsumer::class.java)
        val toggleApproveStatusOfTenantTenancyPayment =
            consumer.toggleTenantTenancyPaymentApproveStatus(
                UserToken.getInstance(mContext).token!!,
                mParamTenancyID!!,
                oldPayment.id!!
            )
        toggleApproveStatusOfTenantTenancyPayment.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    AppToast.show(
                        mContext,
                        "Tenancy payment has been ${if (oldPayment.approved!!) "disapproved" else "approved"} successfully",
                        Toast.LENGTH_LONG
                    )
                } else if (!response.isSuccessful && response.errorBody() != null) {
                    rollbackTenantTenancyPaymentNotApproved()
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
                        displayAPIErrorResponseDialog("Unable to ${if (oldPayment.approved!!) "disapprove" else "approve"} your tenancy payment. Please try again later.")
                    }
                } else {
                    rollbackTenantTenancyPaymentNotApproved()
                    displayAPIErrorResponseDialog("Unable to ${if (oldPayment.approved!!) "disapprove" else "approve"} your tenancy payment. Please try again later.")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                rollbackTenantTenancyPaymentNotApproved()
                displayAPIErrorResponseDialog("Unable to ${if (oldPayment.approved!!) "disapprove" else "approve"} your tenancy payment. Please try again later.")
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

            private fun rollbackTenantTenancyPaymentNotApproved() {
                mViewModel.replaceTenantTenancyPayment(mParamTenancyPosition, position, oldPayment)
            }
        })
    }

    private fun toggleUtilityApproveStatus(position: Int, oldUtility: Utility) {
        val consumer = APIService.getService(APIConsumer::class.java)
        val toggleApproveStatusOfTenantTenancyUtility =
            consumer.toggleTenantTenancyUtilityApproveStatus(
                UserToken.getInstance(mContext).token!!,
                mParamTenancyID!!,
                oldUtility.id!!
            )
        toggleApproveStatusOfTenantTenancyUtility.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    AppToast.show(
                        mContext,
                        "Tenancy utility has been ${if (oldUtility.approved!!) "disapproved" else "approved"} successfully",
                        Toast.LENGTH_LONG
                    )
                } else if (!response.isSuccessful && response.errorBody() != null) {
                    rollbackTenantTenancyUtilityNotApproved()
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
                        displayAPIErrorResponseDialog("Unable to ${if (oldUtility.approved!!) "disapprove" else "approve"} your tenancy utility. Please try again later.")
                    }
                } else {
                    rollbackTenantTenancyUtilityNotApproved()
                    displayAPIErrorResponseDialog("Unable to ${if (oldUtility.approved!!) "disapprove" else "approve"} your tenancy utility. Please try again later.")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                rollbackTenantTenancyUtilityNotApproved()
                displayAPIErrorResponseDialog("Unable to ${if (oldUtility.approved!!) "disapprove" else "approve"} your tenancy utility. Please try again later.")
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

            private fun rollbackTenantTenancyUtilityNotApproved() {
                mViewModel.replaceTenantTenancyUtility(mParamTenancyPosition, position, oldUtility)
            }
        })
    }
}