package softwarica.sunilprasai.cuid10748110.rental.ui.dashboard

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import softwarica.sunilprasai.cuid10748110.rental.AuthViewModel
import softwarica.sunilprasai.cuid10748110.rental.R
import softwarica.sunilprasai.cuid10748110.rental.model.User
import softwarica.sunilprasai.cuid10748110.rental.ui.edit_profile.EditProfileFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.settings.SettingsFragment
import softwarica.sunilprasai.cuid10748110.rental.utils.*
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "DashboardFragment"

class DashboardFragment : Fragment(), View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private lateinit var mUserImage: ImageView
    private lateinit var mUserRating: TextView
    private lateinit var mUserName: TextView
    private lateinit var mUserAddress: TextView
    private lateinit var mUserGender: TextView
    private lateinit var mUserDOB: TextView
    private lateinit var mUserContactNumber: TextView
    private lateinit var mUserEmailAddress: TextView
    private lateinit var mUserTenanciesCount: TextView
    private lateinit var mUserHousesCount: TextView
    private lateinit var mEditProfileBtn: FloatingActionButton
    private lateinit var mSettingsBtn: MaterialButton
    private lateinit var mLogOutBtn: MaterialButton

    private lateinit var mViewMore: TextView
    private lateinit var mViewLess: TextView
    private lateinit var mMoreDetails: View

    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    private lateinit var mViewModel: AuthViewModel
    private lateinit var mContext: Context

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        mContext = requireContext()
        mUserImage = view.findViewById(R.id.userImage)
        mUserRating = view.findViewById(R.id.userRating)
        mUserName = view.findViewById(R.id.userName)
        mUserAddress = view.findViewById(R.id.userAddress)
        mUserGender = view.findViewById(R.id.userGender)
        mUserDOB = view.findViewById(R.id.userDOB)
        mUserContactNumber = view.findViewById(R.id.userContactNumber)
        mUserEmailAddress = view.findViewById(R.id.userEmailAddress)
        mUserHousesCount = view.findViewById(R.id.userHousesCount)
        mUserTenanciesCount = view.findViewById(R.id.userTenanciesCount)
        mEditProfileBtn = view.findViewById(R.id.editProfileBtn)
        mEditProfileBtn.setOnClickListener(this)
        mSettingsBtn = view.findViewById(R.id.settingBtn)
        mSettingsBtn.setOnClickListener(this)
        mLogOutBtn = view.findViewById(R.id.logOutBtn)
        mLogOutBtn.setOnClickListener(this)
        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        mSwipeRefreshLayout.setOnRefreshListener(this)

        mViewMore = view.findViewById(R.id.viewMore)
        mViewMore.setOnClickListener(this)
        mViewLess = view.findViewById(R.id.viewLess)
        mViewLess.setOnClickListener(this)
        mMoreDetails = view.findViewById(R.id.moreDetailsWrapper)
        mViewMore.visibility = View.VISIBLE
        mViewLess.visibility = View.GONE
        mMoreDetails.visibility = View.GONE

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
        mViewModel.getLoggedInUser().observe(requireActivity(), {
            if (isAdded && it != null) {
                AppState.loggedInUser = it
                updateView(it)
            }
        })
    }

    private fun updateView(user: User) {
        user.image?.let {
            LoadImage(object : LoadImage.ImageLoader {
                override fun onImageLoaded(imageBitmap: Bitmap?) {
                    mUserImage.setImageBitmap(imageBitmap)
                }
            }).execute(user.image!!.buffer!!)
        }

        var totalReviewsCount = 0
        var totalReviewPoint = 0
        user.reviews!!.forEach {
            totalReviewPoint += it.rating!!
            totalReviewsCount += 1
        }

        if (totalReviewPoint > 0 && totalReviewsCount > 0)
            mUserRating.text = resources.getString(R.string.review_format)
                .format(totalReviewPoint / totalReviewsCount, totalReviewsCount)
        else
            mUserRating.text = resources.getString(R.string.review_format)
                .format(0, 0)

        mUserName.text = user.fullName!!
        if (user.address != null) {
            mUserAddress.visibility =
                View.VISIBLE
            mUserAddress.text = user.address
        } else mUserAddress.visibility =
            View.GONE
        if (user.gender != null) {
            mUserGender.visibility = View.VISIBLE
            mUserGender.text =
                user.gender.toString()
        } else mUserGender.visibility = View.GONE
        if (user.dob != null) {
            mUserDOB.visibility = View.VISIBLE
            mUserDOB.text = SimpleDateFormat(
                dd_MMMMM_yyyy,
                Locale.ENGLISH
            ).format(user.dob!!)
        } else mUserDOB.visibility = View.GONE
        mUserContactNumber.text = user.phone!!.toString()
        if (user.email != null) {
            mUserEmailAddress.visibility = View.VISIBLE
            mUserEmailAddress.text =
                user.email
        } else mUserEmailAddress.visibility = View.GONE
        mUserTenanciesCount.text = user.tenancies!!.size.toString()
        mUserHousesCount.text = user.houses!!.size.toString()
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                mEditProfileBtn.id -> {
                    requireActivity().supportFragmentManager
                        .beginTransaction()
                        .addToBackStack(EditProfileFragment::class.java.simpleName)
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right)
                        .replace(R.id.fragment_holder_two, EditProfileFragment())
                        .commit()
                }

                mLogOutBtn.id -> {
                    logOutUser()
                }

                mSettingsBtn.id -> {
                    requireActivity().supportFragmentManager
                        .beginTransaction()
                        .addToBackStack(SettingsFragment::class.java.simpleName)
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right)
                        .replace(R.id.fragment_holder_two, SettingsFragment())
                        .commit()
                }

                mViewMore.id -> {
                    mMoreDetails.visibility = View.VISIBLE
                    mViewLess.visibility = View.VISIBLE
                    mViewMore.visibility = View.GONE
                }

                mViewLess.id -> {
                    mMoreDetails.visibility = View.GONE
                    mViewLess.visibility = View.GONE
                    mViewMore.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun logOutUser() {
        val consumer: APIConsumer = APIService.getService(APIConsumer::class.java)
        val logOutUser: Call<ResponseBody> =
            consumer.logOutUser(UserToken.getInstance(mContext).token!!)
        logOutUser.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    response.body()?.let {
                        UserToken.getInstance(mContext).deleteToken()
                        mViewModel.setLoggedInUser(null)
                        AppState.loggedInUser = null
                    }
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
                        displayAPIErrorResponseDialog("Unable to log you out. Please try again later")
                    }
                } else {
                    displayAPIErrorResponseDialog("Unable to log you out. Please try again later")
                }
                mSwipeRefreshLayout.isRefreshing = false
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                mSwipeRefreshLayout.isRefreshing = false
                displayAPIErrorResponseDialog("Unable to log you out. Please try again later")
            }

            private fun displayAPIErrorResponseDialog(message: String) {
                android.app.AlertDialog.Builder(mContext)
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
        val consumer: APIConsumer = APIService.getService(APIConsumer::class.java)
        val getLoggedInUserProfile: Call<ResponseBody> =
            consumer.getUserProfile(UserToken.getInstance(mContext).token!!)
        getLoggedInUserProfile.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    response.body()?.let {
                        val jsonResponse = JSONObject(it.string())
                        Log.d(TAG, "onResponse of fetchLoggedInUserDetails: $jsonResponse")
                        val user = User(jsonResponse)
                        mViewModel.setLoggedInUser(user)
                        AppState.loggedInUser = user
                    }
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
                        displayAPIErrorResponseDialog("Unable to refresh your details. Please try again later")
                    }
                } else {
                    displayAPIErrorResponseDialog("Unable to refresh your details. Please try again later")
                }
                mSwipeRefreshLayout.isRefreshing = false
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                mSwipeRefreshLayout.isRefreshing = false
                displayAPIErrorResponseDialog("Unable to refresh your details. Please try again later")
            }

            private fun displayAPIErrorResponseDialog(message: String) {
                android.app.AlertDialog.Builder(mContext)
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