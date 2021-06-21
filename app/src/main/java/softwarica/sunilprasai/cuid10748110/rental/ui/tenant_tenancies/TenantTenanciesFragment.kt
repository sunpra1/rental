package softwarica.sunilprasai.cuid10748110.rental.ui.tenant_tenancies

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
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
import softwarica.sunilprasai.cuid10748110.rental.ui.shared_view_model.TenantTenanciesViewModel
import softwarica.sunilprasai.cuid10748110.rental.ui.tenant_tenancies_history.TenantTenanciesHistoryFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.view_tenant_tenancy.ViewTenantTenancyFragment
import softwarica.sunilprasai.cuid10748110.rental.utils.APIConsumer
import softwarica.sunilprasai.cuid10748110.rental.utils.APIService
import softwarica.sunilprasai.cuid10748110.rental.utils.AppToast
import softwarica.sunilprasai.cuid10748110.rental.utils.UserToken

private const val TAG = "TenantTenanciesFragment"

class TenantTenanciesFragment : Fragment(), TenanciesRVItemTouchListener.OnTenantTenancyTouch,
    SwipeRefreshLayout.OnRefreshListener {
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mRecyclerViewAdapter: TenanciesRVA
    private lateinit var mNoTenancyInfo: TextView

    private lateinit var mViewModel: TenantTenanciesViewModel
    private lateinit var mContext: Context
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tenant_tenancies, container, false)
        mContext = requireContext()
        setHasOptionsMenu(true)
        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        mSwipeRefreshLayout.setOnRefreshListener(this)
        mRecyclerView = view.findViewById(R.id.tenantTenanciesRV)
        mRecyclerView.layoutManager = LinearLayoutManager(mContext)
        mRecyclerViewAdapter = TenanciesRVA()
        mRecyclerView.adapter = mRecyclerViewAdapter
        mRecyclerView.addOnItemTouchListener(
            TenanciesRVItemTouchListener(
                mContext,
                mRecyclerView,
                this
            )
        )

        mNoTenancyInfo = view.findViewById(R.id.noTenancyInfo)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mSwipeRefreshLayout.isRefreshing = true
        mViewModel = ViewModelProvider(requireActivity()).get(TenantTenanciesViewModel::class.java)
        mViewModel.getTenantTenancies().observe(requireActivity(), {
            updateView(it)
        })

        getTenantTenancies()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(
            TAG,
            "onOptionsItemSelected: ${item.itemId} and ${R.id.tenantTenancyHistoryOptionMenu}"
        )
        return when (item.itemId) {
            R.id.tenantTenancyHistoryOptionMenu -> {
                requireActivity().supportFragmentManager
                    .beginTransaction()
                    .addToBackStack(TenantTenanciesHistoryFragment::class.java.simpleName)
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right)
                    .replace(R.id.fragment_holder_one, TenantTenanciesHistoryFragment())
                    .commit()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateView(tenantTenancies: ArrayList<Tenancy>) {
        mRecyclerViewAdapter.setTenantTenancies(tenantTenancies)
        if (tenantTenancies.size > 0) {
            mRecyclerView.visibility = View.VISIBLE
            mNoTenancyInfo.visibility = View.GONE
        } else {
            mRecyclerView.visibility = View.GONE
            mNoTenancyInfo.visibility = View.VISIBLE
        }
    }

    private fun getTenantTenancies() {
        val consumer = APIService.getService(APIConsumer::class.java)
        val getTenantTenancies =
            consumer.getTenantTenancies(UserToken.getInstance(mContext).token!!)
        getTenantTenancies.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    val tenantTenanciesResponse = Tenancy.getTenancyArrayListFromJSONArray(
                        JSONArray(
                            response.body()!!.string()
                        )
                    )
                    mViewModel.setTenantTenancies(tenantTenanciesResponse)
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
                        displayAPIErrorResponseDialog("Unable to get your tenancies. Please try again later")
                    }
                } else {
                    displayAPIErrorResponseDialog("Unable to get your tenancies. Please try again later")
                }
                mSwipeRefreshLayout.isRefreshing = false
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                displayAPIErrorResponseDialog("Unable to get your tenancies. Please try again later")
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

    override fun onTenantTenancyClick(position: Int) {
        val tenancy = mViewModel.getTenantTenanciesValue()[position]
        val viewTenantTenancyFrag = ViewTenantTenancyFragment.newInstance(position, tenancy.id!!)
        requireActivity().supportFragmentManager
            .beginTransaction()
            .addToBackStack(ViewTenantTenancyFragment::class.java.simpleName)
            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right)
            .replace(R.id.fragment_holder_two, viewTenantTenancyFrag)
            .commit()
    }

    private fun deleteTenantTenancy(position: Int, tenancy: Tenancy) {
        val consumer = APIService.getService(APIConsumer::class.java)
        val deleteTenantTenancy =
            consumer.deleteTenantTenancy(UserToken.getInstance(mContext).token!!, tenancy.id!!)
        deleteTenantTenancy.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    AppToast.show(
                        mContext,
                        "Your tenancy has been deleted successfully",
                        Toast.LENGTH_LONG
                    )
                } else if (!response.isSuccessful && response.errorBody() != null) {
                    rollBackTenantTenancyNotDeleted()
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
                        displayAPIErrorResponseDialog("Unable to delete your tenancy. Please try again later.")
                    }
                } else {
                    rollBackTenantTenancyNotDeleted()
                    displayAPIErrorResponseDialog("Unable to delete your tenancy. Please try again later.")
                }
                mSwipeRefreshLayout.isRefreshing = false
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                rollBackTenantTenancyNotDeleted()
                displayAPIErrorResponseDialog("Unable to approve your tenancy. Please try again later.")
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

            private fun rollBackTenantTenancyNotDeleted() {
                mViewModel.addTenantTenancy(position, tenancy)
            }
        })
    }

    override fun onTenantTenancyDeleteSelected(position: Int) {
        val tenancy = mViewModel.getTenantTenanciesValue()[position]
        mViewModel.deleteTenantTenancy(position)
        Snackbar.make(
            requireView(),
            "Click UNDO to rollback the deleted tenancy",
            Snackbar.LENGTH_LONG
        )
            .setAction("UNDO") {
                mViewModel.addTenantTenancy(position, tenancy)
            }
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)
                    if (event == DISMISS_EVENT_SWIPE || event == DISMISS_EVENT_TIMEOUT) {
                        deleteTenantTenancy(position, tenancy)
                    }
                }
            }).show()
    }

    override fun onTenantTenancyApproveSelected(position: Int) {
        val unApprovedTenancy = mViewModel.getTenantTenanciesValue()[position]
        val approvedTenancy = Tenancy(unApprovedTenancy).apply { approved = true }
        mViewModel.replaceTenantTenancy(position, approvedTenancy)

        Snackbar.make(
            requireView(),
            "Click UNDO to rollback approved tenancy to unapproved",
            Snackbar.LENGTH_LONG
        )
            .setAction("UNDO") {
                mViewModel.replaceTenantTenancy(position, unApprovedTenancy)
            }
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)
                    if (event == DISMISS_EVENT_SWIPE || event == DISMISS_EVENT_TIMEOUT) {
                        approveTenantTenancy(position, unApprovedTenancy)
                    }
                }
            }).show()
    }

    private fun approveTenantTenancy(position: Int, unapprovedTenancy: Tenancy) {
        val consumer = APIService.getService(APIConsumer::class.java)
        val approveTenantTenancy = consumer.approveTenantTenancy(
            UserToken.getInstance(mContext).token!!,
            unapprovedTenancy.id!!
        )
        approveTenantTenancy.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    val tenantTenancyResponse = Tenancy(JSONObject(response.body()!!.string()))
                    mViewModel.replaceTenantTenancy(position, tenantTenancyResponse)
                    if (isAdded)
                        AppToast.show(
                            mContext,
                            "Tenancy has been approved successfully",
                            Toast.LENGTH_LONG
                        )
                } else if (!response.isSuccessful && response.errorBody() != null) {
                    rollbackTenantTenancyNotApproved()
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
                        displayAPIErrorResponseDialog("Unable to approve your tenancy. Please try again later.")
                    }
                } else {
                    rollbackTenantTenancyNotApproved()
                    displayAPIErrorResponseDialog("Unable to approve your tenancy. Please try again later.")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                rollbackTenantTenancyNotApproved()
                displayAPIErrorResponseDialog("Unable to approve your tenancy. Please try again later.")
            }

            private fun displayAPIErrorResponseDialog(message: String) {
                if (isAdded)
                    AlertDialog.Builder(mContext)
                        .setIcon(R.drawable.information)
                        .setTitle("INFORMATION")
                        .setMessage(message)
                        .setPositiveButton(
                            "OK"
                        ) { dialog, _ -> dialog!!.dismiss() }
                        .show()
            }

            private fun rollbackTenantTenancyNotApproved() {
                mViewModel.replaceTenantTenancy(position, unapprovedTenancy)
            }
        })
    }

    override fun onRefresh() {
        mSwipeRefreshLayout.isRefreshing = true
        getTenantTenancies()
    }
}