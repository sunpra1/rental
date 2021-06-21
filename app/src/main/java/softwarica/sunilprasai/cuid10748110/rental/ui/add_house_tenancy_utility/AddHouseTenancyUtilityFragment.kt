package softwarica.sunilprasai.cuid10748110.rental.ui.add_house_tenancy_utility

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
import softwarica.sunilprasai.cuid10748110.rental.model.Tenancy
import softwarica.sunilprasai.cuid10748110.rental.model.Utility
import softwarica.sunilprasai.cuid10748110.rental.ui.shared_view_model.HouseViewModel
import softwarica.sunilprasai.cuid10748110.rental.utils.*

private const val TAG = "AddUtilityFragment"
private const val ARG_PARAM_TENANCY_POSITION = "tenancyPosition"
private const val ARG_PARAM_TENANCY_ID = "tenancyId"
private const val ARG_PARAM_HOUSE_POSITION = "housePosition"
private const val ARG_PARAM_HOUSE_ID = "houseId"

class AddHouseTenancyUtilityFragment private constructor() : Fragment(), View.OnFocusChangeListener,
    View.OnClickListener {
    private var mParamTenancyPosition: Int = -1
    private var mParamTenancyID: String? = null
    private var mParamHousePosition: Int = -1
    private var mParamHouseID: String? = null

    private lateinit var mUtilityNameTil: TextInputLayout
    private lateinit var mUtilityNameEt: TextInputEditText

    private lateinit var mUtilityPriceTil: TextInputLayout
    private lateinit var mUtilityPriceEt: TextInputEditText

    private lateinit var mIsUtilityVariableSwitch: SwitchCompat
    private lateinit var mAddUtilityBtn: MaterialButton

    private lateinit var mViewModel: HouseViewModel
    private lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mParamHousePosition = it.getInt(ARG_PARAM_HOUSE_POSITION)
            mParamHouseID = it.getString(ARG_PARAM_HOUSE_ID)
            mParamTenancyPosition = it.getInt(ARG_PARAM_TENANCY_POSITION)
            mParamTenancyID = it.getString(ARG_PARAM_TENANCY_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_house_tenancy_utility, container, false)
        mContext = requireContext()
        mUtilityNameTil = view.findViewById(R.id.utilityNameTil)
        mUtilityNameEt = view.findViewById(R.id.utilityNameEt)
        mUtilityNameEt.onFocusChangeListener = this

        mUtilityPriceTil = view.findViewById(R.id.utilityPriceTil)
        mUtilityPriceEt = view.findViewById(R.id.utilityPriceEt)
        mUtilityPriceEt.onFocusChangeListener = this

        mIsUtilityVariableSwitch = view.findViewById(R.id.isUtilityVariableSwitch)
        mAddUtilityBtn = view.findViewById(R.id.addUtilityBtn)
        mAddUtilityBtn.setOnClickListener(this)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel = ViewModelProvider(requireActivity()).get(HouseViewModel::class.java)
        mViewModel.getHouses().observe(requireActivity()) {
            if (isAdded) {
                if (it != null && mParamHousePosition != -1 && mParamTenancyPosition != -1 && mParamTenancyPosition < it[mParamHousePosition].tenancies!!.size && (it[mParamHousePosition].id != mParamHouseID || it[mParamHousePosition].tenancies!![mParamTenancyPosition].id != mParamTenancyID)) {
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(
            housePosition: Int,
            houseId: String,
            tenantPosition: Int,
            tenantId: String
        ) =
            AddHouseTenancyUtilityFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM_HOUSE_POSITION, housePosition)
                    putString(ARG_PARAM_HOUSE_ID, houseId)
                    putInt(ARG_PARAM_TENANCY_POSITION, tenantPosition)
                    putString(ARG_PARAM_TENANCY_ID, tenantId)
                }
            }
    }

    private fun addUtility(utility: Utility) {
        val requestBody =
            RequestBody.create(MediaType.parse(TYPE_JSON), utility.getJSONObject().toString())
        val consumer = APIService.getService(APIConsumer::class.java)
        val addUtility = consumer.addUtility(
            UserToken.getInstance(mContext).token!!,
            mParamHouseID!!,
            mParamTenancyID!!,
            requestBody
        )
        addUtility.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    val tenancy = Tenancy(JSONObject(response.body()!!.string()))
                    mViewModel.replaceTenancy(
                        mParamHousePosition,
                        mParamTenancyPosition,
                        tenancy
                    )
                    AppToast.show(
                        mContext,
                        "New Utility ${utility.name!!} added successfully",
                        Toast.LENGTH_LONG
                    )
                    requireActivity().supportFragmentManager.popBackStack()
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
                        displayAPIErrorResponseDialog("Unable to add utility. Please try again later")
                    }
                } else {
                    displayAPIErrorResponseDialog("Unable to add utility. Please try again later")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                displayAPIErrorResponseDialog("Unable to add utility. Please try again later")
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

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (v != null) {
            when (v.id) {
                mUtilityNameEt.id -> {
                    if (!hasFocus) {
                        mUtilityNameEt.text?.let {
                            if (it.trim().isEmpty()) {
                                mUtilityNameTil.apply {
                                    isErrorEnabled = true
                                    error = "Utility name is required"
                                }
                                VibrateView.vibrate(mContext, R.anim.shake, mUtilityNameTil)
                            }
                        }
                    } else {
                        mUtilityNameTil.isErrorEnabled = false
                    }
                }

                mUtilityPriceEt.id -> {
                    if (!hasFocus) {
                        mUtilityPriceEt.text?.let {
                            when {
                                it.trim().isEmpty() -> {
                                    mUtilityPriceTil.apply {
                                        isErrorEnabled = true
                                        error = "Utility price is required"
                                    }
                                    VibrateView.vibrate(
                                        mContext,
                                        R.anim.shake,
                                        mUtilityPriceTil
                                    )
                                }
                                !it.trim().isDigitsOnly() -> {
                                    mUtilityPriceTil.apply {
                                        isErrorEnabled = true
                                        error = "Utility price must be numeric"
                                    }
                                }
                                else -> null
                            }
                        }
                    } else {
                        mUtilityPriceTil.isErrorEnabled = false
                    }
                }
            }
        }
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                mAddUtilityBtn.id -> {
                    if (validate()) {
                        val utility = Utility().apply {
                            name = mUtilityNameEt.text!!.trim().toString()
                            isVariableCost = mIsUtilityVariableSwitch.isChecked
                            price = mUtilityPriceEt.text!!.trim().toString().toInt()
                        }
                        addUtility(utility)
                    }
                }
            }
        }
    }

    private fun validate(): Boolean {
        var isValid = true
        mUtilityNameEt.text?.let {
            if (it.trim().isEmpty()) {
                isValid = false
                mUtilityNameTil.apply {
                    isErrorEnabled = true
                    error = "Utility name is required"
                }
            }
        }
        mUtilityPriceEt.text?.let {
            if (it.trim().isEmpty()) {
                isValid = false
                mUtilityPriceTil.apply {
                    isErrorEnabled = true
                    error = "Utility price is required"
                }
            }
        }

        if (!isValid)
            VibrateView.vibrate(
                mContext,
                R.anim.shake,
                requireView().findViewById(R.id.formCardView)
            )

        return isValid
    }
}