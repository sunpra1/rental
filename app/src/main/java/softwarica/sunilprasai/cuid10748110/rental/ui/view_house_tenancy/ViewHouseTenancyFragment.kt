package softwarica.sunilprasai.cuid10748110.rental.ui.view_house_tenancy

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
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
import softwarica.sunilprasai.cuid10748110.rental.model.Review
import softwarica.sunilprasai.cuid10748110.rental.model.Tenancy
import softwarica.sunilprasai.cuid10748110.rental.model.Utility
import softwarica.sunilprasai.cuid10748110.rental.ui.add_house_tenancy_payment.AddHouseTenancyPaymentFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.add_house_tenancy_payment.EditHouseTenancyPaymentFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.add_house_tenancy_utility.AddHouseTenancyUtilityFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.edit_house_tenancy_utility.EditHouseTenancyUtilityFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.shared_view_model.HouseViewModel
import softwarica.sunilprasai.cuid10748110.rental.ui.view_house_tenancy_payment.ViewHouseTenancyPaymentFragment
import softwarica.sunilprasai.cuid10748110.rental.utils.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "ViewHouseTenancyFragment"
private const val ARG_PARAM_TENANCY_POSITION = "tenancyPosition"
private const val ARG_PARAM_TENANCY_ID = "tenancyId"
private const val ARG_PARAM_HOUSE_POSITION = "housePosition"
private const val ARG_PARAM_HOUSE_ID = "houseId"
private const val REQUEST_PERMISSION_TO_MAKE_CALL: Int = 1001

