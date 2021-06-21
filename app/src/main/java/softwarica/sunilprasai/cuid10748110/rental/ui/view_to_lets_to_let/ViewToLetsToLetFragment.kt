package softwarica.sunilprasai.cuid10748110.rental.ui.view_to_lets_to_let

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
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import softwarica.sunilprasai.cuid10748110.rental.model.Image
import softwarica.sunilprasai.cuid10748110.rental.model.Location
import softwarica.sunilprasai.cuid10748110.rental.model.Review
import softwarica.sunilprasai.cuid10748110.rental.model.ToLet
import softwarica.sunilprasai.cuid10748110.rental.ui.add_house_tenancy.AddHouseTenancyFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.add_house_to_let.AddHouseToLetFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.shared_view_model.ToLetsViewModel
import softwarica.sunilprasai.cuid10748110.rental.ui.view_house_tenancy.ReviewsRVA
import softwarica.sunilprasai.cuid10748110.rental.ui.view_location.ViewLocationFragment
import softwarica.sunilprasai.cuid10748110.rental.utils.APIConsumer
import softwarica.sunilprasai.cuid10748110.rental.utils.APIService
import softwarica.sunilprasai.cuid10748110.rental.utils.LoadImage
import softwarica.sunilprasai.cuid10748110.rental.utils.UserToken

private const val TAG = "ViewToLetsToLetFragment"
private const val ARG_PARAM_TO_LET_POSITION = "position"
private const val ARG_PARAM_TO_LET_ID = "id"
private const val REQUEST_PERMISSION_TO_MAKE_CALL: Int = 1001

