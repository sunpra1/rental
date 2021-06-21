package softwarica.sunilprasai.cuid10748110.rental.ui.view_house_to_let

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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import softwarica.sunilprasai.cuid10748110.rental.R
import softwarica.sunilprasai.cuid10748110.rental.model.ToLet
import softwarica.sunilprasai.cuid10748110.rental.ui.shared_view_model.HouseViewModel
import softwarica.sunilprasai.cuid10748110.rental.utils.*
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_PARAM_HOUSE_POSITION = "housePosition"
private const val ARG_PARAM_HOUSE_ID = "houseId"
private const val ARG_PARAM_TO_LET_POSITION = "toLetPosition"
private const val ARG_PARAM_TO_LET_ID = "toLetId"

class ViewHouseToLetFragment : Fragment(), View.OnClickListener,
    SwipeRefreshLayout.OnRefreshListener {
    private var mParamHousePosition: Int = -1
    private var mParamHouseID: String? = null
    private var mParamToLetPosition: Int = -1
    private var mParamToLetID: String? = null

    private lateinit var mViewModel: HouseViewModel

    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout

    private lateinit var mToLetImage: ImageView

    private lateinit var mPreviousImageBtn: TextView
    private lateinit var mNextImageBtn: TextView

    private lateinit var mToLetRoomType: TextView
    private lateinit var mToLetFacilities: TextView
    private lateinit var mToLetRoomCount: TextView
    private lateinit var mToLetRoomRentAmount: TextView
    private lateinit var mToLetPostedOn: TextView

    private lateinit var mToLet: ToLet
    private var mCurrentImagePosition = 0
    private lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mParamHousePosition = it.getInt(ARG_PARAM_HOUSE_POSITION)
            mParamHouseID = it.getString(ARG_PARAM_HOUSE_ID)
            mParamToLetPosition = it.getInt(ARG_PARAM_TO_LET_POSITION)
            mParamToLetID = it.getString(ARG_PARAM_TO_LET_ID)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel = ViewModelProvider(requireActivity()).get(HouseViewModel::class.java)
        mViewModel.getHouses().observe(requireActivity()) {
            if (isAdded) {
                if (mParamHousePosition != -1 && mParamToLetPosition != -1 && mParamHousePosition < it.size && mParamToLetPosition < it[mParamHousePosition].toLets!!.size && it[mParamHousePosition].id == mParamHouseID && it[mParamHousePosition].toLets!![mParamToLetPosition].id == mParamToLetID) {
                    mToLet = it[mParamHousePosition].toLets!![mParamToLetPosition]
                    updateView()
                } else {
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        }
    }

    private fun updateView() {
        if (mToLet.images!!.size > 0) {
            LoadImage(object : LoadImage.ImageLoader {
                override fun onImageLoaded(imageBitmap: Bitmap?) {
                    mToLetImage.setImageBitmap(imageBitmap)
                }
            }).execute(mToLet.images!![0].buffer)
        }

        mToLetRoomType.text = mToLet.roomType.toString()

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
        mToLetRoomCount.text = mToLet.roomCount.toString()
        mToLetRoomRentAmount.text = mToLet.amount.toString()
        mToLetPostedOn.text =
            SimpleDateFormat(yyyy_MM_dd, Locale.ENGLISH).format(mToLet.createdAt!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_house_to_let, container, false)
        mContext = requireContext()
        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        mSwipeRefreshLayout.setOnRefreshListener(this)
        mToLetImage = view.findViewById(R.id.toLetImage)

        mPreviousImageBtn = view.findViewById(R.id.previousImageBtn)
        mPreviousImageBtn.setOnClickListener(this)
        mNextImageBtn = view.findViewById(R.id.nextImageBtn)
        mNextImageBtn.setOnClickListener(this)

        mToLetRoomType = view.findViewById(R.id.toLetRoomType)
        mToLetFacilities = view.findViewById(R.id.toLetFacilities)
        mToLetRoomCount = view.findViewById(R.id.toLetRoomCount)
        mToLetRoomRentAmount = view.findViewById(R.id.toLetRoomRentAmount)
        mToLetPostedOn = view.findViewById(R.id.toLetPostedOn)

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(housePosition: Int, houseID: String, toLetPosition: Int, toLetID: String) =
            ViewHouseToLetFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM_HOUSE_POSITION, housePosition)
                    putString(ARG_PARAM_HOUSE_ID, houseID)
                    putInt(ARG_PARAM_TO_LET_POSITION, toLetPosition)
                    putString(ARG_PARAM_TO_LET_ID, toLetID)
                }
            }
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                mPreviousImageBtn.id -> {
                    if (mToLet.images!!.size > 0) {
                        if (mCurrentImagePosition == 0) {
                            mCurrentImagePosition = mToLet.images!!.size - 1
                        } else {
                            mCurrentImagePosition--
                        }
                        LoadImage(object : LoadImage.ImageLoader {
                            override fun onImageLoaded(imageBitmap: Bitmap?) {
                                mToLetImage.setImageBitmap(imageBitmap)
                            }
                        }).execute(mToLet.images!![mCurrentImagePosition].buffer)
                    }
                }

                mNextImageBtn.id -> {
                    if (mToLet.images!!.size > 0) {
                        if (mCurrentImagePosition == mToLet.images!!.size - 1) {
                            mCurrentImagePosition = 0
                        } else {
                            mCurrentImagePosition++
                        }
                        LoadImage(object : LoadImage.ImageLoader {
                            override fun onImageLoaded(imageBitmap: Bitmap?) {
                                mToLetImage.setImageBitmap(imageBitmap)
                            }
                        }).execute(mToLet.images!![mCurrentImagePosition].buffer)
                    }
                }
            }
        }
    }

    override fun onRefresh() {
        mSwipeRefreshLayout.isRefreshing = true
        val consumer = APIService.getService(APIConsumer::class.java)
        val getToLet = consumer.getToLet(
            UserToken.getInstance(mContext).token!!,
            mParamHouseID!!,
            mParamToLetID!!
        )
        getToLet.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    val toLetResponse = ToLet(JSONObject(response.body()!!.string()))
                    mViewModel.replaceToLet(mParamHousePosition, mParamToLetPosition, toLetResponse)
                    mSwipeRefreshLayout.isRefreshing = false
                } else if (!response.isSuccessful && response.errorBody() != null) {
                    mSwipeRefreshLayout.isRefreshing = false
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
                        displayAPIErrorResponseDialog("Unable to update to-let details. Please try again later")
                    }
                } else {
                    displayAPIErrorResponseDialog("Unable to update to-let details. Please try again later")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                mSwipeRefreshLayout.isRefreshing = false
                displayAPIErrorResponseDialog("Unable to update to-let details. Please try again later")
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