class ViewHouseTenancyFragment : Fragment(), View.OnClickListener,
    PaymentsRVItemTouchListener.OnPaymentTouch, UtilitiesRVItemTouchListener.OnUtilityTouch,
    SwipeRefreshLayout.OnRefreshListener {
    private var mParamTenancyPosition: Int = -1
    private var mParamTenancyID: String? = null
    private var mParamHousePosition: Int = -1
    private var mParamHouseID: String? = null

    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout

    private lateinit var mTenantImage: ImageView
    private lateinit var mTenantName: TextView
    private lateinit var mTenantReview: TextView
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
    private lateinit var mReviewTenantBtn: MaterialButton

    private lateinit var mRVTitle: TextView
    private lateinit var mRVNoItemsInfo: TextView
    private lateinit var mUtilitiesRecyclerView: RecyclerView
    private lateinit var mPaymentsRecyclerView: RecyclerView
    private lateinit var mUtilitiesRecyclerViewAdapter: UtilitiesRVA
    private lateinit var mPaymentsRecyclerViewAdapter: PaymentsRVA

    private lateinit var mViewModel: HouseViewModel

    private lateinit var mTenancy: Tenancy
    private var mCurrentImagePosition = 0
    private var mIsUtilityListVisible: Boolean = false
    private lateinit var mContext: Context

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
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.fragment_view_house_tenancy, container, false)
        mContext = requireContext()
        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        mSwipeRefreshLayout.setOnRefreshListener(this)

        mTenantImage = view.findViewById(R.id.tenantImage)
        mTenantName = view.findViewById(R.id.tenantName)
        mTenantReview = view.findViewById(R.id.tenantReview)
        mTenantReview.setOnClickListener(this)
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
            UtilitiesRVItemTouchListener(
                mUtilitiesRecyclerView,
                this
            )
        )

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
        mReviewTenantBtn = view.findViewById(R.id.reviewTenant)
        mReviewTenantBtn.setOnClickListener(this)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel = ViewModelProvider(requireActivity()).get(HouseViewModel::class.java)

        mViewModel.getHouses().observe(requireActivity()) {
            if (isAdded) {
                if (it != null && mParamHousePosition != -1 && mParamTenancyPosition != -1 && mParamTenancyPosition < it[mParamHousePosition].tenancies!!.size && it[mParamHousePosition].id == mParamHouseID && it[mParamHousePosition].tenancies!![mParamTenancyPosition].id == mParamTenancyID) {
                    mTenancy = it[mParamHousePosition].tenancies!![mParamTenancyPosition]
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

        var totalReviewsCount = 0
        var totalReviewPoint = 0
        mTenancy.tenant!!.reviews!!.forEach {
            if (it.context == Review.Context.TENANT) {
                totalReviewPoint += it.rating!!
                totalReviewsCount += 1
            }
        }

        if (totalReviewPoint > 0 && totalReviewsCount > 0)
            mTenantReview.text = resources.getString(R.string.review_format)
                .format(totalReviewPoint / totalReviewsCount, totalReviewsCount)
        else
            mTenantReview.text = resources.getString(R.string.review_format).format(0, 0)

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
                mTenancyNextRentDueInfo.text = resources.getString(R.string.tenancy_next_due_today)
            }
            else -> {
                mTenancyNextRentDueInfo.text =
                    resources.getString(R.string.tenancy_next_due_info_format).format(
                        "${nextRentDueDate.get(Calendar.YEAR)}-${nextRentDueDate.get(Calendar.MONTH) + 1}-${
                            nextRentDueDate.get(Calendar.DAY_OF_MONTH)
                        }"
                    )
            }
        }
        mTenancyDetails.text = resources.getString(R.string.tenancy_details_format).format(
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
            mTenancyDueInfo.text = getString(R.string.cleared_all_dues_info)
            mTenancyDueInfo.setTextColor(resources.getColor(R.color.colorGreen, null))
        } else {
            val dueInfo = StringBuilder()
            dueInfo.append("Tenant has not paid rent for ")
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

    @SuppressLint("InflateParams")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.endTenancyOptionMenu -> {
                AlertDialog.Builder(mContext)
                    .setIcon(R.drawable.information)
                    .setTitle("END TENANCY?")
                    .setMessage("Please confirm to end tenancy of ${mTenancy.tenant!!.fullName!!}")
                    .setPositiveButton(R.string.ok) { _, _ ->
                        endTenancy()
                    }
                    .setNegativeButton(R.string.cancel) { dialog, _ ->
                        dialog.cancel()
                    }
                    .show()
                true
            }

            R.id.addUtilityOptionMenu -> {
                requireActivity().supportFragmentManager.beginTransaction()
                    .addToBackStack(AddHouseTenancyUtilityFragment::class.java.simpleName)
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right).replace(
                        R.id.fragment_holder_two,
                        AddHouseTenancyUtilityFragment.newInstance(
                            mParamHousePosition,
                            mParamHouseID!!,
                            mParamTenancyPosition,
                            mParamTenancyID!!
                        )
                    ).commit()
                true
            }

            R.id.addPaymentOptionMenu -> {
                requireActivity().supportFragmentManager.beginTransaction()
                    .addToBackStack(AddHouseTenancyPaymentFragment::class.java.simpleName)
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right).replace(
                        R.id.fragment_holder_two,
                        AddHouseTenancyPaymentFragment.newInstance(
                            mParamHousePosition,
                            mParamHouseID!!,
                            mParamTenancyPosition,
                            mParamTenancyID!!
                        )
                    ).commit()
                true
            }
            R.id.callTenantOptionMenu -> {
                if (ActivityCompat.checkSelfPermission(
                        mContext,
                        Manifest.permission.CALL_PHONE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    startActivity(
                        Intent(
                            Intent.ACTION_CALL,
                            Uri.parse("tel:${mTenancy.tenant!!.phone!!}")
                        )
                    )
                } else {
                    handleCallPhonePermissions()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun handleCallPhonePermissions() {
        Log.d(TAG, "handlePermissions: ")
        if (shouldShowRequestPermissionRationale(
                Manifest.permission.CALL_PHONE
            )
        ) {
            AlertDialog.Builder(mContext).apply {
                setIcon(R.drawable.information)
                setTitle("Information")
                setMessage("To contact tenant this app requires permission to make call.\nDo you want to provide this permission?")
                setPositiveButton(R.string.ok) { _, _ ->
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri =
                        Uri.fromParts(
                            "package",
                            requireActivity().packageName,
                            this::class.java.simpleName
                        )
                    intent.data = uri
                    startActivity(intent)
                }
                setNegativeButton(getString(R.string.no_thanks)) { _, _ ->
                    //Do nothing
                }
                show()
            }
        } else {
            Log.d(TAG, "handlePermissions: shouldShowRequestPermissionRationale is false")
            requestPermissions(
                arrayOf(Manifest.permission.CALL_PHONE),
                REQUEST_PERMISSION_TO_MAKE_CALL
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionsResult: $requestCode")
        if (requestCode == REQUEST_PERMISSION_TO_MAKE_CALL && ContextCompat.checkSelfPermission(
                mContext,
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startActivity(
                Intent(
                    Intent.ACTION_CALL,
                    Uri.parse("tel:${mTenancy.tenant!!.phone!!}")
                )
            )
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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
            ViewHouseTenancyFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM_HOUSE_POSITION, housePosition)
                    putString(ARG_PARAM_HOUSE_ID, houseId)
                    putInt(ARG_PARAM_TENANCY_POSITION, tenancyPosition)
                    putString(ARG_PARAM_TENANCY_ID, tenancyId)
                }
            }
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

                mReviewTenantBtn.id -> {
                    val reviewView = layoutInflater.inflate(R.layout.review_dialog, null)
                    val starOne: ImageButton = reviewView.findViewById(R.id.startOne)
                    val starTwo: ImageButton = reviewView.findViewById(R.id.startTwo)
                    val starThree: ImageButton = reviewView.findViewById(R.id.startThree)
                    val starFour: ImageButton = reviewView.findViewById(R.id.startFour)
                    val starFive: ImageButton = reviewView.findViewById(R.id.startFive)
                    val reviewCommentEt: TextInputEditText =
                        reviewView.findViewById(R.id.reviewCommentEt)
                    val reviewCommentTil: TextInputLayout =
                        reviewView.findViewById(R.id.reviewCommentTil)
                    val reviewRatingTil: TextInputLayout =
                        reviewView.findViewById(R.id.reviewRatingTil)
                    var reviewRating = 0

                    val starClickListener = View.OnClickListener {
                        it?.let {
                            when (it.id) {
                                starOne.id -> {
                                    starOne.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            mContext,
                                            R.drawable.star_yellow
                                        )
                                    )
                                    starTwo.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            mContext,
                                            R.drawable.star_dark
                                        )
                                    )
                                    starThree.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            mContext,
                                            R.drawable.star_dark
                                        )
                                    )
                                    starFour.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            mContext,
                                            R.drawable.star_dark
                                        )
                                    )
                                    starFive.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            mContext,
                                            R.drawable.star_dark
                                        )
                                    )
                                    reviewRating = 1
                                }

                                starTwo.id -> {
                                    starOne.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            mContext,
                                            R.drawable.star_yellow
                                        )
                                    )
                                    starTwo.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            mContext,
                                            R.drawable.star_yellow
                                        )
                                    )
                                    starThree.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            mContext,
                                            R.drawable.star_dark
                                        )
                                    )
                                    starFour.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            mContext,
                                            R.drawable.star_dark
                                        )
                                    )
                                    starFive.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            mContext,
                                            R.drawable.star_dark
                                        )
                                    )
                                    reviewRating = 2
                                }

                                starThree.id -> {
                                    starOne.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            mContext,
                                            R.drawable.star_yellow
                                        )
                                    )
                                    starTwo.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            mContext,
                                            R.drawable.star_yellow
                                        )
                                    )
                                    starThree.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            mContext,
                                            R.drawable.star_yellow
                                        )
                                    )
                                    starFour.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            mContext,
                                            R.drawable.star_dark
                                        )
                                    )
                                    starFive.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            mContext,
                                            R.drawable.star_dark
                                        )
                                    )
                                    reviewRating = 3
                                }

                                starFour.id -> {
                                    starOne.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            mContext,
                                            R.drawable.star_yellow
                                        )
                                    )
                                    starTwo.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            mContext,
                                            R.drawable.star_yellow
                                        )
                                    )
                                    starThree.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            mContext,
                                            R.drawable.star_yellow
                                        )
                                    )
                                    starFour.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            mContext,
                                            R.drawable.star_yellow
                                        )
                                    )
                                    starFive.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            mContext,
                                            R.drawable.star_dark
                                        )
                                    )
                                    reviewRating = 4
                                }

                                starFive.id -> {
                                    starOne.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            mContext,
                                            R.drawable.star_yellow
                                        )
                                    )
                                    starTwo.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            mContext,
                                            R.drawable.star_yellow
                                        )
                                    )
                                    starThree.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            mContext,
                                            R.drawable.star_yellow
                                        )
                                    )
                                    starFour.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            mContext,
                                            R.drawable.star_yellow
                                        )
                                    )
                                    starFive.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            mContext,
                                            R.drawable.star_yellow
                                        )
                                    )
                                    reviewRating = 5
                                }
                            }
                        }
                    }

                    starOne.setOnClickListener(starClickListener)
                    starTwo.setOnClickListener(starClickListener)
                    starThree.setOnClickListener(starClickListener)
                    starFour.setOnClickListener(starClickListener)
                    starFive.setOnClickListener(starClickListener)

                    val dialog = AlertDialog.Builder(mContext)
                        .setTitle("REVIEW TENANT")
                        .setIcon(R.drawable.star_yellow)
                        .setView(reviewView)
                        .setPositiveButton("REVIEW", null)
                        .setNegativeButton("CANCEL") { _, _ ->

                        }
                        .show()

                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        if (reviewRating != 0 && !reviewCommentEt.text.isNullOrEmpty()) {
                            postReview(Review().apply {
                                context = Review.Context.TENANT
                                rating = reviewRating
                                comment = reviewCommentEt.text!!.toString()
                            })
                            dialog.dismiss()
                        } else {
                            if (reviewCommentEt.text.isNullOrEmpty()) {
                                reviewCommentTil.isErrorEnabled = true
                                reviewCommentTil.error = "Review comment is required."
                            }

                            if (reviewRating == 0) {
                                reviewRatingTil.isErrorEnabled = true
                                reviewRatingTil.error = "Please select your rating."
                            }
                        }
                    }
                }

                mTenantReview.id -> {
                    val reviewsView = layoutInflater.inflate(R.layout.view_review_dialog, null)
                    val recyclerView: RecyclerView = reviewsView.findViewById(R.id.recyclerView)
                    recyclerView.layoutManager = LinearLayoutManager(mContext)
                    val recyclerViewAdapter =
                        ReviewsRVA(mTenancy.tenant!!.reviews!!.filter { it.context == Review.Context.TENANT } as ArrayList<Review>)
                    recyclerView.adapter = recyclerViewAdapter

                    AlertDialog.Builder(mContext)
                        .setTitle("TENANT REVIEWS")
                        .setIcon(R.drawable.review_list)
                        .setView(reviewsView)
                        .setPositiveButton("CLOSE") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        }
    }

    private fun postReview(review: Review) {
        val consumer = APIService.getService(APIConsumer::class.java)
        val requestBody =
            RequestBody.create(MediaType.parse(TYPE_JSON), review.getJSONObject().toString())
        val postReview = consumer.postReview(
            UserToken.getInstance(mContext).token!!,
            mViewModel.getTenancy(mParamHousePosition, mParamTenancyPosition).tenant!!.id!!,
            requestBody
        )
        postReview.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    val reviewResponse = Review(JSONObject(response.body()!!.string()))
                    mViewModel.addHouseTenantReview(
                        mParamHousePosition,
                        mParamTenancyPosition,
                        reviewResponse
                    )
                    AppToast.show(
                        mContext,
                        "Your review has been posted successfully.",
                        Toast.LENGTH_LONG
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
                        displayAPIErrorResponseDialog("Unable to post your review. Please try again later")
                    }
                } else {
                    displayAPIErrorResponseDialog("Unable to post your review. Please try again later")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                displayAPIErrorResponseDialog("Unable to post your review. Please try again later")
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

    override fun onPaymentClick(position: Int) {
        val utility = mViewModel.getPayment(mParamHousePosition, mParamTenancyPosition, position)
        val viewPaymentFragment = ViewHouseTenancyPaymentFragment.newInstance(
            mParamHousePosition,
            mParamHouseID!!,
            mParamTenancyPosition,
            mParamTenancyID!!,
            position,
            utility.id!!
        )
        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right)
            .replace(R.id.fragment_holder_two, viewPaymentFragment)
            .addToBackStack(ViewHouseTenancyPaymentFragment::class.simpleName).commit()
    }

    override fun onPaymentDeleteSelected(position: Int) {
        val payment =
            mViewModel.getHouses().value!![mParamHousePosition].tenancies!![mParamTenancyPosition].payments!![position]
        mViewModel.deletePayment(mParamHousePosition, mParamTenancyPosition, position)

        Snackbar.make(requireView(), "Press UNDO to rollback payment deleted", Snackbar.LENGTH_LONG)
            .setAction(
                "UNDO"
            ) {
                mViewModel.addPayment(payment, mParamHousePosition, mParamTenancyPosition, position)
            }
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)
                    if (event == DISMISS_EVENT_TIMEOUT || event == DISMISS_EVENT_SWIPE) {
                        deletePayment(payment, position)
                    }
                }
            })
            .show()
    }

    override fun onPaymentEditSelected(position: Int) {
        val utility = mViewModel.getPayment(mParamHousePosition, mParamTenancyPosition, position)
        val editTenancyFragment = EditHouseTenancyPaymentFragment.newInstance(
            mParamHousePosition,
            mParamHouseID!!,
            mParamTenancyPosition,
            mParamTenancyID!!,
            position,
            utility.id!!
        )
        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right)
            .replace(R.id.fragment_holder_two, editTenancyFragment)
            .addToBackStack(EditHouseTenancyPaymentFragment::class.simpleName).commit()
    }

    override fun onUtilityDeleteSelected(position: Int) {
        val utility =
            mViewModel.getHouses().value!![mParamHousePosition].tenancies!![mParamTenancyPosition].utilities!![position]
        mViewModel.deleteUtility(mParamHousePosition, mParamTenancyPosition, position)
        Snackbar.make(requireView(), "Press UNDO to rollback utility deleted", Snackbar.LENGTH_LONG)
            .setAction("UNDO") {
                mViewModel.addUtility(utility, mParamHousePosition, mParamTenancyPosition, position)
            }
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)
                    if (event == DISMISS_EVENT_SWIPE || event == DISMISS_EVENT_TIMEOUT) {
                        deleteUtility(utility, position)
                    }
                }
            })
            .show()
    }

    override fun onUtilityEditSelected(position: Int) {
        val utility = mViewModel.getUtility(mParamHousePosition, mParamTenancyPosition, position)
        val editTenancyFragment = EditHouseTenancyUtilityFragment.newInstance(
            mParamHousePosition,
            mParamHouseID!!,
            mParamTenancyPosition,
            mParamTenancyID!!,
            position,
            utility.id!!
        )
        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right)
            .replace(R.id.fragment_holder_two, editTenancyFragment)
            .addToBackStack(EditHouseTenancyUtilityFragment::class.simpleName).commit()
    }

    private fun deletePayment(payment: Payment, position: Int) {
        val consumer = APIService.getService(APIConsumer::class.java)
        val deletePayment = consumer.deletePayment(
            UserToken.getInstance(mContext).token!!,
            mParamHouseID!!,
            mParamTenancyID!!,
            payment.id!!
        )
        deletePayment.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    AppToast.show(
                        mContext,
                        "Payment has been deleted successfully",
                        Toast.LENGTH_LONG
                    )
                } else if (!response.isSuccessful && response.errorBody() != null) {
                    rollbackPaymentNotDeleted()
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
                        displayAPIErrorResponseDialog("Unable to delete payment. Please try again later")
                    }
                } else {
                    rollbackPaymentNotDeleted()
                    displayAPIErrorResponseDialog("Unable to delete payment. Please try again later")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                rollbackPaymentNotDeleted()
                displayAPIErrorResponseDialog("Unable to delete payment. Please try again later")
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

            fun rollbackPaymentNotDeleted() {
                mViewModel.addPayment(payment, mParamHousePosition, mParamTenancyPosition, position)
            }
        })
    }

    private fun deleteUtility(utility: Utility, position: Int) {
        val consumer = APIService.getService(APIConsumer::class.java)
        val deleteUtility = consumer.deleteUtility(
            UserToken.getInstance(mContext).token!!,
            mParamHouseID!!,
            mParamTenancyID!!,
            utility.id!!
        )
        deleteUtility.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    AppToast.show(
                        mContext,
                        "Utility has been deleted successfully",
                        Toast.LENGTH_LONG
                    )
                } else if (!response.isSuccessful && response.errorBody() != null) {
                    rollbackUtilityNotDeleted()
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
                        displayAPIErrorResponseDialog("Unable to delete utility. Please try again later")
                    }
                } else {
                    rollbackUtilityNotDeleted()
                    displayAPIErrorResponseDialog("Unable to delete utility. Please try again later")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                rollbackUtilityNotDeleted()
                displayAPIErrorResponseDialog("Unable to delete utility. Please try again later")
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

            fun rollbackUtilityNotDeleted() {
                mViewModel.addUtility(utility, mParamHousePosition, mParamTenancyPosition, position)
            }
        })
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
                        displayAPIErrorResponseDialog("Unable to update tenancy details. Please try again later")
                    }
                } else {
                    displayAPIErrorResponseDialog("Unable to update tenancy details. Please try again later")
                }
                mSwipeRefreshLayout.isRefreshing = false
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                displayAPIErrorResponseDialog("Unable to update tenancy details. Please try again later")
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

    private fun endTenancy() {
        val consumer = APIService.getService(APIConsumer::class.java)
        val endTenancy = consumer.endTenancy(
            UserToken.getInstance(mContext).token!!,
            mParamHouseID!!,
            mTenancy.id!!
        )
        endTenancy.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    val tenancyResponse = Tenancy(JSONObject(response.body()!!.string()))
                    mViewModel.replaceTenancy(
                        mParamHousePosition,
                        mParamTenancyPosition,
                        tenancyResponse
                    )
                    AppToast.show(
                        mContext,
                        "Tenancy of ${mTenancy.tenant!!.fullName!!} has been ended successfully",
                        Toast.LENGTH_LONG
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
                        displayAPIErrorResponseDialog("Unable to end tenancy of ${mTenancy.tenant!!.fullName!!}. Please try again later")
                    }
                } else {
                    displayAPIErrorResponseDialog("Unable to end tenancy of ${mTenancy.tenant!!.fullName!!}. Please try again later")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                displayAPIErrorResponseDialog("Unable to end tenancy of ${mTenancy.tenant!!.fullName!!}. Please try again later")
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