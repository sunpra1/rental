package softwarica.sunilprasai.cuid10748110.rental.ui.view_tenant_tenancy_house

import android.Manifest
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
import softwarica.sunilprasai.cuid10748110.rental.model.Image
import softwarica.sunilprasai.cuid10748110.rental.model.Location
import softwarica.sunilprasai.cuid10748110.rental.model.Review
import softwarica.sunilprasai.cuid10748110.rental.model.Tenancy
import softwarica.sunilprasai.cuid10748110.rental.ui.shared_view_model.TenantTenanciesViewModel
import softwarica.sunilprasai.cuid10748110.rental.ui.view_house_tenancy.ReviewsRVA
import softwarica.sunilprasai.cuid10748110.rental.ui.view_location.ViewLocationFragment
import softwarica.sunilprasai.cuid10748110.rental.utils.*

private const val ARG_PARAM_TENANCY_POSITION = "position"
private const val ARG_PARAM_TENANCY_ID = "id"

private const val TAG = "ViewTenantTenancyHouseF"
private const val REQUEST_PERMISSION_TO_MAKE_CALL: Int = 1001

class ViewTenantTenancyHouseFragment : Fragment(), View.OnClickListener,
    SwipeRefreshLayout.OnRefreshListener {
    private var mParamTenancyPosition: Int = -1
    private var mParamTenancyID: String? = null

    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout

    private lateinit var mOwnerImage: ImageView
    private lateinit var mOwnerName: TextView
    private lateinit var mHouseAddress: TextView
    private lateinit var mHouseImage: ImageView
    private lateinit var mHouseFloorsCount: TextView
    private lateinit var mOwnerContactNumber: TextView
    private lateinit var mOwnerEmailAddress: TextView
    private lateinit var mOwnerReview: TextView

    private lateinit var mViewOnMapBtn: MaterialButton
    private lateinit var mReviewOwnerBtn: MaterialButton

    private lateinit var mPreviousImageBtn: TextView
    private lateinit var mNextImageBtn: TextView

    private lateinit var mViewModel: TenantTenanciesViewModel
    private var mCurrentImagePosition = 0
    private lateinit var mHouseImages: ArrayList<Image>
    private lateinit var mContext: Context
    private lateinit var mTenancy: Tenancy

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
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.fragment_view_tenant_tenancy_house, container, false)
        mContext = requireContext()
        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        mSwipeRefreshLayout.setOnRefreshListener(this)

        mOwnerImage = view.findViewById(R.id.ownerImage)
        mOwnerName = view.findViewById(R.id.ownerName)

        mHouseImage = view.findViewById(R.id.houseImage)
        mHouseFloorsCount = view.findViewById(R.id.floorsCount)
        mOwnerContactNumber = view.findViewById(R.id.ownerContactNumber)
        mOwnerEmailAddress = view.findViewById(R.id.ownerEmailAddress)
        mHouseAddress = view.findViewById(R.id.houseAddress)
        mOwnerReview = view.findViewById(R.id.ownerReview)
        mOwnerReview.setOnClickListener(this)

        mViewOnMapBtn = view.findViewById(R.id.viewOnMapBtn)
        mViewOnMapBtn.setOnClickListener(this)

        mReviewOwnerBtn = view.findViewById(R.id.reviewOwner)
        mReviewOwnerBtn.setOnClickListener(this)

        mPreviousImageBtn = view.findViewById(R.id.previousImageBtn)
        mPreviousImageBtn.setOnClickListener(this)
        mNextImageBtn = view.findViewById(R.id.nextImageBtn)
        mNextImageBtn.setOnClickListener(this)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.callOwnerOptionMenu -> {
                if (ActivityCompat.checkSelfPermission(
                        mContext,
                        Manifest.permission.CALL_PHONE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    startActivity(
                        Intent(
                            Intent.ACTION_CALL,
                            Uri.parse("tel:${mTenancy.house!!.owner!!.phone!!}")
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

    private fun updateView() {
        mHouseImages = mTenancy.house!!.images!!
        mTenancy.house!!.owner!!.image?.let {
            LoadImage(object : LoadImage.ImageLoader {
                override fun onImageLoaded(imageBitmap: Bitmap?) {
                    mOwnerImage.setImageBitmap(imageBitmap)
                }
            }).execute(mTenancy.house!!.owner!!.image!!.buffer)
        }

        mOwnerName.text = mTenancy.house!!.owner!!.fullName
        mHouseAddress.text = mTenancy.house!!.address!!
        mHouseFloorsCount.text =
            resources.getString(R.string.floors_info_format).format(mTenancy.house!!.floors!!)

        mTenancy.house!!.images!!.let {
            LoadImage(object : LoadImage.ImageLoader {
                override fun onImageLoaded(imageBitmap: Bitmap?) {
                    mHouseImage.setImageBitmap(imageBitmap)
                }
            }).execute(mTenancy.house!!.images!![0].buffer)
        }

        mOwnerContactNumber.text = mTenancy.house!!.owner!!.phone.toString()
        if (mTenancy.house!!.owner!!.email != null) {
            mOwnerEmailAddress.text = mTenancy.house!!.owner!!.email
        } else {
            mOwnerEmailAddress.text = "N/A"
        }

        var totalReviewsCount = 0
        var totalReviewPoint = 0
        mTenancy.house!!.owner!!.reviews!!.forEach {
            if (it.context == Review.Context.OWNER) {
                totalReviewPoint += it.rating!!
                totalReviewsCount += 1
            }
        }

        if (totalReviewPoint > 0 && totalReviewsCount > 0)
            mOwnerReview.text = resources.getString(R.string.review_format)
                .format(totalReviewPoint / totalReviewsCount, totalReviewsCount)
        else
            mOwnerReview.text = resources.getString(R.string.review_format)
                .format(0, 0)
    }

    companion object {
        @JvmStatic
        fun newInstance(tenancyPosition: Int, tenancyID: String) =
            ViewTenantTenancyHouseFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM_TENANCY_POSITION, tenancyPosition)
                    putString(ARG_PARAM_TENANCY_ID, tenancyID)
                }
            }
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                mViewOnMapBtn.id -> {
                    val location: Location =
                        mViewModel.getTenantTenancy(mParamTenancyPosition).house!!.location!!
                    requireActivity().supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right)
                        .addToBackStack(ViewLocationFragment::class.java.simpleName).replace(
                            R.id.fragment_holder_two,
                            ViewLocationFragment.newInstance(
                                location.latitude!!,
                                location.longitude!!
                            )
                        ).commit()
                }
                mPreviousImageBtn.id -> {
                    if (mHouseImages.size > 0) {
                        if (mCurrentImagePosition == 0) {
                            mCurrentImagePosition = mHouseImages.size - 1
                        } else {
                            mCurrentImagePosition--
                        }
                        LoadImage(object : LoadImage.ImageLoader {
                            override fun onImageLoaded(imageBitmap: Bitmap?) {
                                mHouseImage.setImageBitmap(imageBitmap)
                            }
                        }).execute(mHouseImages[mCurrentImagePosition].buffer)
                    }
                }

                mNextImageBtn.id -> {
                    if (mHouseImages.size > 0) {
                        if (mCurrentImagePosition == mHouseImages.size - 1) {
                            mCurrentImagePosition = 0
                        } else {
                            mCurrentImagePosition++
                        }
                        LoadImage(object : LoadImage.ImageLoader {
                            override fun onImageLoaded(imageBitmap: Bitmap?) {
                                mHouseImage.setImageBitmap(imageBitmap)
                            }
                        }).execute(mHouseImages[mCurrentImagePosition].buffer)
                    }
                }

                mReviewOwnerBtn.id -> {
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
                        .setTitle("REVIEW OWNER")
                        .setIcon(R.drawable.star_yellow)
                        .setView(reviewView)
                        .setPositiveButton("REVIEW", null)
                        .setNegativeButton("CANCEL") { _, _ ->

                        }
                        .show()

                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        if (reviewRating != 0 && !reviewCommentEt.text.isNullOrEmpty()) {
                            postReview(Review().apply {
                                context = Review.Context.OWNER
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

                mOwnerReview.id -> {
                    val reviewsView = layoutInflater.inflate(R.layout.view_review_dialog, null)
                    val recyclerView: RecyclerView = reviewsView.findViewById(R.id.recyclerView)
                    recyclerView.layoutManager = LinearLayoutManager(mContext)
                    val recyclerViewAdapter =
                        ReviewsRVA(mTenancy.house!!.owner!!.reviews!!.filter { it.context == Review.Context.OWNER } as ArrayList<Review>)
                    recyclerView.adapter = recyclerViewAdapter

                    AlertDialog.Builder(mContext)
                        .setTitle("OWNER REVIEWS")
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
            mViewModel.getTenantTenancy(mParamTenancyPosition).house!!.owner!!.id!!,
            requestBody
        )
        postReview.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    val reviewResponse = Review(JSONObject(response.body()!!.string()))
                    mViewModel.addTenantHouseOwnerReview(mParamTenancyPosition, reviewResponse)
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

    override fun onRefresh() {
        mSwipeRefreshLayout.isRefreshing = true
        val consumer = APIService.getService(APIConsumer::class.java)
        val tenancyResponse =
            consumer.getTenantTenancy(UserToken.getInstance(mContext).token!!, mParamTenancyID!!)
        tenancyResponse.enqueue(object : Callback<ResponseBody> {
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
                        displayAPIErrorResponseDialog("Unable to refresh your tenancy house details. Please try again later")
                    }
                } else {
                    displayAPIErrorResponseDialog("Unable to refresh your tenancy house details. Please try again later")
                }
                mSwipeRefreshLayout.isRefreshing = false
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                displayAPIErrorResponseDialog("Unable to refresh your tenancy house details. Please try again later")
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