package softwarica.sunilprasai.cuid10748110.rental.ui.houses

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
import softwarica.sunilprasai.cuid10748110.rental.model.House
import softwarica.sunilprasai.cuid10748110.rental.ui.add_house.AddHouseFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.edit_house.EditHouseFragment
import softwarica.sunilprasai.cuid10748110.rental.ui.shared_view_model.HouseViewModel
import softwarica.sunilprasai.cuid10748110.rental.ui.view_house.ViewHouseFragment
import softwarica.sunilprasai.cuid10748110.rental.utils.APIConsumer
import softwarica.sunilprasai.cuid10748110.rental.utils.APIService
import softwarica.sunilprasai.cuid10748110.rental.utils.AppToast
import softwarica.sunilprasai.cuid10748110.rental.utils.UserToken

private const val TAG = "HouseFragment"

class HousesFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener,
    HousesRVItemTouchListener.OnHouseTouch {

    private lateinit var mViewModel: HouseViewModel
    private lateinit var mNoHouseInfo: TextView
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mRecyclerViewAdapter: HousesRVA
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    private lateinit var mContext: Context

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.fragment_houses, container, false)
        mContext = requireContext()
        mNoHouseInfo = view.findViewById(R.id.noHouseInfo)
        mRecyclerView = view.findViewById(R.id.housesRV)
        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        mSwipeRefreshLayout.setOnRefreshListener(this)

        mRecyclerViewAdapter = HousesRVA()
        mRecyclerView.layoutManager = LinearLayoutManager(mContext)
        mRecyclerView.adapter = mRecyclerViewAdapter

        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }

        })
        mRecyclerView.addOnItemTouchListener(
            HousesRVItemTouchListener(
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
            ViewModelProvider(requireActivity()).get(HouseViewModel::class.java)

        mViewModel.getHouses().observe(requireActivity()) {
            Log.d(TAG, "onActivityCreated: houses list has changed")
            if (isAdded && it != null) {
                mSwipeRefreshLayout.isRefreshing = false
                mRecyclerViewAdapter.swipeHouses(it)
                if (it.size == 0) {
                    mNoHouseInfo.visibility = View.VISIBLE
                } else {
                    mNoHouseInfo.visibility = View.GONE
                }
            }
        }
        getUserHouses()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.addHouseOptionMenu -> {
                requireActivity().supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right)
                    .addToBackStack(AddHouseFragment::class.java.simpleName)
                    .replace(R.id.fragment_holder_two, AddHouseFragment()).commit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRefresh() {
        mSwipeRefreshLayout.isRefreshing = true
        getUserHouses()
    }

    override fun onHouseClick(position: Int) {
        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right)
            .addToBackStack(ViewHouseFragment::class.java.simpleName).replace(
                R.id.fragment_holder_two,
                ViewHouseFragment.newInstance(
                    position,
                    mViewModel.getHouse(position).id!!
                )
            ).commit()
    }

    override fun onHouseDeleteSelected(position: Int) {
        Log.d(TAG, "onHouseDeleteSelected: at position $position")
        val house = mViewModel.getHouses().value!![position]
        mViewModel.removeHouse(position)

        Snackbar.make(
            requireView(),
            "Click UNDO to rollback the deleted house",
            Snackbar.LENGTH_LONG
        ).setAction("UNDO") {
            mViewModel.addHouse(position, house)
        }.addCallback(object : Snackbar.Callback() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                super.onDismissed(transientBottomBar, event)
                if (DISMISS_EVENT_TIMEOUT == event || DISMISS_EVENT_SWIPE == event) {
                    deleteUserHouse(house, position)
                }
            }
        }).show()
    }

    override fun onHouseEditSelected(position: Int) {
        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right)
            .addToBackStack(EditHouseFragment::class.java.simpleName).replace(
                R.id.fragment_holder_two,
                EditHouseFragment.newInstance(
                    position,
                    mViewModel.getHouse(position).id!!
                )
            ).commit()
        mRecyclerViewAdapter.notifyItemChanged(position)
    }

    private fun getUserHouses() {
        val consumer = APIService.getService(APIConsumer::class.java)
        val getHouses = consumer.getHouses(UserToken.getInstance(mContext).token!!)
        getHouses.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val apiFetchedHouses = House.getHousesArrayListFromJSONArray(
                        JSONArray(
                            response.body()!!.string()
                        )
                    )
                    mViewModel.setHouses(apiFetchedHouses)
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
                        displayAPIErrorResponseDialog("Unable to get your house(s). Please try again later")
                    }
                } else {
                    displayAPIErrorResponseDialog("Unable to get your house(s). Please try again later")
                }
                mSwipeRefreshLayout.isRefreshing = false
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                mSwipeRefreshLayout.isRefreshing = false
                displayAPIErrorResponseDialog("Unable to get your house(s). Please try again later")
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
        })
    }

    private fun deleteUserHouse(house: House, position: Int) {
        val consumer: APIConsumer = APIService.getService(APIConsumer::class.java)
        val deleteHouse =
            consumer.deleteHouse(UserToken.getInstance(mContext).token!!, house.id!!)
        deleteHouse.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    AppToast.show(
                        mContext,
                        "Your house has been removed",
                        Toast.LENGTH_LONG
                    )
                } else if (!response.isSuccessful && response.errorBody() != null) {
                    rollbackHouseNotDeleted()
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
                        displayAPIErrorResponseDialog("Unable to delete your house. Please try again later")
                    }
                } else {
                    rollbackHouseNotDeleted()
                    displayAPIErrorResponseDialog("Unable to delete your house. Please try again later")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                rollbackHouseNotDeleted()
                displayAPIErrorResponseDialog("Unable to delete your house. Please try again later")
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

            fun rollbackHouseNotDeleted() {
                mViewModel.addHouse(position, house)
            }
        })
    }
}