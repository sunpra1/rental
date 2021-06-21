package softwarica.sunilprasai.cuid10748110.rental.ui.view_house

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
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
import softwarica.sunilprasai.cuid10748110.rental.model.*
import softwarica.sunilprasai.cuid10748110.rental.ui.add_house_tenancy.AddHouseTenancyFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.add_house_to_let.AddHouseToLetFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.edit_house_tenancy.EditHouseTenancyFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.edit_house_to_let.EditHouseToLetFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.house_tenancies_history.HouseTenanciesHistoryFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.shared_view_model.HouseViewModel
import softwarica.sunilprasai.cuid10748110.rental.ui.view_house_tenancy.ViewHouseTenancyFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.view_house_to_let.ViewHouseToLetFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.view_location.ViewLocationFragment
import softwarica.sunilprasai.cuid10748110.rental.utils.*
import java.util.*

private const val TAG = "ViewHouseFragment"
private const val ARG_PARAM_HOUSE_POSITION = "position"
private const val ARG_PARAM_HOUSE_ID = "id"

class ViewHouseFragment : Fragment(), View.OnClickListener,
    TenanciesRVItemTouchListener.OnTenancyTouch, SwipeRefreshLayout.OnRefreshListener,
    ToLetsRVItemTouchListener.OnToLetTouch {
    private var mParamHousePosition: Int = -1
    private var mParamHouseID: String? = null

    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout

    private lateinit var mHouseImage: ImageView
    private lateinit var mHouseFloorsCount: TextView
    private lateinit var mHouseAddress: TextView
    private lateinit var mHouseTenanciesCount: TextView
    private lateinit var mHouseToLetsCount: TextView
    private lateinit var mViewOnMapBtn: MaterialButton

    private lateinit var mPreviousImageBtn: TextView
    private lateinit var mNextImageBtn: TextView

    private lateinit var mTenanciesRecyclerView: RecyclerView
    private lateinit var mToLetsRecyclerView: RecyclerView
    private lateinit var mTenantsRecyclerViewAdapter: TenanciesRVA
    private lateinit var mToLetsRecyclerViewAdapter: ToLetsRVA

    private lateinit var mRVTitle: TextView
    private lateinit var mRVNoItemsInfo: TextView
    private lateinit var mViewTenanciesOrToLetBtn: MaterialButton

    private lateinit var mViewModel: HouseViewModel
    private lateinit var mHouse: House
    private var mCurrentImagePosition = 0
    private lateinit var mHouseImages: ArrayList<Image>
    private var isTenanciesVisible: Boolean = true
    private lateinit var mContext: Context
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mParamHousePosition = it.getInt(ARG_PARAM_HOUSE_POSITION)
            mParamHouseID = it.getString(ARG_PARAM_HOUSE_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.fragment_view_house, container, false)
        mContext = requireContext()
        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        mSwipeRefreshLayout.setOnRefreshListener(this)

        mHouseImage = view.findViewById(R.id.houseImage)
        mHouseFloorsCount = view.findViewById(R.id.floorsCount)
        mHouseAddress = view.findViewById(R.id.address)
        mHouseTenanciesCount = view.findViewById(R.id.tenanciesCount)
        mHouseToLetsCount = view.findViewById(R.id.toLetsCount)
        mViewOnMapBtn = view.findViewById(R.id.viewOnMapBtn)
        mViewOnMapBtn.setOnClickListener(this)

        mPreviousImageBtn = view.findViewById(R.id.previousImageBtn)
        mPreviousImageBtn.setOnClickListener(this)
        mNextImageBtn = view.findViewById(R.id.nextImageBtn)
        mNextImageBtn.setOnClickListener(this)

        mRVTitle = view.findViewById(R.id.rvTitle)
        mRVNoItemsInfo = view.findViewById(R.id.rvNoItemsInfo)

        mTenanciesRecyclerView = view.findViewById(R.id.tenanciesRecyclerView)
        mTenanciesRecyclerView.layoutManager = LinearLayoutManager(mContext)
        mTenantsRecyclerViewAdapter = TenanciesRVA()
        mTenanciesRecyclerView.adapter = mTenantsRecyclerViewAdapter
        mTenanciesRecyclerView.addOnItemTouchListener(
            TenanciesRVItemTouchListener(
                mContext,
                mTenanciesRecyclerView,
                this
            )
        )

        mToLetsRecyclerView = view.findViewById(R.id.toLetsRecyclerView)
        mToLetsRecyclerView.layoutManager = LinearLayoutManager(mContext)
        mToLetsRecyclerViewAdapter = ToLetsRVA()
        mToLetsRecyclerView.adapter = mToLetsRecyclerViewAdapter
        mToLetsRecyclerView.addOnItemTouchListener(
            ToLetsRVItemTouchListener(
                mContext,
                mToLetsRecyclerView,
                this
            )
        )

        mViewTenanciesOrToLetBtn = view.findViewById(R.id.viewTenanciesOrToLetBtn)
        mViewTenanciesOrToLetBtn.setOnClickListener(this)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel = ViewModelProvider(requireActivity()).get(HouseViewModel::class.java)

        mViewModel.getHouses().observe(requireActivity()) {
            if (isAdded) {
                if (it != null && mParamHousePosition != -1 && mParamHousePosition < it.size && it[mParamHousePosition].id == mParamHouseID) {
                    mHouse = it[mParamHousePosition]
                    updateView()
                } else {
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.viewTenancyHistoryOptionMenu -> {
                requireActivity().supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right)
                    .addToBackStack(HouseTenanciesHistoryFragment::class.java.simpleName).replace(
                        R.id.fragment_holder_two,
                        HouseTenanciesHistoryFragment.newInstance(
                            mParamHousePosition,
                            mParamHouseID!!
                        )
                    ).commit()
                true
            }
            R.id.addTenancyOptionMenu -> {
                requireActivity().supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right)
                    .addToBackStack(AddHouseTenancyFragment::class.java.simpleName).replace(
                        R.id.fragment_holder_two,
                        AddHouseTenancyFragment.newInstance(mParamHousePosition, mParamHouseID!!)
                    ).commit()
                true
            }
            R.id.addToLetOptionMenu -> {
                requireActivity().supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right)
                    .addToBackStack(AddHouseToLetFragment::class.java.simpleName).replace(
                        R.id.fragment_holder_two,
                        AddHouseToLetFragment.newInstance(mParamHousePosition, mParamHouseID!!)
                    ).commit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateView() {
        mHouse.images?.let {
            mHouseImages = it
            if (it.size > 0) {
                LoadImage(object : LoadImage.ImageLoader {
                    override fun onImageLoaded(imageBitmap: Bitmap?) {
                        mHouseImage.setImageBitmap(imageBitmap)
                    }
                }).execute(it[0].buffer)
            }
        }

        mHouse.address?.let {
            mHouseAddress.text = mHouse.address
        }

        mHouse.floors?.let {
            mHouseFloorsCount.text =
                resources.getString(R.string.floors_info_format).format(mHouse.floors.toString())
        }

        mHouse.images?.let {
            if (it.size > 0) {
                LoadImage(object : LoadImage.ImageLoader {
                    override fun onImageLoaded(imageBitmap: Bitmap?) {
                        mHouseImage.setImageBitmap(imageBitmap)
                    }
                }).execute(it[0].buffer)
            }
        }

        mHouseTenanciesCount.text = mHouse.tenancies!!.size.toString()
        mHouseToLetsCount.text = mHouse.toLets!!.size.toString()
        mTenantsRecyclerViewAdapter.setTenancy(mHouse.tenancies!!)
        mToLetsRecyclerViewAdapter.setToLets(mHouse.toLets!!)

        mViewTenanciesOrToLetBtn.text =
            if (isTenanciesVisible) resources.getString(R.string.to_lets) else resources.getString(
                R.string.tenancies
            )
        if (isTenanciesVisible) {
            mRVTitle.text = resources.getString(R.string.tenancies)
            mToLetsRecyclerView.visibility = View.GONE
            mTenanciesRecyclerView.visibility = View.VISIBLE
            if (mHouse.tenancies!!.size == 0) {
                mRVNoItemsInfo.visibility = View.VISIBLE
                mRVNoItemsInfo.text = resources.getString(R.string.no_tenancy_yet)
            } else {
                mRVNoItemsInfo.visibility = View.GONE
            }
        } else {
            mRVTitle.text = resources.getString(R.string.to_lets)
            mTenanciesRecyclerView.visibility = View.GONE
            mToLetsRecyclerView.visibility = View.VISIBLE
            if (mHouse.toLets!!.size == 0) {
                mRVNoItemsInfo.visibility = View.VISIBLE
                mRVNoItemsInfo.text = resources.getString(R.string.no_to_lets_yet)
            } else {
                mRVNoItemsInfo.visibility = View.GONE
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(housePosition: Int, houseID: String) =
            ViewHouseFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM_HOUSE_POSITION, housePosition)
                    putString(ARG_PARAM_HOUSE_ID, houseID)
                }
            }
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                mViewOnMapBtn.id -> {
                    val location: Location = mViewModel.getHouse(mParamHousePosition).location!!
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

                mViewTenanciesOrToLetBtn.id -> {
                    isTenanciesVisible = !isTenanciesVisible
                    mViewTenanciesOrToLetBtn.text =
                        if (isTenanciesVisible) resources.getString(R.string.to_lets) else resources.getString(
                            R.string.tenancies
                        )
                    if (isTenanciesVisible) {
                        mRVTitle.text = resources.getString(R.string.tenancies)
                        mToLetsRecyclerView.visibility = View.GONE
                        mTenanciesRecyclerView.visibility = View.VISIBLE
                        if (mHouse.tenancies!!.size == 0) {
                            mRVNoItemsInfo.visibility = View.VISIBLE
                            mRVNoItemsInfo.text = resources.getString(R.string.no_tenancy_yet)
                        } else {
                            mRVNoItemsInfo.visibility = View.GONE
                        }
                    } else {
                        mRVTitle.text = resources.getString(R.string.to_lets)
                        mTenanciesRecyclerView.visibility = View.GONE
                        mToLetsRecyclerView.visibility = View.VISIBLE
                        if (mHouse.toLets!!.size == 0) {
                            mRVNoItemsInfo.visibility = View.VISIBLE
                            mRVNoItemsInfo.text = resources.getString(R.string.no_to_lets_yet)
                        } else {
                            mRVNoItemsInfo.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    override fun onTenancyClick(position: Int) {
        val tenancy = mViewModel.getTenancy(mParamHousePosition, position)
        if (tenancy.approved!!) {
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right)
                .addToBackStack(ViewHouseTenancyFragment::class.java.simpleName).replace(
                    R.id.fragment_holder_two,
                    ViewHouseTenancyFragment.newInstance(
                        mParamHousePosition,
                        mParamHouseID!!,
                        position,
                        tenancy.id!!
                    )
                ).commit()
        } else {
            AppToast.show(
                mContext,
                "Tenancy request has not been approved yet",
                Toast.LENGTH_LONG
            )
        }
    }

    override fun onTenancyDeleteSelected(position: Int) {
        val tenancyToBeDeleted = mViewModel.getHouse(mParamHousePosition).tenancies!![position]
        mViewModel.deleteTenancy(mParamHousePosition, position)

        Snackbar.make(
            requireView(), "Click UNDO to rollback the deleted tenancy",
            Snackbar.LENGTH_LONG
        ).setAction("UNDO") {
            mViewModel.addTenancy(mParamHousePosition, position, tenancyToBeDeleted)
        }.addCallback(object : Snackbar.Callback() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                super.onDismissed(transientBottomBar, event)
                if (DISMISS_EVENT_TIMEOUT == event || DISMISS_EVENT_SWIPE == event) {
                    deleteTenancy(tenancyToBeDeleted, position)
                }
            }
        }).show()
    }

    override fun onTenancyEditSelected(position: Int) {
        Log.d(TAG, "onTenancyClick: ")
        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right)
            .addToBackStack(EditHouseTenancyFragment::class.java.simpleName).replace(
                R.id.fragment_holder_two,
                EditHouseTenancyFragment.newInstance(
                    mParamHousePosition,
                    mParamHouseID!!,
                    position,
                    mViewModel.getTenancy(mParamHousePosition, position).id!!
                )
            ).commit()
    }

    private fun deleteTenancy(tenancy: Tenancy, position: Int) {
        val consumer = APIService.getService(APIConsumer::class.java)
        val deleteTenancy = consumer.deleteTenancy(
            UserToken.getInstance(mContext).token!!,
            mParamHouseID!!,
            tenancy.id!!
        )
        deleteTenancy.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    val houseResponse = House(JSONObject(response.body()!!.string()))
                    mViewModel.replaceHouse(mParamHousePosition, houseResponse)
                    AppToast.show(
                        mContext,
                        "Tenancy has been deleted successfully",
                        Toast.LENGTH_LONG
                    )
                } else if (!response.isSuccessful && response.errorBody() != null) {
                    rollbackTenancyNotDeleted()
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
                        displayAPIErrorResponseDialog("Unable to delete tenancy. Please try again later")
                    }
                } else {
                    rollbackTenancyNotDeleted()
                    displayAPIErrorResponseDialog("Unable to delete tenancy. Please try again later")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                rollbackTenancyNotDeleted()
                displayAPIErrorResponseDialog("Unable to delete tenancy. Please try again later")
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

            fun rollbackTenancyNotDeleted() {
                mViewModel.addTenancy(mParamHousePosition, position, tenancy)
            }
        })
    }

    override fun onRefresh() {
        mSwipeRefreshLayout.isRefreshing = true
        val consumer = APIService.getService(APIConsumer::class.java)
        val getHouse =
            consumer.getHouse(UserToken.getInstance(mContext).token!!, mParamHouseID!!)
        getHouse.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val apiFetchedHouse = House(JSONObject(response.body()!!.string()))
                    mViewModel.replaceHouse(mParamHousePosition, apiFetchedHouse)
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
                        displayAPIErrorResponseDialog("Unable to refresh your house details. Please try again later")
                    }
                } else {
                    displayAPIErrorResponseDialog("Unable to refresh your house details. Please try again later")
                }
                mSwipeRefreshLayout.isRefreshing = false
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                displayAPIErrorResponseDialog("Unable to refresh your house details. Please try again later")
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

    override fun onToLetClick(position: Int) {
        val toLet = mViewModel.getToLet(mParamHousePosition, position)
        requireActivity().supportFragmentManager
            .beginTransaction()
            .addToBackStack(ViewHouseToLetFragment::class.java.simpleName)
            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right)
            .replace(
                R.id.fragment_holder_two,
                ViewHouseToLetFragment.newInstance(
                    mParamHousePosition,
                    mParamHouseID!!,
                    position,
                    toLet.id!!
                )
            )
            .commit()
    }

    override fun onToLetDeleteSelected(position: Int) {
        val toLet = mViewModel.getHouse(mParamHousePosition).toLets!![position]
        mViewModel.deleteToLet(mParamHousePosition, position)
        Snackbar.make(
            requireView(),
            "Click UNDO to rollback the deleted tenancy",
            Snackbar.LENGTH_LONG
        )
            .setAction("UNDO") {
                mViewModel.addToLet(mParamHousePosition, position, toLet)
            }
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)
                    if (event == DISMISS_EVENT_TIMEOUT || event == DISMISS_EVENT_SWIPE) {
                        deleteToLet(toLet, position)
                    }
                }
            }).show()
    }

    override fun onToLetEditSelected(position: Int) {
        val editToLetFragment = EditHouseToLetFragment.newInstance(
            mParamHousePosition,
            mParamHouseID!!,
            position,
            mViewModel.getToLet(mParamHousePosition, position).id!!
        )
        requireActivity().supportFragmentManager
            .beginTransaction()
            .addToBackStack(EditHouseToLetFragment::class.java.simpleName)
            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right)
            .replace(R.id.fragment_holder_two, editToLetFragment)
            .commit()
    }

    private fun deleteToLet(toLet: ToLet, position: Int) {
        val consumer = APIService.getService(APIConsumer::class.java)
        val deleteToLet = consumer.deleteToLet(
            UserToken.getInstance(mContext).token!!,
            mParamHouseID!!,
            toLet.id!!
        )
        deleteToLet.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    AppToast.show(
                        mContext,
                        "To-let has been deleted successfully",
                        Toast.LENGTH_LONG
                    )
                } else if (!response.isSuccessful && response.errorBody() != null) {
                    rollbackToLetNotDeleted()
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
                        displayAPIErrorResponseDialog("Unable to delete to-let. Please try again later")
                    }
                } else {
                    rollbackToLetNotDeleted()
                    displayAPIErrorResponseDialog("Unable to delete to-let. Please try again later")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                rollbackToLetNotDeleted()
                displayAPIErrorResponseDialog("Unable to delete to-let. Please try again later")
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

            fun rollbackToLetNotDeleted() {
                mViewModel.addToLet(mParamHousePosition, position, toLet)
            }
        })
    }
}