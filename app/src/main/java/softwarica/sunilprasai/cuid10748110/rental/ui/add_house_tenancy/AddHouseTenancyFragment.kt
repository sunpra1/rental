package softwarica.sunilprasai.cuid10748110.rental.ui.add_house_tenancy

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.Patterns
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import softwarica.sunilprasai.cuid10748110.rental.R
import softwarica.sunilprasai.cuid10748110.rental.model.TENANCY_RENT_ACCRUES_AT
import softwarica.sunilprasai.cuid10748110.rental.model.TENANCY_ROOM_TYPE
import softwarica.sunilprasai.cuid10748110.rental.model.Tenancy
import softwarica.sunilprasai.cuid10748110.rental.model.User
import softwarica.sunilprasai.cuid10748110.rental.ui.edit_house.SelectedImagesRVA
import softwarica.sunilprasai.cuid10748110.rental.ui.shared_view_model.HouseViewModel
import softwarica.sunilprasai.cuid10748110.rental.utils.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

private const val TAG = "AddTenancyFragment"

private const val REQUEST_ACCESS_EXTERNAL_STORAGE: Int = 3001
private const val REQUEST_CODE_SELECT_PHOTOS: Int = 3002
private const val ARG_PARAM_HOUSE_POSITION = "position"
private const val ARG_PARAM_HOUSE_ID = "id"