class ViewToLetsToLetFragment : Fragment(), View.OnClickListener,
    SwipeRefreshLayout.OnRefreshListener {
    private var mParamToLetPosition: Int = -1
    private var mParamToLetID: String? = null

    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout

    private lateinit var mOwnerImage: ImageView
    private lateinit var mOwnerName: TextView
    private lateinit var mHouseAddress: TextView
    private lateinit var mHouseImage: ImageView
    private lateinit var mHouseFloorsCount: TextView
    private lateinit var mOwnerContactNumber: TextView
    private lateinit var mOwnerEmailAddress: TextView
    private lateinit var mOwnerReview: TextView
    private lateinit var mToLetFacilities: TextView


    private lateinit var mViewOnMapBtn: MaterialButton
    private lateinit var mMakeCallBtn: MaterialButton

    private lateinit var mPreviousImageBtn: TextView
    private lateinit var mNextImageBtn: TextView

    private lateinit var mViewModel: ToLetsViewModel
    private var mCurrentImagePosition = 0
    private lateinit var mHouseImages: ArrayList<Image>
    private lateinit var mContext: Context
    private lateinit var mToLet: ToLet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: here in ${ViewToLetsToLetFragment::class.java.simpleName}")
        arguments?.let {
            mParamToLetPosition = it.getInt(ARG_PARAM_TO_LET_POSITION)
            mParamToLetID = it.getString(ARG_PARAM_TO_LET_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.fragment_view_to_lets_to_let, container, false)
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
        mToLetFacilities = view.findViewById(R.id.toLetFacilities)

        mViewOnMapBtn = view.findViewById(R.id.viewOnMapBtn)
        mViewOnMapBtn.setOnClickListener(this)

        mMakeCallBtn = view.findViewById(R.id.makeCallBtn)
        mMakeCallBtn.setOnClickListener(this)

        mPreviousImageBtn = view.findViewById(R.id.previousImageBtn)
        mPreviousImageBtn.setOnClickListener(this)
        mNextImageBtn = view.findViewById(R.id.nextImageBtn)
        mNextImageBtn.setOnClickListener(this)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel = ViewModelProvider(requireActivity()).get(ToLetsViewModel::class.java)

        mViewModel.getToLets().observe(requireActivity()) {
            if (isAdded) {
                if (it != null && mParamToLetPosition != -1 && mParamToLetPosition < it.size && it[mParamToLetPosition].id == mParamToLetID) {
                    mToLet = it[mParamToLetPosition]
                    updateView()
                } else {
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.addTenancyOptionMenu -> {
                requireActivity().supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right)
                    .addToBackStack(AddHouseTenancyFragment::class.java.simpleName).replace(
                        R.id.fragment_holder_two,
                        AddHouseTenancyFragment.newInstance(
                            mParamToLetPosition,
                            mParamToLetID!!
                        )
                    ).commit()
                true
            }
            R.id.addToLetOptionMenu -> {
                requireActivity().supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right)
                    .addToBackStack(AddHouseToLetFragment::class.java.simpleName).replace(
                        R.id.fragment_holder_two,
                        AddHouseToLetFragment.newInstance(mParamToLetPosition, mParamToLetID!!)
                    ).commit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateView() {
        mHouseImages = mToLet.house!!.images!!
        mToLet.house!!.owner!!.image?.let {
            LoadImage(object : LoadImage.ImageLoader {
                override fun onImageLoaded(imageBitmap: Bitmap?) {
                    mOwnerImage.setImageBitmap(imageBitmap)
                }
            }).execute(mToLet.house!!.owner!!.image!!.buffer)
        }

        mOwnerName.text = mToLet.house!!.owner!!.fullName
        mHouseAddress.text = mToLet.house!!.address!!
        mHouseFloorsCount.text =
            resources.getString(R.string.floors_info_format).format(mToLet.house!!.floors!!)

        mToLet.house!!.images!!.let {
            LoadImage(object : LoadImage.ImageLoader {
                override fun onImageLoaded(imageBitmap: Bitmap?) {
                    mHouseImage.setImageBitmap(imageBitmap)
                }
            }).execute(mToLet.house!!.images!![0].buffer)
        }

        mOwnerContactNumber.text = mToLet.house!!.owner!!.phone.toString()
        if (mToLet.house!!.owner!!.email != null) {
            mOwnerEmailAddress.text = mToLet.house!!.owner!!.email
        } else {
            mOwnerEmailAddress.text = "N/A"
        }

        val facilitiesInfo = StringBuilder()
        facilitiesInfo.append("Room facilities includes ")
        mToLet.facilities!!.mapIndexed { index, facility ->
            when {
                index < mToLet.facilities!!.size - 2 -> {
                    facilitiesInfo.append(facility).append(", ")
                }
                index < mToLet.facilities!!.size - 1 -> {
                    facilitiesInfo.append(facility).append(" and ")
                }
                else -> {
                    facilitiesInfo.append(facility)
                }
            }
        }
        mToLetFacilities.text = facilitiesInfo

        var totalReviewsCount = 0
        var totalReviewPoint = 0
        mToLet.house!!.owner!!.reviews!!.forEach {
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
        fun newInstance(toLetPosition: Int, toLetID: String) =
            ViewToLetsToLetFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM_TO_LET_POSITION, toLetPosition)
                    putString(ARG_PARAM_TO_LET_ID, toLetID)
                }
            }
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                mViewOnMapBtn.id -> {
                    val location: Location =
                        mViewModel.getToLet(mParamToLetPosition).house!!.location!!
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

                mOwnerReview.id -> {
                    val reviewsView = layoutInflater.inflate(R.layout.view_review_dialog, null)
                    val recyclerView: RecyclerView = reviewsView.findViewById(R.id.recyclerView)
                    recyclerView.layoutManager = LinearLayoutManager(mContext)
                    val recyclerViewAdapter =
                        ReviewsRVA(mToLet.house!!.owner!!.reviews!!.filter { it.context == Review.Context.OWNER } as ArrayList<Review>)
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

                mMakeCallBtn.id -> {
                    if (ActivityCompat.checkSelfPermission(
                            mContext,
                            Manifest.permission.CALL_PHONE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        startActivity(
                            Intent(
                                Intent.ACTION_CALL,
                                Uri.parse("tel:${mToLet.house!!.owner!!.phone!!}")
                            )
                        )
                    } else {
                        handleStoragePermissions()
                    }
                }
            }
        }
    }

    private fun handleStoragePermissions() {
        Log.d(TAG, "handlePermissions: ")
        if (shouldShowRequestPermissionRationale(
                Manifest.permission.CALL_PHONE
            )
        ) {
            AlertDialog.Builder(mContext).apply {
                setIcon(R.drawable.information)
                setTitle("Information")
                setMessage("To contact house owner this app requires permission to make call.\nDo you want to provide this permission?")
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
                    Uri.parse("tel:${mToLet.house!!.owner!!.phone!!}")
                )
            )
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onRefresh() {
        mSwipeRefreshLayout.isRefreshing = true
        val consumer = APIService.getService(APIConsumer::class.java)
        val getToLetsToLet =
            consumer.getToLetsToLet(UserToken.getInstance(mContext).token!!, mParamToLetID!!)
        getToLetsToLet.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val apiFetchedTenancy = ToLet(JSONObject(response.body()!!.string()))
                    mViewModel.replaceToLet(mParamToLetPosition, apiFetchedTenancy)
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
                        displayAPIErrorResponseDialog("Unable to refresh to-lets details. Please try again later")
                    }
                } else {
                    displayAPIErrorResponseDialog("Unable to refresh to-lets details. Please try again later")
                }
                mSwipeRefreshLayout.isRefreshing = false
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                displayAPIErrorResponseDialog("Unable to refresh to-lets details. Please try again later")
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