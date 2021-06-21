package softwarica.sunilprasai.cuid10748110.rental.ui.house_tenancies_history

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import softwarica.sunilprasai.cuid10748110.rental.R
import softwarica.sunilprasai.cuid10748110.rental.model.House
import softwarica.sunilprasai.cuid10748110.rental.model.Tenancy
import softwarica.sunilprasai.cuid10748110.rental.ui.shared_view_model.HouseViewModel
import softwarica.sunilprasai.cuid10748110.rental.ui.view_house_tenancy_history.ViewHouseTenancyHistoryFragment
import softwarica.sunilprasai.cuid10748110.rental.utils.APIConsumer
import softwarica.sunilprasai.cuid10748110.rental.utils.APIService
import softwarica.sunilprasai.cuid10748110.rental.utils.AppToast
import softwarica.sunilprasai.cuid10748110.rental.utils.UserToken

private const val TAG = "ViewHouseTenanciesHisto"
private const val ARG_PARAM_HOUSE_POSITION = "position"
private const val ARG_PARAM_HOUSE_ID = "id"

class HouseTenanciesHistoryFragment : Fragment(),
    TenanciesHistoryRVItemTouchListener.OnTenancyHistoryTouch,
    SwipeRefreshLayout.OnRefreshListener {
    private var mParamHousePosition: Int = -1
    private var mParamHouseID: String? = null

    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout

    private lateinit var mNoTenanciesHistoryInfo: TextView
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mRecyclerViewAdapter: TenanciesHistoryRVA

    private lateinit var mViewModel: HouseViewModel
    private lateinit var mHouse: House
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
        val view =
            inflater.inflate(R.layout.fragment_house_tenancies_history, container, false)
        mContext = requireContext()

        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        mSwipeRefreshLayout.setOnRefreshListener(this)

        mNoTenanciesHistoryInfo = view.findViewById(R.id.noTenanciesHistoryInfo)
        mRecyclerView = view.findViewById(R.id.recyclerView)
        mRecyclerView.layoutManager = LinearLayoutManager(mContext)
        mRecyclerViewAdapter = TenanciesHistoryRVA()
        mRecyclerView.adapter = mRecyclerViewAdapter
        mRecyclerView.addOnItemTouchListener(
            TenanciesHistoryRVItemTouchListener(
                mContext,
                mRecyclerView,
                this
            )
        )
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

    companion object {
        @JvmStatic
        fun newInstance(housePosition: Int, houseID: String) =
            HouseTenanciesHistoryFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM_HOUSE_POSITION, housePosition)
                    putString(ARG_PARAM_HOUSE_ID, houseID)
                }
            }
    }

    private fun updateView() {
        if (mHouse.tenanciesHistory!!.size > 0) {
            mNoTenanciesHistoryInfo.visibility = View.GONE
            mRecyclerViewAdapter.setTenancyHistory(mHouse.tenanciesHistory!!)
        } else {
            mNoTenanciesHistoryInfo.visibility = View.VISIBLE
        }
    }

    override fun onTenancyHistoryClick(position: Int) {
        requireActivity().supportFragmentManager
            .beginTransaction()
            .addToBackStack(ViewHouseTenancyHistoryFragment::class.java.simpleName)
            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right)
            .replace(
                R.id.fragment_holder_two,
                ViewHouseTenancyHistoryFragment.newInstance(
                    mParamHousePosition,
                    mParamHouseID!!,
                    position,
                    mViewModel.getTenancyHistory(mParamHousePosition, position).id!!
                )
            )
            .commit()
    }

    override fun onTenancyHistoryMakeActiveSelected(position: Int) {
        val tenancy = mViewModel.getTenancyHistory(mParamHousePosition, position)
        mViewModel.deleteTenancyHistory(mParamHousePosition, position)

        Snackbar.make(
            requireView(),
            "Click UNDO to rollback tenancy history made active",
            Snackbar.LENGTH_LONG
        )
            .setAction("UNDO") {
                mViewModel.addTenancyHistory(mParamHousePosition, position, tenancy)
            }
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)
                    if (event == DISMISS_EVENT_TIMEOUT || event == DISMISS_EVENT_SWIPE) {
                        makeTenancyActive(tenancy)
                    }
                }
            })
            .show()
    }

    override fun onTenancyHistoryDeleteSelected(position: Int) {
        val tenancy = mViewModel.getTenancyHistory(mParamHousePosition, position)
        mViewModel.deleteTenancyHistory(mParamHousePosition, position)

        Snackbar.make(
            requireView(),
            "Click UNDO to rollback deleted tenancy history",
            Snackbar.LENGTH_LONG
        )
            .setAction("UNDO") {
                mViewModel.addTenancyHistory(mParamHousePosition, position, tenancy)
            }
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)
                    if (event == DISMISS_EVENT_TIMEOUT || event == DISMISS_EVENT_SWIPE) {
                        deleteTenancyHistory(tenancy, position)
                    }
                }
            })
            .show()
    }

    private fun makeTenancyActive(tenancy: Tenancy) {
        val consumer = APIService.getService(APIConsumer::class.java)
        val makeTenancyActive = consumer.makeTenancyActive(
            UserToken.getInstance(mContext).token!!,
            mParamHouseID!!,
            tenancy.id!!
        )
        makeTenancyActive.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    val houseResponse = House(JSONObject(response.body()!!.string()))
                    mViewModel.replaceHouse(mParamHousePosition, houseResponse)
                    AppToast.show(
                        mContext,
                        "Tenancy of ${tenancy.tenant!!.fullName!!} has been made active successfully",
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
                        displayAPIErrorResponseDialog("Unable to make tenancy of ${tenancy.tenant!!.fullName!!} active. Please try again later")
                    }
                } else {
                    displayAPIErrorResponseDialog("Unable to make tenancy of ${tenancy.tenant!!.fullName!!} active. Please try again later")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                displayAPIErrorResponseDialog("Unable to make tenancy of ${tenancy.tenant!!.fullName!!} active. Please try again later")
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

    private fun deleteTenancyHistory(tenancy: Tenancy, position: Int) {
        val consumer = APIService.getService(APIConsumer::class.java)
        val deleteTenancyHistory = consumer.deleteTenancyHistory(
            UserToken.getInstance(mContext).token!!,
            mParamHouseID!!,
            tenancy.id!!
        )
        deleteTenancyHistory.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    AppToast.show(
                        mContext,
                        "Tenancy history has been deleted successfully",
                        Toast.LENGTH_LONG
                    )
                } else if (!response.isSuccessful && response.errorBody() != null) {
                    rollbackTenancyHistoryNotDeleted()
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
                        displayAPIErrorResponseDialog("Unable to delete tenancy history. Please try again later")
                    }
                } else {
                    rollbackTenancyHistoryNotDeleted()
                    displayAPIErrorResponseDialog("Unable to delete tenancy history. Please try again later")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                rollbackTenancyHistoryNotDeleted()
                displayAPIErrorResponseDialog("Unable to delete tenancy history. Please try again later")
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

            fun rollbackTenancyHistoryNotDeleted() {
                mViewModel.addTenancyHistory(mParamHousePosition, position, tenancy)
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
}