class AddHouseTenancyFragment private constructor() : Fragment(), View.OnClickListener,
    View.OnFocusChangeListener, View.OnKeyListener,
    SelectedImagesRVA.OnSelectedImageDeleteClickListener {
    private var mParamHousePosition: Int = -1
    private var mParamHouseID: String? = null

    private lateinit var mViewModel: HouseViewModel

    private lateinit var mTenantPhoneOrEmailTil: TextInputLayout
    private lateinit var mTenantPhoneOrEmailEt: TextInputEditText

    private lateinit var mRoomTypeTil: TextInputLayout
    private lateinit var mRoomTypeSpn: Spinner

    private lateinit var mRoomCountTil: TextInputLayout
    private lateinit var mRoomCountEt: TextInputEditText

    private lateinit var mRentAccrueTil: TextInputLayout
    private lateinit var mRentAccrueSpn: Spinner

    private lateinit var mRentAmountTil: TextInputLayout
    private lateinit var mRentAmountEt: TextInputEditText

    private lateinit var mSelectImagesBtn: MaterialButton
    private lateinit var mSelectedImagesRVTil: TextInputLayout

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mRecyclerViewAdapter: SelectedImagesRVA

    private lateinit var mAddTenantBtn: MaterialButton

    private lateinit var mTenantDetails: TextView

    private var mIsValidPhone = false
    private var mIsValidEmail = false
    private var mIsValidUser = false
    private lateinit var mTenancyStartDateTil: TextInputLayout
    private lateinit var mTenancyStartDateCV: CalendarView
    private var mSelectedTenancyStartDate: Date? = null

    private var mSelectedImagesList: ArrayList<Uri> = ArrayList()
    private val mRentAccrueSpnItemSelectedListener = object : OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            if (position > 0) {
                mRentAccrueTil.isErrorEnabled = false
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            mRentAccrueTil.apply {
                isErrorEnabled = false
                error = "When tenant rent gets accrued is required"
            }
        }
    }

    private val mRoomTypeSpnItemSelectedListener = object : OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            if (position > 0) {
                mRoomTypeTil.isErrorEnabled = false
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            mRoomTypeTil.apply {
                isErrorEnabled = false
                error = "Tenancy room type is required"
            }
        }
    }
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
        val view = inflater.inflate(R.layout.fragment_add_house_tenancy, container, false)
        mContext = requireContext()
        mSelectImagesBtn = view.findViewById(R.id.selectImagesBtn)
        mSelectImagesBtn.setOnClickListener(this)

        mSelectedImagesRVTil = view.findViewById(R.id.selectedImagesRVTil)
        mRecyclerView = view.findViewById(R.id.selectedImagesRV)
        mRecyclerViewAdapter = SelectedImagesRVA()
        mRecyclerViewAdapter.setOnSelectedImageDeleteClickListener(this)
        mRecyclerView.layoutManager = GridLayoutManager(mContext, 2)
        mRecyclerView.adapter = mRecyclerViewAdapter

        mRentAccrueTil = view.findViewById(R.id.rentAccrueTil)
        mRentAccrueSpn = view.findViewById(R.id.rentAccrueSpn)
        mRentAccrueSpn.onItemSelectedListener = mRentAccrueSpnItemSelectedListener


        mTenantPhoneOrEmailTil = view.findViewById(R.id.tenantPhoneOrEmailTil)
        mTenantPhoneOrEmailEt = view.findViewById(R.id.tenantPhoneOrEmailEt)
        mTenantPhoneOrEmailEt.setOnKeyListener(this)
        mTenantPhoneOrEmailEt.onFocusChangeListener = this

        mRoomTypeTil = view.findViewById(R.id.roomTypeTil)
        mRoomTypeSpn = view.findViewById(R.id.roomTypeSpn)
        mRoomTypeSpn.onItemSelectedListener = mRoomTypeSpnItemSelectedListener

        mRoomCountTil = view.findViewById(R.id.roomCountTil)
        mRoomCountEt = view.findViewById(R.id.roomCountEt)
        mRoomCountEt.onFocusChangeListener = this

        mRentAmountTil = view.findViewById(R.id.rentAmountTil)
        mRentAmountEt = view.findViewById(R.id.rentAmountEt)
        mRentAmountEt.onFocusChangeListener = this

        mAddTenantBtn = view.findViewById(R.id.addTenantBtn)
        mAddTenantBtn.setOnClickListener(this)

        mTenantDetails = view.findViewById(R.id.tenantDetails)
        mTenancyStartDateTil = view.findViewById(R.id.tenancyStartsFromTil)
        mTenancyStartDateCV = view.findViewById(R.id.tenancyStartsFromCV)
        mTenancyStartDateCV.setOnDateChangeListener { _, year, month, dayOfMonth ->
            mTenancyStartDateTil.isErrorEnabled = false
            mSelectedTenancyStartDate = Calendar.getInstance().let {
                it.set(year, month, dayOfMonth)
                it.time
            }
        }
        val mRentAccrueSpnItemsAdapter: ArrayAdapter<String> = object : ArrayAdapter<String>(
            mContext, R.layout.support_simple_spinner_dropdown_item, TENANCY_RENT_ACCRUES_AT
        ) {
            override fun isEnabled(position: Int): Boolean {
                return if (position == 0) {
                    false
                } else {
                    super.isEnabled(position)
                }
            }

            override fun areAllItemsEnabled(): Boolean = false
        }

        val mRoomTypeSpnItemsAdapter: ArrayAdapter<String> = object : ArrayAdapter<String>(
            mContext, R.layout.support_simple_spinner_dropdown_item, TENANCY_ROOM_TYPE
        ) {
            override fun isEnabled(position: Int): Boolean {
                return if (position == 0) {
                    false
                } else {
                    super.isEnabled(position)
                }
            }

            override fun areAllItemsEnabled(): Boolean = false
        }
        mRentAccrueSpn.adapter = mRentAccrueSpnItemsAdapter
        mRoomTypeSpn.adapter = mRoomTypeSpnItemsAdapter

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel = ViewModelProvider(requireActivity()).get(HouseViewModel::class.java)

        mViewModel.getHouses().observe(requireActivity()) {
            if (isAdded) {
                if (it != null && mParamHousePosition != -1 && mParamHousePosition < it.size && it[mParamHousePosition].id != mParamHouseID) {
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(position: Int, id: String) =
            AddHouseTenancyFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM_HOUSE_POSITION, position)
                    putString(ARG_PARAM_HOUSE_ID, id)
                }
            }
    }

    override fun onClick(v: View?) {
        v?.let {
            when (it.id) {
                mSelectImagesBtn.id -> {
                    if (ActivityCompat.checkSelfPermission(
                            mContext,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        val intent = Intent(Intent.ACTION_PICK).apply {
                            type = "image/*"
                            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                        }
                        startActivityForResult(
                            intent,
                            REQUEST_CODE_SELECT_PHOTOS
                        )
                    } else {
                        handleStoragePermissions()
                    }
                }

                mAddTenantBtn.id -> {
                    if (validate()) {
                        val tenancy = Tenancy().apply {
                            startDate = mSelectedTenancyStartDate!!
                            tenant = User().apply {
                                val phoneOrEmail = mTenantPhoneOrEmailEt.text!!.trim().toString()
                                if (mIsValidPhone)
                                    phone = phoneOrEmail.toLong()

                                if (mIsValidEmail)
                                    email = phoneOrEmail
                            }

                            startDate = mSelectedTenancyStartDate!!
                            amount = mRentAmountEt.text!!.trim().toString().toInt()
                            roomCount = mRoomCountEt.text!!.trim().toString().toInt()
                            roomType =
                                Tenancy.getRoomType(TENANCY_ROOM_TYPE[mRoomTypeSpn.selectedItemPosition])
                            accrue =
                                Tenancy.getRentAccruedAt(TENANCY_RENT_ACCRUES_AT[mRentAccrueSpn.selectedItemPosition])
                        }

                        addTenancy(tenancy)
                    }
                }
            }
        }
    }

    private fun handleStoragePermissions() {
        Log.d(TAG, "handlePermissions: ")
        if (shouldShowRequestPermissionRationale(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            AlertDialog.Builder(mContext).apply {
                setIcon(R.drawable.information)
                setTitle("Information")
                setMessage("To be able to upload the image, this app requires permission to access file storage.\nDo you want to provide file storage access?")
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
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_ACCESS_EXTERNAL_STORAGE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionsResult: $requestCode")
        if (requestCode == REQUEST_ACCESS_EXTERNAL_STORAGE && ContextCompat.checkSelfPermission(
                mContext,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "onRequestPermissionsResult: permission to access external storage granted")
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            startActivityForResult(
                intent,
                REQUEST_CODE_SELECT_PHOTOS
            )
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_SELECT_PHOTOS && resultCode == Activity.RESULT_OK && data != null) {
            var isAllImagesValid = true
            if (data.clipData != null && data.clipData!!.itemCount > 0) {
                mSelectedImagesRVTil.isErrorEnabled = false
                mSelectedImagesList.clear()

                data.clipData?.let {
                    for (i in 0 until it.itemCount) {
                        val roomImageUri = it.getItemAt(i).uri
                        roomImageUri?.let { uri ->
                            if (isAllImagesValid && FileHandler.getFileSize(
                                    uri,
                                    mContext
                                ) > VALID_IMAGE_SIZE
                            ) {
                                isAllImagesValid = false
                            }
                            mSelectedImagesList.add(uri)
                        }
                    }
                    mSelectedImagesRVTil.visibility = View.VISIBLE
                    mRecyclerViewAdapter.setSelectedImages(mSelectedImagesList)
                }
            } else if (data.data != null) {
                mSelectedImagesList.clear()
                mSelectedImagesList.add(data.data!!)

                if (FileHandler.getFileSize(data.data!!, mContext) > VALID_IMAGE_SIZE) {
                    isAllImagesValid = false
                }

                mSelectedImagesRVTil.visibility = View.VISIBLE
                mRecyclerViewAdapter.setSelectedImages(mSelectedImagesList)
            } else {
                mSelectedImagesRVTil.visibility = View.GONE
                mRecyclerViewAdapter.setSelectedImages(ArrayList())
            }

            if (!isAllImagesValid) {
                mSelectedImagesRVTil.isErrorEnabled = true
                mSelectedImagesRVTil.error =
                    "Image highlighted in red has exceeded the prescribed limit of 2MB"

                VibrateView.vibrate(
                    mContext,
                    R.anim.shake,
                    mSelectedImagesRVTil
                )
            } else {
                mSelectedImagesRVTil.isErrorEnabled = false
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (v != null) {
            when (v.id) {
                mTenantPhoneOrEmailEt.id -> {
                    if (!hasFocus) {
                        mTenantPhoneOrEmailEt.text?.let {
                            when {
                                it.trim().isEmpty() -> {
                                    mTenantPhoneOrEmailTil.apply {
                                        isErrorEnabled = true
                                        error =
                                            "Tenant contact info, either phone or email is required"
                                    }
                                    VibrateView.vibrate(
                                        mContext,
                                        R.anim.shake,
                                        mTenantPhoneOrEmailTil
                                    )
                                }
                                !mIsValidPhone && !mIsValidEmail -> {
                                    val message: String? =
                                        if (it.trim().isDigitsOnly() && !mIsValidPhone) {
                                            "Phone number must be 10 characters long"
                                        } else if (!it.trim().isDigitsOnly() && !mIsValidEmail) {
                                            "Please provide valid email address"
                                        } else if (!mIsValidPhone && !mIsValidEmail) {
                                            "Please provide valid email address or phone number"
                                        } else {
                                            handlePhoneOrEmailValidation(it.trim().toString())
                                            null
                                        }

                                    if (message != null) {
                                        mTenantPhoneOrEmailTil.apply {
                                            isErrorEnabled = true
                                            error = message
                                        }
                                        VibrateView.vibrate(
                                            mContext,
                                            R.anim.shake,
                                            mTenantPhoneOrEmailTil
                                        )
                                    } else {
                                        mTenantPhoneOrEmailTil.isErrorEnabled = false
                                    }
                                }
                            }
                        }
                    } else {
                        mTenantPhoneOrEmailTil.isErrorEnabled = false
                    }
                }

                mRoomCountEt.id -> {
                    if (!hasFocus) {
                        mRoomCountEt.text?.let {
                            when {
                                it.trim().isEmpty() -> {
                                    mRoomCountTil.apply {
                                        isErrorEnabled = true
                                        error = "Total rooms lent is required"
                                    }
                                    VibrateView.vibrate(
                                        mContext,
                                        R.anim.shake,
                                        mRoomCountTil
                                    )
                                }
                                !it.trim().isDigitsOnly() -> {
                                    mRoomCountTil.apply {
                                        isErrorEnabled = true
                                        error = "Room count takes numeric value only"
                                    }
                                    VibrateView.vibrate(
                                        mContext,
                                        R.anim.shake,
                                        mRoomCountTil
                                    )
                                }
                            }
                        }
                    } else {
                        mRoomCountTil.isErrorEnabled = false
                    }
                }

                mRentAmountEt.id -> {
                    if (!hasFocus) {
                        mRentAmountEt.text?.let {
                            if (it.trim().isEmpty()) {
                                mRentAmountTil.apply {
                                    isErrorEnabled = true
                                    error = "Agreed upon rent amount is required"
                                }
                                VibrateView.vibrate(
                                    mContext,
                                    R.anim.shake,
                                    mRentAmountTil
                                )
                            } else if (!it.trim().isDigitsOnly()) {
                                mRentAmountTil.apply {
                                    isErrorEnabled = true
                                    error = "Rent amount takes numeric value only"
                                }
                                VibrateView.vibrate(
                                    mContext,
                                    R.anim.shake,
                                    mRentAmountTil
                                )
                            }
                        }
                    } else {
                        mRentAmountTil.isErrorEnabled = false
                    }
                }
            }
        }
    }

    private fun validate(): Boolean {
        var isValid = true

        mTenantPhoneOrEmailEt.text?.let {
            when {
                it.trim().isEmpty() -> {
                    isValid = false
                    mTenantPhoneOrEmailTil.apply {
                        isErrorEnabled = true
                        error = "Tenant contact info, either phone or email is required"
                    }
                    VibrateView.vibrate(
                        mContext,
                        R.anim.shake,
                        mTenantPhoneOrEmailTil
                    )
                }
                !mIsValidPhone && !mIsValidEmail && !mIsValidUser -> {
                    val message = if (it.trim().isDigitsOnly() && !mIsValidPhone) {
                        "Phone number must be 10 characters long"
                    } else if (!it.trim().isDigitsOnly() && !mIsValidEmail) {
                        "Please provide valid email address"
                    } else if (mIsValidPhone && !mIsValidUser) {
                        "This phone number is not associated with any tenant"
                    } else if (mIsValidEmail && !mIsValidUser) {
                        "This email address is not associated with any tenant"
                    } else null

                    if (message != null) {
                        isValid = false
                        mTenantPhoneOrEmailTil.apply {
                            isErrorEnabled = true
                            error = message
                        }
                    } else {
                        mTenantPhoneOrEmailTil.isErrorEnabled = false
                    }
                }
                else -> null
            }
        }

        mRoomCountEt.text?.let {
            when {
                it.trim().isEmpty() -> {
                    isValid = false
                    mRoomCountTil.apply {
                        isErrorEnabled = true
                        error = "Total rooms lent is required"
                    }
                }
                !it.trim().isDigitsOnly() -> {
                    isValid = false
                    mRoomCountTil.apply {
                        isErrorEnabled = true
                        error = "Room count takes numeric value only"
                    }
                }
                else -> {
                    mRoomCountTil.isErrorEnabled = false
                }
            }
        }

        mRentAmountEt.text?.let {
            when {
                it.trim().isEmpty() -> {
                    isValid = false
                    mRentAmountTil.apply {
                        isErrorEnabled = true
                        error = "Agreed upon rent amount is required"
                    }
                }
                !it.trim().isDigitsOnly() -> {
                    isValid = false
                    mRentAmountTil.apply {
                        isErrorEnabled = true
                        error = "Rent amount takes numeric value only"
                    }
                }
                else -> {
                    mRentAmountTil.isErrorEnabled = false
                }
            }
        }

        mRoomTypeSpn.let {
            when (it.selectedItemPosition) {
                0 -> {
                    isValid = false
                    mRoomTypeTil.apply {
                        isErrorEnabled = true
                        error =
                            "Weather tenant has taken whole flat or shared the flat with others is required"
                    }
                }
                else -> {
                    mRoomTypeTil.isErrorEnabled = false
                }
            }
        }

        mRentAccrueSpn.let {
            when (it.selectedItemPosition) {
                0 -> {
                    isValid = false
                    mRentAccrueTil.apply {
                        isErrorEnabled = true
                        error =
                            "Weather rent gets accrued at the start or end of the month is required"
                    }
                }
                else -> {
                    mRentAccrueTil.isErrorEnabled = false
                }
            }
        }

        if (mSelectedTenancyStartDate == null) {
            isValid = false
            mTenancyStartDateTil.isErrorEnabled = true
            mTenancyStartDateTil.error = "Tenancy start date is required"
        }

        if (mSelectedImagesList.size > 0) {
            var isAllImagesValid = true
            mSelectedImagesList.forEach {
                if (isAllImagesValid && FileHandler.getFileSize(
                        it,
                        mContext
                    ) > VALID_IMAGE_SIZE
                ) {
                    isAllImagesValid = false
                }
            }
            if (!isAllImagesValid) {
                isValid = false
                mSelectedImagesRVTil.isErrorEnabled = true
                mSelectedImagesRVTil.error =
                    "Image highlighted in red has exceeded the prescribed limit of 2MB"
            }
        }
        if (!isValid) {
            VibrateView.vibrate(
                mContext,
                R.anim.shake,
                requireView().findViewById(R.id.formCardView)
            )
        }
        return isValid
    }


    private fun handlePhoneOrEmailValidation(phoneOrEmail: String) {
        val consumer = APIService.getService(APIConsumer::class.java)
        var validatePhoneOrEmail: Call<ResponseBody>? = null

        val jsonObject = JSONObject().apply {
            if (mIsValidPhone) {
                put(User.PHONE, phoneOrEmail.toLong())
            } else if (mIsValidEmail) {
                put(User.EMAIL, phoneOrEmail)
            }
        }
        val requestBody = RequestBody.create(MediaType.parse(TYPE_JSON), jsonObject.toString())
        if (mIsValidPhone) {
            validatePhoneOrEmail = consumer.validatePhoneNumber(requestBody)
        } else if (mIsValidEmail) {
            validatePhoneOrEmail = consumer.validateEmailAddress(requestBody)
        }

        validatePhoneOrEmail?.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val jsonResponse = JSONObject(response.body()!!.string())
                    Log.d(TAG, "onResponse: $jsonResponse")
                    mIsValidUser =
                        jsonResponse.has("isUnique") && !jsonResponse.getBoolean("isUnique")

                    var user: User? = null
                    if (jsonResponse.has("user") && !jsonResponse.isNull("user")) {
                        user = User(jsonResponse.getJSONObject("user"))
                    }

                    if (!mIsValidUser) {
                        mTenantDetails.apply {
                            text = ""
                            visibility = View.GONE
                        }
                        mTenantPhoneOrEmailTil.apply {
                            endIconMode = TextInputLayout.END_ICON_NONE
                            isErrorEnabled = true
                            error = when {
                                mIsValidPhone -> {
                                    "This phone number is not associated with any tenant"
                                }
                                mIsValidEmail -> {
                                    "This email address is not associated with any tenant"
                                }
                                else -> {
                                    "Unable to validate the tenant phone number or email address"
                                }
                            }
                        }

                        mTenantDetails.visibility = View.GONE
                        mTenantDetails.text = ""

                    } else {
                        mTenantPhoneOrEmailTil.apply {
                            isErrorEnabled = false
                            endIconMode = TextInputLayout.END_ICON_CUSTOM
                            setEndIconDrawable(R.drawable.check_circle_green)
                        }

                        user?.let {
                            mTenantDetails.visibility = View.VISIBLE
                            mTenantDetails.text = it.fullName?.toUpperCase(Locale.ROOT)
                        }
                    }
                } else {
                    mTenantPhoneOrEmailTil.apply {
                        isErrorEnabled = true
                        error = when {
                            mIsValidPhone -> {
                                "Unable to validate the tenant phone number"
                            }
                            mIsValidEmail -> {
                                "Unable to validate the tenant email address"
                            }
                            else -> {
                                "Unable to validate the tenant phone number or email address"
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                mTenantPhoneOrEmailTil.apply {
                    isErrorEnabled = true
                    error = when {
                        mIsValidPhone -> {
                            "Unable to validate the tenant phone number"
                        }
                        mIsValidEmail -> {
                            "Unable to validate the tenant email address"
                        }
                        else -> {
                            "Unable to validate the tenant phone number or email address"
                        }
                    }
                }
            }
        })
    }


    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        if (event != null && event.action == KeyEvent.ACTION_UP && v != null) {
            when (v.id) {
                mTenantPhoneOrEmailEt.id -> {
                    if (mTenantPhoneOrEmailEt.text != null && mTenantPhoneOrEmailEt.text!!.isNotEmpty()) {

                        if (mTenantPhoneOrEmailTil.isErrorEnabled) mTenantPhoneOrEmailTil.isErrorEnabled =
                            false
                        Log.d(TAG, "onKey: hello")
                        if (mTenantDetails.visibility == View.VISIBLE) {
                            mTenantDetails.apply {
                                text = ""
                                visibility = View.GONE
                            }
                        }
                        mTenantPhoneOrEmailTil.endIconMode = TextInputLayout.END_ICON_NONE

                        val phoneOrEmail = mTenantPhoneOrEmailEt.text!!.trim().toString()
                        mIsValidPhone =
                            phoneOrEmail.trim().isDigitsOnly() && phoneOrEmail.trim().length == 10
                        mIsValidEmail =
                            Patterns.EMAIL_ADDRESS.matcher(phoneOrEmail.trim()).matches()

                        Log.d(
                            TAG,
                            "onKey: isValidPhone -> ${mIsValidPhone}, isValidEmail -> ${mIsValidEmail}"
                        )
                        if (mIsValidPhone && AppState.loggedInUser!!.phone != null && AppState.loggedInUser!!.phone == phoneOrEmail.toLong()) {
                            mTenantPhoneOrEmailTil.apply {
                                isErrorEnabled = true
                                error = "Own contact number provided"
                            }
                        } else if (mIsValidEmail && AppState.loggedInUser!!.email != null && AppState.loggedInUser!!.email == phoneOrEmail) {
                            mTenantPhoneOrEmailTil.apply {
                                isErrorEnabled = true
                                error = "Own email address provided"
                            }
                        } else if (mIsValidPhone || mIsValidEmail)
                            handlePhoneOrEmailValidation(phoneOrEmail)

                    } else {
                        mIsValidPhone = false
                        mIsValidEmail = false
                    }
                    return true
                }
            }
        }
        return false
    }

    override fun onDeleteClick(position: Int) {
        mSelectedImagesList.removeAt(position)
        mRecyclerViewAdapter.setSelectedImages(mSelectedImagesList)

        if (mSelectedImagesList.size > 0) {
            var isAllImagesValid = true

            mSelectedImagesList.forEach {
                if (isAllImagesValid && FileHandler.getFileSize(
                        it,
                        mContext
                    ) > VALID_IMAGE_SIZE
                ) {
                    isAllImagesValid = false
                    return
                }
            }

            if (!isAllImagesValid) {
                mSelectedImagesRVTil.isErrorEnabled = true
                mSelectedImagesRVTil.error =
                    "Image highlighted in red has exceeded the prescribed limit of 2MB"
            } else {
                mSelectedImagesRVTil.isErrorEnabled = false
            }
        } else {
            mSelectedImagesRVTil.visibility = View.GONE
            mRecyclerViewAdapter.setSelectedImages(mSelectedImagesList)
        }
    }

    private fun addTenancy(tenancy: Tenancy) {
        val requestBody = HashMap<String, RequestBody>().apply {
            put(
                Tenancy.START_DATE,
                RequestBody.create(
                    MultipartBody.FORM,
                    SimpleDateFormat(
                        yyyy_MM_dd,
                        Locale.ENGLISH
                    ).format(mSelectedTenancyStartDate!!)
                )
            )

            put(
                Tenancy.ROOM_TYPE,
                RequestBody.create(MultipartBody.FORM, tenancy.roomType.toString())
            )

            put(
                Tenancy.ROOM_COUNT,
                RequestBody.create(MultipartBody.FORM, tenancy.roomCount.toString())
            )

            put(Tenancy.ACCRUE, RequestBody.create(MultipartBody.FORM, tenancy.accrue.toString()))

            put(Tenancy.AMOUNT, RequestBody.create(MultipartBody.FORM, tenancy.amount.toString()))

            tenancy.tenant?.email?.let {
                put(User.EMAIL, RequestBody.create(MultipartBody.FORM, it))
            }

            tenancy.tenant?.phone.let {
                put(User.PHONE, RequestBody.create(MultipartBody.FORM, it.toString()))
            }
        }

        val images: ArrayList<MultipartBody.Part> = ArrayList()
        if (mSelectedImagesList.size > 0) {
            mSelectedImagesList.forEach {
                val selectedFile = FileHandler.getFile(it, mContext)
                val filePart = RequestBody.create(MultipartBody.FORM, selectedFile)
                images.add(
                    MultipartBody.Part.createFormData(
                        Tenancy.IMAGES,
                        selectedFile.name,
                        filePart
                    )
                )

            }
        }

        val consumer = APIService.getService(APIConsumer::class.java)
        val addTenancy = consumer.addTenancy(
            UserToken.getInstance(mContext).token!!,
            mParamHouseID!!,
            images,
            requestBody
        )

        addTenancy.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val tenancyResponse = Tenancy(JSONObject(response.body()!!.string()))
                    mViewModel.addTenancy(mParamHousePosition, tenancyResponse)

                    AppToast.show(
                        mContext,
                        "Tenancy has been added successfully",
                        Toast.LENGTH_LONG
                    )

                    clearFields()
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
                        displayAPIErrorResponseDialog("Unable to add tenant. Please try again later")
                    }
                } else {
                    displayAPIErrorResponseDialog("Unable to add tenant. Please try again later")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                displayAPIErrorResponseDialog("Unable to add tenant. Please try again later")
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


    private fun clearFields() {
        mTenantPhoneOrEmailEt.text?.clear()
        mTenancyStartDateCV.date = Calendar.getInstance().time.time
        mRentAmountEt.text?.clear()
        mRoomCountEt.text?.clear()
        mRoomTypeSpn.setSelection(0)
        mRentAccrueSpn.setSelection(0)
        mRecyclerViewAdapter.setSelectedImages(ArrayList())
    }
}

