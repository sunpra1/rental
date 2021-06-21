package softwarica.sunilprasai.cuid10748110.rental.ui.to_lets

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.textfield.TextInputEditText
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import softwarica.sunilprasai.cuid10748110.rental.R
import softwarica.sunilprasai.cuid10748110.rental.model.TO_LET_ROOM_TYPE
import softwarica.sunilprasai.cuid10748110.rental.model.ToLet
import softwarica.sunilprasai.cuid10748110.rental.ui.shared_view_model.ToLetsViewModel
import softwarica.sunilprasai.cuid10748110.rental.ui.view_to_lets_to_let.ViewToLetsToLetFragment
import softwarica.sunilprasai.cuid10748110.rental.utils.APIConsumer
import softwarica.sunilprasai.cuid10748110.rental.utils.APIService
import softwarica.sunilprasai.cuid10748110.rental.utils.UserToken

private const val TAG = "ToLetsFragment"

class ToLetsFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener,
    ToLetsRVItemTouchListener.OnToLetTouch {
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    private lateinit var mNoToLetsInfo: TextView
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mRecyclerViewAdapter: ToLetsRVA
    private lateinit var mViewModel: ToLetsViewModel
    private lateinit var mContext: Context

    //for filter
    private var mRoomType: String = "null"
    private var mRoomCount: String = "null"
    private var mMinRentAmount: String = "null"
    private var mMaxRentAmount: String = "null"
    private var mAddress: String = "null"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.fragment_to_lets, container, false)
        mContext = requireContext()
        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        mSwipeRefreshLayout.setOnRefreshListener(this)
        mNoToLetsInfo = view.findViewById(R.id.noToLetsInfo)
        mRecyclerView = view.findViewById(R.id.searchToLetsRV)
        mRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        mRecyclerViewAdapter = ToLetsRVA()
        mRecyclerView.adapter = mRecyclerViewAdapter
        mRecyclerView.addOnItemTouchListener(
            ToLetsRVItemTouchListener(
                mContext,
                mRecyclerView,
                this
            )
        )
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel = ViewModelProvider(requireActivity()).get(ToLetsViewModel::class.java)
        mViewModel.getToLets().observe(requireActivity(), {
            if (isAdded) {
                if (it != null) {
                    updateView(it)
                }
            }
        })
        searchToLets()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.let {
            when (it.itemId) {
                R.id.filterToLetsOptionMenu -> {
                    val filterDialogView = layoutInflater.inflate(R.layout.filter_dialog, null)
                    val clearFilter: View = filterDialogView.findViewById(R.id.clearFilter)
                    val filterToLetByRoomTypeSpn: Spinner =
                        filterDialogView.findViewById(R.id.filterToLetByRoomType)
                    val filterToLetByRoomCountEt: TextInputEditText =
                        filterDialogView.findViewById(R.id.filterToLetByRoomCount)
                    val filterToLetByMinRentAmountEt: TextInputEditText =
                        filterDialogView.findViewById(R.id.filterToLetByMinRentAmount)
                    val filterToLetByMaxRentAmountEt: TextInputEditText =
                        filterDialogView.findViewById(R.id.filterToLetByMaxRentAmount)
                    val filterToLetByAddressEt: TextInputEditText =
                        filterDialogView.findViewById(R.id.filterToLetByAddress)

                    clearFilter.setOnClickListener {
                        filterToLetByRoomTypeSpn.setSelection(0)
                        filterToLetByRoomCountEt.text = null
                        filterToLetByMinRentAmountEt.text = null
                        filterToLetByMaxRentAmountEt.text = null
                        filterToLetByAddressEt.text = null
                    }

                    filterToLetByRoomTypeSpn.adapter = object : ArrayAdapter<String>(
                        mContext,
                        R.layout.support_simple_spinner_dropdown_item,
                        TO_LET_ROOM_TYPE
                    ) {
                        override fun isEnabled(position: Int): Boolean {
                            return if (position == 0) false
                            else super.isEnabled(position)
                        }

                        override fun areAllItemsEnabled(): Boolean = false
                    }
                    filterToLetByRoomCountEt.setText(if (mRoomCount != "null") mRoomCount else null)
                    filterToLetByMinRentAmountEt.setText(if (mMinRentAmount != "null") mMinRentAmount else null)
                    filterToLetByMaxRentAmountEt.setText(if (mMaxRentAmount != "null") mMaxRentAmount else null)
                    filterToLetByAddressEt.setText(if (mAddress != "null") mAddress else null)
                    filterToLetByRoomTypeSpn.setSelection(TO_LET_ROOM_TYPE.indexOf(mRoomType))

                    AlertDialog.Builder(mContext)
                        .setIcon(R.drawable.filter)
                        .setTitle("FILTER TO-LETS")
                        .setView(filterDialogView)
                        .setPositiveButton(
                            "APPLY"
                        ) { _, _ ->
                            mRoomType =
                                if (filterToLetByRoomTypeSpn.selectedItemPosition > 0) TO_LET_ROOM_TYPE[filterToLetByRoomTypeSpn.selectedItemPosition] else "null"
                            mRoomCount =
                                if (filterToLetByRoomCountEt.text!!.isNotEmpty() && filterToLetByRoomCountEt.text!!.isDigitsOnly() && filterToLetByRoomCountEt.text!!.toString()
                                        .toInt() > 0
                                ) filterToLetByRoomCountEt.text!!.toString() else "null"
                            mMinRentAmount =
                                if (filterToLetByMinRentAmountEt.text!!.isNotEmpty() && filterToLetByMinRentAmountEt.text!!.isDigitsOnly() && filterToLetByMinRentAmountEt.text!!.toString()
                                        .toInt() > 0
                                ) filterToLetByMinRentAmountEt.text!!.toString() else "null"
                            mMaxRentAmount =
                                if (filterToLetByMaxRentAmountEt.text!!.isNotEmpty() && filterToLetByMaxRentAmountEt.text!!.isDigitsOnly() && filterToLetByMaxRentAmountEt.text!!.toString()
                                        .toInt() > 0
                                ) filterToLetByMaxRentAmountEt.text!!.toString() else "null"
                            mAddress = if (filterToLetByAddressEt.text!!.isNotEmpty()
                            ) filterToLetByAddressEt.text!!.toString() else "null"

                            searchToLets()
                        }
                        .setNegativeButton(
                            "CANCEL"
                        ) { dialog, _ -> dialog.cancel() }
                        .show()
                    true
                }
                else -> null
            }
        } ?: super.onOptionsItemSelected(item)
    }

    private fun searchToLets(
        page: Int = 1,
        limit: Int = 100,
        roomType: String = mRoomType,
        roomCount: String = mRoomCount,
        minRentAmount: String = mMinRentAmount,
        maxRentAmount: String = mMaxRentAmount,
        address: String = mAddress
    ) {
        val apiConsumer = APIService.getService(APIConsumer::class.java)
        val searchToLets = apiConsumer.searchToLets(
            UserToken.getInstance(mContext).token!!,
            page,
            limit,
            roomType,
            roomCount,
            minRentAmount,
            maxRentAmount,
            address
        )
        searchToLets.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    val searchToLetsResponse =
                        ToLet.getToLetArrayListFromJSONArray(JSONArray(response.body()!!.string()))
                    mViewModel.setToLets(searchToLetsResponse)
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
                        displayAPIErrorResponseDialog("Unable to get to-lets. Please try again later")
                    }
                } else {
                    displayAPIErrorResponseDialog("Unable to get to-lets. Please try again later")
                }
                mSwipeRefreshLayout.isRefreshing = false
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                mSwipeRefreshLayout.isRefreshing = false
                displayAPIErrorResponseDialog("Unable to get to-lets. Please try again later")
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

    private fun updateView(toLets: ArrayList<ToLet>) {
        if (toLets.size > 0) {
            mNoToLetsInfo.visibility = View.GONE
            mRecyclerView.visibility = View.VISIBLE
            mRecyclerViewAdapter.setToLets(toLets)
        } else {
            mRecyclerView.visibility = View.GONE
            mNoToLetsInfo.visibility = View.VISIBLE
        }
    }

    override fun onRefresh() {
        mSwipeRefreshLayout.isRefreshing = true
        searchToLets()
    }

    override fun onToLetClicked(position: Int) {
        Log.d(TAG, "onToLetClicked: to let is clicked")
        requireActivity().supportFragmentManager
            .beginTransaction()
            .addToBackStack(ViewToLetsToLetFragment::class.java.simpleName)
            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right)
            .replace(
                R.id.fragment_holder_two,
                ViewToLetsToLetFragment.newInstance(position, mViewModel.getToLet(position).id!!)
            )
            .commit()
    }
}