package softwarica.sunilprasai.cuid10748110.rental.ui.tenant_tenancies_history

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
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import softwarica.sunilprasai.cuid10748110.rental.R
import softwarica.sunilprasai.cuid10748110.rental.model.Tenancy
import softwarica.sunilprasai.cuid10748110.rental.ui.shared_view_model.TenantTenanciesHistoryViewModel
import softwarica.sunilprasai.cuid10748110.rental.ui.view_tenant_tenancy_history.ViewTenantTenancyHistoryFragment
import softwarica.sunilprasai.cuid10748110.rental.utils.APIConsumer
import softwarica.sunilprasai.cuid10748110.rental.utils.APIService
import softwarica.sunilprasai.cuid10748110.rental.utils.AppToast
import softwarica.sunilprasai.cuid10748110.rental.utils.UserToken

class TenantTenanciesHistoryFragment : Fragment(),
    TenancyHistoryRVItemTouchListener.OnTenancyHistoryTouch,
    SwipeRefreshLayout.OnRefreshListener {
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout

    private lateinit var mNoTenanciesHistoryInfo: TextView
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mRecyclerViewAdapter: TenanciesHistoryRVA

    private lateinit var mViewModel: TenantTenanciesHistoryViewModel
    private lateinit var mContext: Context

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            inflater.inflate(R.layout.fragment_tenant_tenancies_history, container, false)
        mContext = requireContext()

        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        mSwipeRefreshLayout.setOnRefreshListener(this)

        mNoTenanciesHistoryInfo = view.findViewById(R.id.noTenanciesHistoryInfo)
        mRecyclerView = view.findViewById(R.id.recyclerView)
        mRecyclerView.layoutManager = LinearLayoutManager(mContext)
        mRecyclerViewAdapter = TenanciesHistoryRVA()
        mRecyclerView.adapter = mRecyclerViewAdapter
        mRecyclerView.addOnItemTouchListener(
            TenancyHistoryRVItemTouchListener(
                mContext,
                mRecyclerView,
                this
            )
        )
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel =
            ViewModelProvider(requireActivity()).get(TenantTenanciesHistoryViewModel::class.java)

        mViewModel.getTenantTenanciesHistory().observe(requireActivity()) {
            if (isAdded) {
                if (it != null) {
                    updateView(it)
                } else {
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        }
        getTenantTenanciesHistory()
    }

    private fun updateView(tenantTenancyHistory: ArrayList<Tenancy>) {
        if (tenantTenancyHistory.size > 0) {
            mNoTenanciesHistoryInfo.visibility = View.GONE
            mRecyclerViewAdapter.setTenancyHistory(tenantTenancyHistory)
        } else {
            mNoTenanciesHistoryInfo.visibility = View.VISIBLE
        }
    }

    override fun onTenancyHistoryClick(position: Int) {
        requireActivity().supportFragmentManager
            .beginTransaction()
            .addToBackStack(ViewTenantTenancyHistoryFragment::class.java.simpleName)
            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right)
            .replace(
                R.id.fragment_holder_two,
                ViewTenantTenancyHistoryFragment.newInstance(
                    position,
                    mViewModel.getTenantTenancyHistory(position).id!!
                )
            )
            .commit()
    }

    override fun onTenancyHistoryDeleteSelected(position: Int) {
        val tenancy = mViewModel.getTenantTenancyHistory(position)
        mViewModel.deleteTenantTenancyHistory(position)

        Snackbar.make(
            requireView(),
            "Click UNDO to rollback deleted tenancy history",
            Snackbar.LENGTH_LONG
        )
            .setAction("UNDO") {
                mViewModel.addTenantTenancyHistory(position, tenancy)
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

    private fun getTenantTenanciesHistory() {
        val consumer = APIService.getService(APIConsumer::class.java)
        val getTenantTenanciesHistory =
            consumer.getTenantTenanciesHistory(UserToken.getInstance(mContext).token!!)
        getTenantTenanciesHistory.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    val tenantTenanciesResponse = Tenancy.getTenancyArrayListFromJSONArray(
                        JSONArray(
                            response.body()!!.string()
                        )
                    )
                    mViewModel.setTenantTenanciesHistory(tenantTenanciesResponse)
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
                        displayAPIErrorResponseDialog("Unable to get your tenancies history. Please try again later")
                    }
                } else {
                    displayAPIErrorResponseDialog("Unable to get your tenancies history. Please try again later")
                }
                mSwipeRefreshLayout.isRefreshing = false
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                displayAPIErrorResponseDialog("Unable to get your tenancies history. Please try again later")
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

    private fun deleteTenancyHistory(tenancy: Tenancy, position: Int) {
        val consumer = APIService.getService(APIConsumer::class.java)
        val deleteTenantTenancyHistory = consumer.deleteTenantTenanciesHistory(
            UserToken.getInstance(mContext).token!!,
            tenancy.id!!
        )
        deleteTenantTenancyHistory.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    AppToast.show(
                        mContext,
                        "Your tenancy history has been deleted successfully",
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
                        displayAPIErrorResponseDialog("Unable to delete your tenancy history. Please try again later")
                    }
                } else {
                    rollbackTenancyHistoryNotDeleted()
                    displayAPIErrorResponseDialog("Unable to delete your tenancy history. Please try again later")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                rollbackTenancyHistoryNotDeleted()
                displayAPIErrorResponseDialog("Unable to delete your tenancy history. Please try again later")
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
                mViewModel.addTenantTenancyHistory(position, tenancy)
            }
        })
    }

    override fun onRefresh() {
        mSwipeRefreshLayout.isRefreshing = true
        getTenantTenanciesHistory()
    }
}